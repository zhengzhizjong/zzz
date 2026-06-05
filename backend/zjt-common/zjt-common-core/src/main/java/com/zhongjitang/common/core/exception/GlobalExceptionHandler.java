package com.zhongjitang.common.core.exception;

import com.zhongjitang.common.core.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一拦截并处理所有Controller层抛出的异常，
 * 返回标准化的错误响应体R，便于前端统一处理。
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 统一响应体
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleBusinessException(BusinessException e) {
        String requestId = generateRequestId();
        log.warn("业务异常 [requestId={}]: code={}, message={}", requestId, e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage()).requestId(requestId);
    }

    /**
     * 处理参数校验异常（@Valid/@Validated 触发）
     *
     * @param e 参数校验异常
     * @return 统一响应体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String requestId = generateRequestId();
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常 [requestId={}]: {}", requestId, message);
        return R.fail(40001, message).requestId(requestId);
    }

    /**
     * 处理约束违反异常（@Validated 在方法参数上触发）
     *
     * @param e 约束违反异常
     * @return 统一响应体
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String requestId = generateRequestId();
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 [requestId={}]: {}", requestId, message);
        return R.fail(40001, "参数校验失败: " + message).requestId(requestId);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e 缺少请求参数异常
     * @return 统一响应体
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String requestId = generateRequestId();
        String message = "缺少必填参数: " + e.getParameterName();
        log.warn("缺少必填参数 [requestId={}]: {}", requestId, e.getParameterName());
        return R.fail(40002, message).requestId(requestId);
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e 请求方法不支持异常
     * @return 统一响应体
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String requestId = generateRequestId();
        String message = "请求方法不支持: " + e.getMethod();
        log.warn("请求方法不支持 [requestId={}]: {}", requestId, e.getMethod());
        return R.fail(40003, message).requestId(requestId);
    }

    /**
     * 处理所有未捕获的异常
     *
     * @param e 未知异常
     * @return 统一响应体
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleException(Exception e) {
        String requestId = generateRequestId();
        log.error("系统内部错误 [requestId={}]", requestId, e);
        return R.fail(50001, "系统内部错误").requestId(requestId);
    }

    /**
     * 生成请求ID
     *
     * @return UUID格式的请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
