package com.zhongjitang.common.core.exception;

import com.zhongjitang.common.core.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器测试
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("处理BusinessException - 应返回对应错误码和消息")
    void handleBusinessException() {
        BusinessException e = new BusinessException(40401, "资源不存在");
        R<Void> result = handler.handleBusinessException(e);

        assertEquals(40401, result.getCode());
        assertEquals("资源不存在", result.getMessage());
        assertNotNull(result.getRequestId());
    }

    @Test
    @DisplayName("处理BusinessException - 使用ErrorCode枚举")
    void handleBusinessExceptionWithErrorCode() {
        BusinessException e = new BusinessException(ErrorCode.UNAUTHORIZED);
        R<Void> result = handler.handleBusinessException(e);

        assertEquals(40101, result.getCode());
        assertEquals("未认证，请先登录", result.getMessage());
        assertNotNull(result.getRequestId());
    }

    @Test
    @DisplayName("处理MethodArgumentNotValidException - 应返回40001和字段错误信息")
    void handleMethodArgumentNotValidException() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "phone", "手机号格式不正确"));
        bindingResult.addError(new FieldError("request", "name", "姓名不能为空"));

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(null, bindingResult);
        R<Void> result = handler.handleMethodArgumentNotValidException(e);

        assertEquals(40001, result.getCode());
        assertTrue(result.getMessage().contains("手机号格式不正确"));
        assertTrue(result.getMessage().contains("姓名不能为空"));
        assertNotNull(result.getRequestId());
    }

    @Test
    @DisplayName("处理ConstraintViolationException - 应返回40001和参数校验失败信息")
    void handleConstraintViolationException() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = createMockConstraintViolation("手机号格式不正确");
        violations.add(violation);

        ConstraintViolationException e = new ConstraintViolationException("参数校验失败", violations);
        R<Void> result = handler.handleConstraintViolationException(e);

        assertEquals(40001, result.getCode());
        assertTrue(result.getMessage().contains("参数校验失败"));
        assertNotNull(result.getRequestId());
    }

    @Test
    @DisplayName("处理MissingServletRequestParameterException - 应返回40002")
    void handleMissingServletRequestParameterException() {
        MissingServletRequestParameterException e =
                new MissingServletRequestParameterException("phone", "String");
        R<Void> result = handler.handleMissingServletRequestParameterException(e);

        assertEquals(40002, result.getCode());
        assertTrue(result.getMessage().contains("phone"));
        assertNotNull(result.getRequestId());
    }

    @Test
    @DisplayName("处理HttpRequestMethodNotSupportedException - 应返回40003")
    void handleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException e =
                new HttpRequestMethodNotSupportedException("DELETE", new String[]{"GET", "POST"});
        R<Void> result = handler.handleHttpRequestMethodNotSupportedException(e);

        assertEquals(40003, result.getCode());
        assertTrue(result.getMessage().contains("DELETE"));
        assertNotNull(result.getRequestId());
    }

    @Test
    @DisplayName("处理未知Exception - 应返回50001")
    void handleException() {
        Exception e = new RuntimeException("未知错误");
        R<Void> result = handler.handleException(e);

        assertEquals(50001, result.getCode());
        assertEquals("系统内部错误", result.getMessage());
        assertNotNull(result.getRequestId());
    }

    @Test
    @DisplayName("每个异常处理应生成不同的requestId")
    void differentRequestId() {
        BusinessException e1 = new BusinessException(40001, "错误1");
        BusinessException e2 = new BusinessException(40001, "错误2");

        R<Void> r1 = handler.handleBusinessException(e1);
        R<Void> r2 = handler.handleBusinessException(e2);

        assertNotNull(r1.getRequestId());
        assertNotNull(r2.getRequestId());
        assertNotEquals(r1.getRequestId(), r2.getRequestId());
    }

    /**
     * 创建模拟的ConstraintViolation
     */
    @SuppressWarnings("unchecked")
    private ConstraintViolation<?> createMockConstraintViolation(String message) {
        ConstraintViolation<?> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        org.mockito.Mockito.when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}
