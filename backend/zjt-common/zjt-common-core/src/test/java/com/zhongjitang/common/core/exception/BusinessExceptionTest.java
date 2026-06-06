package com.zhongjitang.common.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessException业务异常测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("使用code和message构造异常应正确赋值")
    void 使用code和message构造() {
        BusinessException exception = new BusinessException(40001, "参数错误");

        assertEquals(40001, exception.getCode());
        assertEquals("参数错误", exception.getMessage());
        assertNull(exception.getCause());
        assertNotNull(exception.toString());
    }

    @Test
    @DisplayName("使用ErrorCode枚举构造异常应正确赋值")
    void 使用ErrorCode枚举构造() {
        BusinessException exception = new BusinessException(ErrorCode.UNAUTHORIZED);

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getCode());
        assertEquals(ErrorCode.UNAUTHORIZED.getMessage(), exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("使用ErrorCode和detail构造异常应拼接消息")
    void 使用ErrorCode和detail构造() {
        BusinessException exception = new BusinessException(ErrorCode.STORE_NOT_FOUND, "门店ID: 999");

        assertEquals(ErrorCode.STORE_NOT_FOUND.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("门店不存在"));
        assertTrue(exception.getMessage().contains("门店ID: 999"));
        assertEquals(ErrorCode.STORE_NOT_FOUND.getMessage() + ": 门店ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("ErrorCode枚举值应包含正确的code和message")
    void errorCode枚举值校验() {
        assertEquals(0, ErrorCode.SUCCESS.getCode());
        assertEquals("success", ErrorCode.SUCCESS.getMessage());

        assertEquals(40001, ErrorCode.PARAM_ERROR.getCode());
        assertEquals("参数错误", ErrorCode.PARAM_ERROR.getMessage());

        assertEquals(40101, ErrorCode.UNAUTHORIZED.getCode());
        assertEquals("未认证，请先登录", ErrorCode.UNAUTHORIZED.getMessage());

        assertEquals(40401, ErrorCode.NOT_FOUND.getCode());
        assertEquals("资源不存在", ErrorCode.NOT_FOUND.getMessage());

        assertEquals(50001, ErrorCode.INTERNAL_ERROR.getCode());
        assertEquals("系统内部错误", ErrorCode.INTERNAL_ERROR.getMessage());
    }

    @Test
    @DisplayName("BusinessException应作为RuntimeException的子类")
    void 应为RuntimeException子类() {
        BusinessException exception = new BusinessException(40001, "测试");

        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertThrows(RuntimeException.class, () -> {
            throw exception;
        });
    }

    @Test
    @DisplayName("不同ErrorCode的code值应各不相同")
    void 不同ErrorCode的code值应不同() {
        assertNotEquals(ErrorCode.PARAM_ERROR.getCode(), ErrorCode.UNAUTHORIZED.getCode());
        assertNotEquals(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.FORBIDDEN.getCode());
        assertNotEquals(ErrorCode.NOT_FOUND.getCode(), ErrorCode.CONFLICT.getCode());
        assertNotEquals(ErrorCode.SUCCESS.getCode(), ErrorCode.INTERNAL_ERROR.getCode());
    }
}
