package com.zhongjitang.ai.service;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.dto.ScriptRequest;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.ai.domain.vo.ScriptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiScriptService {

    private final LlmGatewayService llmGatewayService;

    private static final Map<String, String> SCENE_PROMPTS = new HashMap<>();

    static {
        SCENE_PROMPTS.put("consultation",
                "你是忠济堂中医养生连锁的专业咨询师。根据客户的体质信息和需求，" +
                "生成一段专业的中医养生咨询话术。话术应体现专业性和亲和力，" +
                "引导客户了解自身体质并推荐合适的养生服务。");
        SCENE_PROMPTS.put("upsell",
                "你是忠济堂中医养生连锁的高级顾问。根据客户的历史消费记录和偏好，" +
                "生成一段增值推荐话术。话术应自然过渡，不生硬推销，" +
                "突出服务的健康价值和客户收益。");
        SCENE_PROMPTS.put("followup",
                "你是忠济堂中医养生连锁的客服专员。根据客户的服务记录，" +
                "生成一段回访话术。话术应关心客户体验，了解服务效果，" +
                "并适时推荐后续养生方案。");
    }

    public ScriptResponse generateScript(ScriptRequest request) {
        String scenePrompt = SCENE_PROMPTS.getOrDefault(request.getSceneType(),
                SCENE_PROMPTS.get("consultation"));

        StringBuilder userContent = new StringBuilder();
        userContent.append("请生成一段").append(getSceneName(request.getSceneType())).append("话术。\n");
        if (request.getCustomerInfo() != null && !request.getCustomerInfo().isEmpty()) {
            userContent.append("客户信息：").append(request.getCustomerInfo()).append("\n");
        }
        if (request.getTargetService() != null && !request.getTargetService().isEmpty()) {
            userContent.append("目标服务：").append(request.getTargetService()).append("\n");
        }
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            userContent.append("上下文：").append(request.getContext()).append("\n");
        }
        userContent.append("请以JSON格式返回：{\"script\":\"话术内容\",\"tips\":[\"技巧1\",\"技巧2\"]}");

        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setSceneType("script_" + request.getSceneType());

        List<LlmRequest.Message> messages = new ArrayList<>();
        LlmRequest.Message systemMsg = new LlmRequest.Message();
        systemMsg.setRole("system");
        systemMsg.setContent(scenePrompt);
        messages.add(systemMsg);

        LlmRequest.Message userMsg = new LlmRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent(userContent.toString());
        messages.add(userMsg);

        llmRequest.setMessages(messages);
        llmRequest.setTemperature(0.8);

        LlmResponse response = llmGatewayService.chat(llmRequest);
        return parseScriptResponse(response.getContent(), request.getSceneType());
    }

    private ScriptResponse parseScriptResponse(String content, String sceneType) {
        ScriptResponse result = new ScriptResponse();
        result.setSceneType(sceneType);

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

            if (jsonStr.startsWith("{")) {
                result.setScript(extractJsonValue(jsonStr, "script"));
            }

            if (result.getScript() == null || result.getScript().isEmpty()) {
                result.setScript(content);
            }
            if (result.getTips() == null) {
                result.setTips(Collections.emptyList());
            }
        } catch (Exception e) {
            log.warn("解析话术响应失败，返回原始内容", e);
            result.setScript(content);
            result.setTips(Collections.emptyList());
        }
        return result;
    }

    private String getSceneName(String sceneType) {
        switch (sceneType) {
            case "consultation": return "咨询";
            case "upsell": return "增值推荐";
            case "followup": return "回访";
            default: return "咨询";
        }
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
