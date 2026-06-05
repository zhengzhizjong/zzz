package com.zhongjitang.ai.service;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.vo.ConsultationResponse;
import com.zhongjitang.ai.domain.vo.LlmResponse;
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
class AiConsultationServiceTest {

    @Mock
    private LlmGatewayService llmGatewayService;

    @InjectMocks
    private AiConsultationService aiConsultationService;

    private LlmResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new LlmResponse();
        mockResponse.setContent("{\"constitutionType\":\"A1\",\"constitutionName\":\"气虚质\"," +
                "\"answer\":\"根据您的症状分析，您属于气虚体质\",\"suggestions\":[\"补气养血\"]," +
                "\"recommendedItems\":[\"艾灸调理\"]}");
        mockResponse.setModel("deepseek-chat");
        mockResponse.setProvider("deepseek");
    }

    @Test
    void testConsult() {
        when(llmGatewayService.chat(any(LlmRequest.class))).thenReturn(mockResponse);

        ConsultationResponse response = aiConsultationService.consult("经常感到疲劳乏力", 1L);

        assertNotNull(response);
        assertNotNull(response.getAnswer());
        verify(llmGatewayService, times(1)).chat(any(LlmRequest.class));
    }

    @Test
    void testGetConstitutionType() {
        when(llmGatewayService.chat(any(LlmRequest.class))).thenReturn(mockResponse);

        ConsultationResponse response = aiConsultationService.getConstitutionType("容易疲劳，气短懒言");

        assertNotNull(response);
        assertNotNull(response.getConstitutionType());
        verify(llmGatewayService, times(1)).chat(any(LlmRequest.class));
    }

    @Test
    void testConsultWithNonJsonResponse() {
        LlmResponse nonJsonResponse = new LlmResponse();
        nonJsonResponse.setContent("这是一段普通的文本回复，不是JSON格式");
        nonJsonResponse.setModel("deepseek-chat");
        nonJsonResponse.setProvider("deepseek");

        when(llmGatewayService.chat(any(LlmRequest.class))).thenReturn(nonJsonResponse);

        ConsultationResponse response = aiConsultationService.consult("测试问题", null);

        assertNotNull(response);
        assertEquals("这是一段普通的文本回复，不是JSON格式", response.getAnswer());
        assertEquals("A0", response.getConstitutionType());
    }
}
