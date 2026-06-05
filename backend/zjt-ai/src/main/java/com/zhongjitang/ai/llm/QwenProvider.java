package com.zhongjitang.ai.llm;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Component
public class QwenProvider implements LlmProvider {

    @Value("${ai.qwen.api-key:sk-placeholder}")
    private String apiKey;

    @Value("${ai.qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${ai.qwen.default-model:qwen-plus}")
    private String defaultModel;

    private static final Set<String> SUPPORTED_MODELS = new HashSet<>(Arrays.asList(
            "qwen-plus", "qwen-turbo", "qwen-max"
    ));

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
        return "qwen";
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
