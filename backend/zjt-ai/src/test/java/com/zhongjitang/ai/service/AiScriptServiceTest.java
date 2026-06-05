package com.zhongjitang.ai.service;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.dto.ScriptRequest;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.ai.domain.vo.ScriptResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiScriptServiceTest {

    @Mock
    private LlmGatewayService llmGatewayService;

    @InjectMocks
    private AiScriptService aiScriptService;

    private LlmResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new LlmResponse();
        mockResponse.setContent("{\"script\":\"您好，根据您的体质分析，建议您体验我们的艾灸调理服务...\",\"tips\":[\"先了解客户需求\",\"循序渐进推荐\"]}");
        mockResponse.setModel("deepseek-chat");
        mockResponse.setProvider("deepseek");
    }

    @Test
    void testGenerateConsultationScript() {
        when(llmGatewayService.chat(any(LlmRequest.class))).thenReturn(mockResponse);

        ScriptRequest request = new ScriptRequest();
        request.setSceneType("consultation");
        request.setCustomerInfo("气虚质，容易疲劳");
        request.setTargetService("艾灸调理");

        ScriptResponse response = aiScriptService.generateScript(request);

        assertNotNull(response);
        assertEquals("consultation", response.getSceneType());
        assertNotNull(response.getScript());
        verify(llmGatewayService, times(1)).chat(any(LlmRequest.class));
    }

    @Test
    void testGenerateUpsellScript() {
        when(llmGatewayService.chat(any(LlmRequest.class))).thenReturn(mockResponse);

        ScriptRequest request = new ScriptRequest();
        request.setSceneType("upsell");
        request.setCustomerInfo("老客户，偏好推拿");
        request.setTargetService("体质调理套餐");

        ScriptResponse response = aiScriptService.generateScript(request);

        assertNotNull(response);
        assertEquals("upsell", response.getSceneType());
        verify(llmGatewayService, times(1)).chat(any(LlmRequest.class));
    }

    @Test
    void testGenerateFollowupScript() {
        when(llmGatewayService.chat(any(LlmRequest.class))).thenReturn(mockResponse);

        ScriptRequest request = new ScriptRequest();
        request.setSceneType("followup");
        request.setCustomerInfo("上周做了艾灸");
        request.setContext("回访服务效果");

        ScriptResponse response = aiScriptService.generateScript(request);

        assertNotNull(response);
        assertEquals("followup", response.getSceneType());
        verify(llmGatewayService, times(1)).chat(any(LlmRequest.class));
    }
}
