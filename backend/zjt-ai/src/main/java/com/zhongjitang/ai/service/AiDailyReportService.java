package com.zhongjitang.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.entity.AiDailyReportDO;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.ai.mapper.AiDailyReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDailyReportService {

    private final LlmGatewayService llmGatewayService;
    private final AiDailyReportMapper dailyReportMapper;

    private static final String REPORT_SYSTEM_PROMPT = "你是忠济堂中医养生连锁的数据分析师。" +
            "你需要根据门店的运营数据，生成专业的日报分析报告。" +
            "报告应包含：1.当日运营概况总结 2.关键指标分析 3.问题诊断 4.改进建议。" +
            "请以JSON格式返回：{\"summary\":\"概况总结\",\"suggestions\":\"改进建议\"}";

    public AiDailyReportDO generateDailyReport(Long storeId, LocalDate date) {
        // 1. 聚合门店当日数据（模拟，实际应从其他模块获取）
        String metricsSummary = aggregateStoreMetrics(storeId, date);

        // 2. 构建数据摘要
        String userContent = "请为门店ID=" + storeId + "生成" + date + "的日报分析。\n" +
                "当日运营数据：\n" + metricsSummary;

        // 3. 调用LlmGatewayService生成分析报告
        LlmRequest request = new LlmRequest();
        request.setSceneType("daily_report");

        List<LlmRequest.Message> messages = new ArrayList<>();
        LlmRequest.Message systemMsg = new LlmRequest.Message();
        systemMsg.setRole("system");
        systemMsg.setContent(REPORT_SYSTEM_PROMPT);
        messages.add(systemMsg);

        LlmRequest.Message userMsg = new LlmRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent(userContent);
        messages.add(userMsg);

        request.setMessages(messages);
        request.setTemperature(0.5);

        LlmResponse response = llmGatewayService.chat(request);

        // 4. 保存到ai_daily_report表
        AiDailyReportDO report = new AiDailyReportDO();
        report.setReportDate(date);
        report.setStoreId(storeId);
        report.setReportType("daily");
        report.setMetricsJson(metricsSummary);
        report.setSummary(extractSummary(response.getContent()));
        report.setSuggestions(extractSuggestions(response.getContent()));
        dailyReportMapper.insert(report);

        return report;
    }

    public AiDailyReportDO getReport(Long storeId, LocalDate date) {
        LambdaQueryWrapper<AiDailyReportDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiDailyReportDO::getStoreId, storeId)
                .eq(AiDailyReportDO::getReportDate, date)
                .orderByDesc(AiDailyReportDO::getCreatedAt)
                .last("LIMIT 1");
        return dailyReportMapper.selectOne(wrapper);
    }

    public AiDailyReportDO getReportById(Long id) {
        return dailyReportMapper.selectById(id);
    }

    public List<AiDailyReportDO> listReports(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<AiDailyReportDO> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(AiDailyReportDO::getStoreId, storeId);
        }
        if (startDate != null) {
            wrapper.ge(AiDailyReportDO::getReportDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(AiDailyReportDO::getReportDate, endDate);
        }
        wrapper.orderByDesc(AiDailyReportDO::getReportDate);
        return dailyReportMapper.selectList(wrapper);
    }

    private String aggregateStoreMetrics(Long storeId, LocalDate date) {
        // 模拟门店数据聚合（实际应调用zjt-trade/zjt-data等模块的接口）
        return "{\n" +
                "  \"appointmentCount\": 45,\n" +
                "  \"completedCount\": 38,\n" +
                "  \"revenue\": 15800.00,\n" +
                "  \"customerFlow\": 42,\n" +
                "  \"cancelRate\": 0.07,\n" +
                "  \"avgServiceDuration\": 65,\n" +
                "  \"topService\": \"中医推拿\",\n" +
                "  \"newCustomerCount\": 8,\n" +
                "  \"returnCustomerCount\": 34\n" +
                "}";
    }

    private String extractSummary(String content) {
        try {
            String jsonStr = content;
            if (content.contains("```json")) {
                jsonStr = content.substring(content.indexOf("```json") + 7);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            } else if (content.contains("```")) {
                jsonStr = content.substring(content.indexOf("```") + 3);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            }
            jsonStr = jsonStr.trim();

            String pattern = "\"summary\"";
            int idx = jsonStr.indexOf(pattern);
            if (idx >= 0) {
                int colonIdx = jsonStr.indexOf(":", idx + pattern.length());
                int startQuote = jsonStr.indexOf("\"", colonIdx + 1);
                int endQuote = jsonStr.indexOf("\"", startQuote + 1);
                if (startQuote >= 0 && endQuote >= 0) {
                    return jsonStr.substring(startQuote + 1, endQuote);
                }
            }
        } catch (Exception e) {
            log.warn("提取摘要失败", e);
        }
        return content;
    }

    private String extractSuggestions(String content) {
        try {
            String jsonStr = content;
            if (content.contains("```json")) {
                jsonStr = content.substring(content.indexOf("```json") + 7);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            } else if (content.contains("```")) {
                jsonStr = content.substring(content.indexOf("```") + 3);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            }
            jsonStr = jsonStr.trim();

            String pattern = "\"suggestions\"";
            int idx = jsonStr.indexOf(pattern);
            if (idx >= 0) {
                int colonIdx = jsonStr.indexOf(":", idx + pattern.length());
                int startQuote = jsonStr.indexOf("\"", colonIdx + 1);
                int endQuote = jsonStr.indexOf("\"", startQuote + 1);
                if (startQuote >= 0 && endQuote >= 0) {
                    return jsonStr.substring(startQuote + 1, endQuote);
                }
            }
        } catch (Exception e) {
            log.warn("提取建议失败", e);
        }
        return "";
    }
}
