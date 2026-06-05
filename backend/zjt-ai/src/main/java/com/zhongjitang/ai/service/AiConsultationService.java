package com.zhongjitang.ai.service;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.vo.ConsultationResponse;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConsultationService {

    private final LlmGatewayService llmGatewayService;

    private static final String SYSTEM_PROMPT = "你是忠济堂中医养生连锁的AI问诊专家，精通中医理论和九种体质辨识。" +
            "你需要根据用户的症状描述，运用中医四诊(望闻问切)的思路进行分析，" +
            "辨识用户的体质类型，并给出针对性的养生建议和推荐服务。" +
            "九种体质：平和质(A0)、气虚质(A1)、阳虚质(A2)、阴虚质(A3)、痰湿质(A4)、湿热质(A5)、血瘀质(A6)、气郁质(A7)、特禀质(A8)。" +
            "请以JSON格式返回，包含：answer(回答)、constitutionType(体质编码)、constitutionName(体质名称)、" +
            "suggestions(养生建议列表)、recommendedItems(推荐服务项目列表)。";

    private static final Map<String, String> CONSTITUTION_MAP = new LinkedHashMap<>();

    static {
        CONSTITUTION_MAP.put("A0", "平和质");
        CONSTITUTION_MAP.put("A1", "气虚质");
        CONSTITUTION_MAP.put("A2", "阳虚质");
        CONSTITUTION_MAP.put("A3", "阴虚质");
        CONSTITUTION_MAP.put("A4", "痰湿质");
        CONSTITUTION_MAP.put("A5", "湿热质");
        CONSTITUTION_MAP.put("A6", "血瘀质");
        CONSTITUTION_MAP.put("A7", "气郁质");
        CONSTITUTION_MAP.put("A8", "特禀质");
    }

    public ConsultationResponse consult(String question, Long memberId) {
        LlmRequest request = new LlmRequest();
        request.setSceneType("consultation");

        List<LlmRequest.Message> messages = new ArrayList<>();
        LlmRequest.Message systemMsg = new LlmRequest.Message();
        systemMsg.setRole("system");
        systemMsg.setContent(SYSTEM_PROMPT);
        messages.add(systemMsg);

        LlmRequest.Message userMsg = new LlmRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent(question);
        messages.add(userMsg);

        request.setMessages(messages);
        request.setTemperature(0.7);

        LlmResponse response = llmGatewayService.chat(request);
        return parseConsultationResponse(response.getContent());
    }

    public ConsultationResponse getConstitutionType(String symptoms) {
        String prompt = "请根据以下症状描述，辨识用户的中医体质类型，并给出养生建议和推荐服务。\n" +
                "症状描述：" + symptoms + "\n" +
                "请严格以JSON格式返回：{\"constitutionType\":\"A0\",\"constitutionName\":\"平和质\"," +
                "\"answer\":\"分析回答\",\"suggestions\":[\"建议1\",\"建议2\"],\"recommendedItems\":[\"项目1\",\"项目2\"]}";

        LlmRequest request = new LlmRequest();
        request.setSceneType("constitution");

        List<LlmRequest.Message> messages = new ArrayList<>();
        LlmRequest.Message systemMsg = new LlmRequest.Message();
        systemMsg.setRole("system");
        systemMsg.setContent(SYSTEM_PROMPT);
        messages.add(systemMsg);

        LlmRequest.Message userMsg = new LlmRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent(prompt);
        messages.add(userMsg);

        request.setMessages(messages);
        request.setTemperature(0.3);

        LlmResponse response = llmGatewayService.chat(request);
        return parseConsultationResponse(response.getContent());
    }

    private ConsultationResponse parseConsultationResponse(String content) {
        ConsultationResponse result = new ConsultationResponse();
        try {
            // 尝试从返回内容中提取JSON
            String jsonStr = content;
            if (content.contains("```json")) {
                jsonStr = content.substring(content.indexOf("```json") + 7);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            } else if (content.contains("```")) {
                jsonStr = content.substring(content.indexOf("```") + 3);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            }
            jsonStr = jsonStr.trim();

            // 简单JSON解析（避免引入额外依赖）
            if (jsonStr.startsWith("{")) {
                result.setConstitutionType(extractJsonValue(jsonStr, "constitutionType"));
                result.setConstitutionName(extractJsonValue(jsonStr, "constitutionName"));
                result.setAnswer(extractJsonValue(jsonStr, "answer"));
            }

            // 如果未能解析出体质类型，设置默认值
            if (result.getConstitutionType() == null || result.getConstitutionType().isEmpty()) {
                result.setConstitutionType("A0");
                result.setConstitutionName("平和质");
            }
            if (result.getAnswer() == null || result.getAnswer().isEmpty()) {
                result.setAnswer(content);
            }
            if (result.getSuggestions() == null) {
                result.setSuggestions(Collections.emptyList());
            }
            if (result.getRecommendedItems() == null) {
                result.setRecommendedItems(Collections.emptyList());
            }
        } catch (Exception e) {
            log.warn("解析问诊响应失败，返回原始内容", e);
            result.setAnswer(content);
            result.setConstitutionType("A0");
            result.setConstitutionName("平和质");
            result.setSuggestions(Collections.emptyList());
            result.setRecommendedItems(Collections.emptyList());
        }
        return result;
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) {
            return null;
        }
        int colonIdx = json.indexOf(":", idx + pattern.length());
        if (colonIdx < 0) {
            return null;
        }
        int startQuote = json.indexOf("\"", colonIdx + 1);
        if (startQuote < 0) {
            return null;
        }
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote < 0) {
            return null;
        }
        return json.substring(startQuote + 1, endQuote);
    }
}
