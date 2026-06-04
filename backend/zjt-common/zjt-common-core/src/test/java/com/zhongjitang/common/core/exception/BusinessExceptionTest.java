package com.zhongjitang.common.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessException 业务异常测试
 */
class BusinessExceptionTest {

    @Test
    void testCreateWithCodeAndMessage() {
        BusinessException ex = new BusinessException(40001, "参数错误");
        assertEquals(40001, ex.getCode());
        assertEquals("参数错误", ex.getMessage());
    }

    @Test
    void testCreateWithErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.PARAM_ERROR);
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        assertEquals(ErrorCode.PARAM_ERROR.getMessage(), ex.getMessage());
    }

    @Test
    void testCreateWithErrorCodeAndDetail() {
        BusinessException ex = new BusinessException(ErrorCode.PARAM_ERROR, "手机号格式不正确");
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("手机号格式不正确"));
    }

    @Test
    void testIsRuntimeException() {
        BusinessException ex = new BusinessException(ErrorCode.UNAUTHORIZED);
        assertTrue(ex instanceof RuntimeException);
    }
}
