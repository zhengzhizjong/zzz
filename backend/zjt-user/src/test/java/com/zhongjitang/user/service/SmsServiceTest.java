package com.zhongjitang.user.service;

import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.redis.util.RedisUtil;
import com.zhongjitang.user.service.impl.SmsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private SmsServiceImpl smsService;

    private static final String TEST_PHONE = "13800138000";

    @Test
    void testSendVerifyCode() {
        when(redisUtil.hasKey(any())).thenReturn(false);
        doNothing().when(redisUtil).setWithExpire(any(), any(), anyLong(), any());

        assertDoesNotThrow(() -> {
            smsService.sendVerifyCode(TEST_PHONE);
        });

        verify(redisUtil, times(2)).setWithExpire(any(), any(), anyLong(), any());
    }

    @Test
    void testSendVerifyCodeRateLimited() {
        when(redisUtil.hasKey(any())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            smsService.sendVerifyCode(TEST_PHONE);
        });
        assertEquals(ErrorCode.RATE_LIMITED.getCode(), exception.getCode());
    }

    @Test
    void testVerifyCodeSuccess() {
        when(redisUtil.get(any())).thenReturn("123456");
        when(redisUtil.delete(any())).thenReturn(true);

        boolean result = smsService.verifyCode(TEST_PHONE, "123456");

        assertTrue(result);
        verify(redisUtil, times(1)).delete(any());
    }

    @Test
    void testVerifyCodeWrong() {
        when(redisUtil.get(any())).thenReturn("654321");

        boolean result = smsService.verifyCode(TEST_PHONE, "123456");

        assertFalse(result);
        verify(redisUtil, never()).delete(any());
    }

    @Test
    void testVerifyCodeExpired() {
        when(redisUtil.get(any())).thenReturn(null);

        boolean result = smsService.verifyCode(TEST_PHONE, "123456");

        assertFalse(result);
        verify(redisUtil, never()).delete(any());
    }
}
