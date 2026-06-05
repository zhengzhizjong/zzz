package com.zhongjitang.ai.service;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.entity.AiLlmCallLogDO;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.ai.llm.LlmProvider;
import com.zhongjitang.ai.mapper.AiLlmCallLogMapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.redis.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmGatewayService {

    private final List<LlmProvider> providers;
    private final RedisUtil redisUtil;
    private final AiLlmCallLogMapper callLogMapper;

    @Value("${ai.gateway.rate-limit-per-minute:60}")
    private int rateLimitPerMinute;

    @Value("${ai.gateway.circuit-failure-threshold:0.5}")
    private double circuitFailureThreshold;

    @Value("${ai.gateway.circuit-window-minutes:5}")
    private int circuitWindowMinutes;

    private static final String RATE_LIMIT_KEY_PREFIX = "ai:gateway:rate:";
    private static final String CIRCUIT_KEY_PREFIX = "ai:gateway:circuit:";
    private static final String CIRCUIT_STATS_KEY_PREFIX = "ai:gateway:circuit:stats:";

    private static final String CIRCUIT_OPEN = "OPEN";
    private static final String CIRCUIT_HALF_OPEN = "HALF_OPEN";
    private static final String CIRCUIT_CLOSED = "CLOSED";

    private static final Map<String, BigDecimal> MODEL_PRICING = new HashMap<>();

    static {
        // 每千Token单价(元)
        MODEL_PRICING.put("deepseek-chat", new BigDecimal("0.001"));
        MODEL_PRICING.put("deepseek-reasoner", new BigDecimal("0.004"));
        MODEL_PRICING.put("qwen-plus", new BigDecimal("0.004"));
        MODEL_PRICING.put("qwen-turbo", new BigDecimal("0.002"));
        MODEL_PRICING.put("qwen-max", new BigDecimal("0.04"));
    }

    public LlmResponse chat(LlmRequest request) {
        // 1. 限流检查
        checkRateLimit();

        // 2. 路由选择
        LlmProvider provider = selectProvider(request.getModel());

        // 3. 熔断检查
        provider = checkCircuitBreaker(provider);

        // 4. 调用provider
        long startTime = System.currentTimeMillis();
        LlmResponse response;
        try {
            response = provider.chat(request).block();
        } catch (Exception e) {
            log.error("LLM调用失败, provider={}", provider.getProviderName(), e);
            recordCircuitFailure(provider.getProviderName());
            // 尝试切换备用provider
            LlmProvider fallback = findFallbackProvider(provider);
            if (fallback != null) {
                log.info("切换到备用provider: {}", fallback.getProviderName());
                try {
                    response = fallback.chat(request).block();
                    provider = fallback;
                } catch (Exception ex) {
                    log.error("备用provider也失败, provider={}", fallback.getProviderName(), ex);
                    throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI服务暂时不可用，请稍后重试");
                }
            } else {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI服务暂时不可用，请稍后重试");
            }
        }

        // 5. 记录调用日志
        long latencyMs = System.currentTimeMillis() - startTime;
        saveCallLog(request, response, provider, latencyMs, 200, null);

        // 6. 记录成功状态(重置熔断)
        recordCircuitSuccess(provider.getProviderName());

        return response;
    }

    public List<Map<String, Object>> getAvailableProviders() {
        return providers.stream().map(p -> {
            Map<String, Object> info = new HashMap<>();
            info.put("providerName", p.getProviderName());
            info.put("defaultModel", p.getDefaultModel());
            info.put("circuitState", getCircuitState(p.getProviderName()));
            return info;
        }).collect(java.util.stream.Collectors.toList());
    }

    public Map<String, Object> getUsageStatistics(String startDate, String endDate) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiLlmCallLogDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (startDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.ge(AiLlmCallLogDO::getCreatedAt, start);
        }
        if (endDate != null) {
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.le(AiLlmCallLogDO::getCreatedAt, end);
        }

        List<AiLlmCallLogDO> logs = callLogMapper.selectList(wrapper);

        int totalCalls = logs.size();
        int totalTokens = logs.stream().mapToInt(l -> l.getTotalTokens() != null ? l.getTotalTokens() : 0).sum();
        BigDecimal totalCost = logs.stream()
                .map(l -> l.getCostAmount() != null ? l.getCostAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int failedCalls = (int) logs.stream().filter(l -> l.getStatusCode() != null && l.getStatusCode() != 200).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCalls", totalCalls);
        stats.put("totalTokens", totalTokens);
        stats.put("totalCost", totalCost);
        stats.put("failedCalls", failedCalls);
        stats.put("successRate", totalCalls > 0 ? (double) (totalCalls - failedCalls) / totalCalls : 1.0);
        return stats;
    }

    private void checkRateLimit() {
        String key = RATE_LIMIT_KEY_PREFIX + "global";
        Long count = redisUtil.increment(key);
        if (count != null && count == 1) {
            redisUtil.expire(key, 60, TimeUnit.SECONDS);
        }
        if (count != null && count > rateLimitPerMinute) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "AI网关调用频率超限，每分钟最多" + rateLimitPerMinute + "次");
        }
    }

    private LlmProvider selectProvider(String model) {
        if (model != null) {
            for (LlmProvider provider : providers) {
                if (provider.supports(model)) {
                    return provider;
                }
            }
        }
        // 默认使用DeepSeek
        for (LlmProvider provider : providers) {
            if ("deepseek".equals(provider.getProviderName())) {
                return provider;
            }
        }
        if (!providers.isEmpty()) {
            return providers.get(0);
        }
        throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "无可用的AI模型提供方");
    }

    private LlmProvider checkCircuitBreaker(LlmProvider provider) {
        String state = getCircuitState(provider.getProviderName());
        if (CIRCUIT_OPEN.equals(state)) {
            log.warn("Provider {} 熔断开启，尝试切换备用", provider.getProviderName());
            LlmProvider fallback = findFallbackProvider(provider);
            if (fallback != null) {
                String fallbackState = getCircuitState(fallback.getProviderName());
                if (!CIRCUIT_OPEN.equals(fallbackState)) {
                    return fallback;
                }
            }
            // 所有provider都熔断，尝试半开
            log.warn("所有provider熔断，尝试半开状态调用 {}", provider.getProviderName());
        }
        return provider;
    }

    private LlmProvider findFallbackProvider(LlmProvider exclude) {
        for (LlmProvider provider : providers) {
            if (!provider.getProviderName().equals(exclude.getProviderName())) {
                String state = getCircuitState(provider.getProviderName());
                if (!CIRCUIT_OPEN.equals(state)) {
                    return provider;
                }
            }
        }
        return null;
    }

    private String getCircuitState(String providerName) {
        Object state = redisUtil.get(CIRCUIT_KEY_PREFIX + providerName);
        return state != null ? state.toString() : CIRCUIT_CLOSED;
    }

    private void recordCircuitFailure(String providerName) {
        String statsKey = CIRCUIT_STATS_KEY_PREFIX + providerName;
        Long failCount = redisUtil.increment(statsKey + ":fail");
        if (failCount != null && failCount == 1) {
            redisUtil.expire(statsKey + ":fail", circuitWindowMinutes, TimeUnit.MINUTES);
        }

        Object totalObj = redisUtil.get(statsKey + ":total");
        long total = totalObj != null ? Long.parseLong(totalObj.toString()) : 0;
        long totalAfter = total + 1;

        if (failCount != null && totalAfter > 0) {
            double failureRate = (double) failCount / totalAfter;
            if (failureRate >= circuitFailureThreshold && totalAfter >= 5) {
                redisUtil.setWithExpire(CIRCUIT_KEY_PREFIX + providerName, CIRCUIT_OPEN,
                        circuitWindowMinutes, TimeUnit.MINUTES);
                log.warn("Provider {} 熔断开启，失败率={}", providerName, failureRate);
            }
        }
    }

    private void recordCircuitSuccess(String providerName) {
        String statsKey = CIRCUIT_STATS_KEY_PREFIX + providerName;
        redisUtil.increment(statsKey + ":total");

        // 成功调用重置熔断状态为半开
        String currentState = getCircuitState(providerName);
        if (CIRCUIT_OPEN.equals(currentState) || CIRCUIT_HALF_OPEN.equals(currentState)) {
            redisUtil.setWithExpire(CIRCUIT_KEY_PREFIX + providerName, CIRCUIT_HALF_OPEN,
                    circuitWindowMinutes, TimeUnit.MINUTES);
        }
    }

    private void saveCallLog(LlmRequest request, LlmResponse response, LlmProvider provider,
                             long latencyMs, int statusCode, String errorMessage) {
        AiLlmCallLogDO callLog = new AiLlmCallLogDO();
        callLog.setModel(response != null ? response.getModel() : request.getModel());
        callLog.setProvider(provider.getProviderName());
        callLog.setPromptTokens(response != null ? response.getPromptTokens() : 0);
        callLog.setCompletionTokens(response != null ? response.getCompletionTokens() : 0);
        callLog.setTotalTokens(response != null ? response.getTotalTokens() : 0);
        callLog.setLatencyMs((int) latencyMs);
        callLog.setStatusCode(statusCode);
        callLog.setErrorMessage(errorMessage);
        callLog.setSceneType(request.getSceneType());
        callLog.setCostAmount(calculateCost(callLog.getModel(), callLog.getTotalTokens()));
        callLogMapper.insert(callLog);
    }

    private BigDecimal calculateCost(String model, int totalTokens) {
        BigDecimal pricePerThousand = MODEL_PRICING.getOrDefault(model, new BigDecimal("0.001"));
        return pricePerThousand.multiply(new BigDecimal(totalTokens)).divide(new BigDecimal(1000), 6, java.math.RoundingMode.HALF_UP);
    }
}
