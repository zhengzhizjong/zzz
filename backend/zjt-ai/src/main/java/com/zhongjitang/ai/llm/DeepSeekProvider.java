package com.zhongjitang.ai.llm;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.common.redis.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekProvider implements LlmProvider {

    private final RedisUtil redisUtil;

    @Value("${ai.deepseek.api-key:sk-placeholder}")
    private String apiKey;

    @Value("${ai.deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${ai.deepseek.default-model:deepseek-chat}")
    private String defaultModel;

    private static final Set<String> SUPPORTED_MODELS = new HashSet<>(Arrays.asList(
            "deepseek-chat", "deepseek-reasoner"
    ));

    private static final int RATE_LIMIT_PER_MINUTE = 60;

    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();
        }
        return webClient;
    }

    @Override
    public Mono<LlmResponse> chat(LlmRequest request) {
        // 限流检查
        String rateLimitKey = "ai:rate:deepseek";
        Long count = redisUtil.increment(rateLimitKey);
        if (count != null && count == 1) {
            redisUtil.expire(rateLimitKey, 60, java.util.concurrent.TimeUnit.SECONDS);
        }
        if (count != null && count > RATE_LIMIT_PER_MINUTE) {
            return Mono.error(new RuntimeException("DeepSeek调用频率超限，每分钟最多" + RATE_LIMIT_PER_MINUTE + "次"));
        }

        String model = request.getModel() != null ? request.getModel() : defaultModel;
        Map<String, Object> requestBody = buildRequestBody(model, request);

        long startTime = System.currentTimeMillis();

        return getWebClient().post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    LlmResponse llmResponse = new LlmResponse();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
                        if (message != null) {
                            llmResponse.setContent(message.get("content"));
                        }
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> usage = (Map<String, Object>) response.get("usage");
                    if (usage != null) {
                        llmResponse.setPromptTokens(usage.get("prompt_tokens") != null ? ((Number) usage.get("prompt_tokens")).intValue() : 0);
                        llmResponse.setCompletionTokens(usage.get("completion_tokens") != null ? ((Number) usage.get("completion_tokens")).intValue() : 0);
                        llmResponse.setTotalTokens(usage.get("total_tokens") != null ? ((Number) usage.get("total_tokens")).intValue() : 0);
                    }
                    llmResponse.setModel(model);
                    llmResponse.setProvider(getProviderName());
                    llmResponse.setLatencyMs(System.currentTimeMillis() - startTime);
                    return llmResponse;
                });
    }

    @Override
    public String getProviderName() {
        return "deepseek";
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }

    @Override
    public boolean supports(String model) {
        return model != null && SUPPORTED_MODELS.contains(model);
    }

    private Map<String, Object> buildRequestBody(String model, LlmRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        for (LlmRequest.Message msg : request.getMessages()) {
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            messages.add(m);
        }
        body.put("messages", messages);

        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        }
        return body;
    }
}
