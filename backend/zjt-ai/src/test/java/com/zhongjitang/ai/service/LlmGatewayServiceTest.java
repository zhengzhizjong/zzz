package com.zhongjitang.ai.service;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.entity.AiLlmCallLogDO;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.ai.llm.LlmProvider;
import com.zhongjitang.ai.mapper.AiLlmCallLogMapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.redis.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmGatewayServiceTest {

    @Mock
    private AiLlmCallLogMapper callLogMapper;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private LlmProvider deepSeekProvider;

    @Mock
    private LlmProvider qwenProvider;

    @InjectMocks
    private LlmGatewayService llmGatewayService;

    private LlmRequest testRequest;
    private LlmResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new LlmRequest();
        LlmRequest.Message msg = new LlmRequest.Message();
        msg.setRole("user");
        msg.setContent("测试消息");
        testRequest.setMessages(Collections.singletonList(msg));
        testRequest.setSceneType("test");

        testResponse = new LlmResponse();
        testResponse.setContent("测试回复");
        testResponse.setModel("deepseek-chat");
        testResponse.setProvider("deepseek");
        testResponse.setPromptTokens(10);
        testResponse.setCompletionTokens(20);
        testResponse.setTotalTokens(30);
        testResponse.setLatencyMs(100L);
    }

    @Test
    void testChatWithDefaultProvider() {
        when(redisUtil.increment(anyString())).thenReturn(1L);
        when(redisUtil.get(anyString())).thenReturn(null);
        when(deepSeekProvider.getProviderName()).thenReturn("deepseek");
        when(deepSeekProvider.supports(anyString())).thenReturn(true);
        when(deepSeekProvider.chat(any(LlmRequest.class))).thenReturn(Mono.just(testResponse));
        when(callLogMapper.insert(any(AiLlmCallLogDO.class))).thenReturn(1);

        LlmResponse response = llmGatewayService.chat(testRequest);

        assertNotNull(response);
        assertEquals("测试回复", response.getContent());
        assertEquals("deepseek", response.getProvider());
        verify(callLogMapper, times(1)).insert(any(AiLlmCallLogDO.class));
    }

    @Test
    void testChatWithModelRouting() {
        testRequest.setModel("qwen-plus");

        when(redisUtil.increment(anyString())).thenReturn(1L);
        when(redisUtil.get(anyString())).thenReturn(null);
        when(deepSeekProvider.getProviderName()).thenReturn("deepseek");
        when(qwenProvider.getProviderName()).thenReturn("qwen");
        when(qwenProvider.supports("qwen-plus")).thenReturn(true);
        when(qwenProvider.chat(any(LlmRequest.class))).thenReturn(Mono.just(testResponse));
        when(callLogMapper.insert(any(AiLlmCallLogDO.class))).thenReturn(1);

        LlmResponse response = llmGatewayService.chat(testRequest);

        assertNotNull(response);
        verify(qwenProvider, times(1)).chat(any(LlmRequest.class));
    }

    @Test
    void testRateLimitExceeded() {
        when(redisUtil.increment(anyString())).thenReturn(100L);

        assertThrows(BusinessException.class, () -> llmGatewayService.chat(testRequest));
    }

    @Test
    void testCircuitBreakerFallback() {
        when(redisUtil.increment(anyString())).thenReturn(1L);
        when(redisUtil.get(contains("circuit:deepseek"))).thenReturn("OPEN");
        when(redisUtil.get(contains("circuit:qwen"))).thenReturn("CLOSED");
        when(deepSeekProvider.getProviderName()).thenReturn("deepseek");
        when(qwenProvider.getProviderName()).thenReturn("qwen");
        when(qwenProvider.chat(any(LlmRequest.class))).thenReturn(Mono.just(testResponse));
        when(callLogMapper.insert(any(AiLlmCallLogDO.class))).thenReturn(1);

        LlmResponse response = llmGatewayService.chat(testRequest);

        assertNotNull(response);
        verify(qwenProvider, times(1)).chat(any(LlmRequest.class));
    }

    @Test
    void testGetAvailableProviders() {
        when(deepSeekProvider.getProviderName()).thenReturn("deepseek");
        when(deepSeekProvider.getDefaultModel()).thenReturn("deepseek-chat");
        when(qwenProvider.getProviderName()).thenReturn("qwen");
        when(qwenProvider.getDefaultModel()).thenReturn("qwen-plus");
        when(redisUtil.get(anyString())).thenReturn("CLOSED");

        var providers = llmGatewayService.getAvailableProviders();

        assertNotNull(providers);
        assertEquals(2, providers.size());
    }
}
