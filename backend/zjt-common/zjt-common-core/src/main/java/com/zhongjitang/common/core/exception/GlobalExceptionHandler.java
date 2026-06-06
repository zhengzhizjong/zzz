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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleBusinessException(BusinessException e) {
        String requestId = generateRequestId();
        log.warn("业务异常 [requestId={}]: code={}, message={}", requestId, e.getCode(), e.getMessage());
        R<Void> r = R.fail(e.getCode(), e.getMessage());
        r.setRequestId(requestId);
        return r;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String requestId = generateRequestId();
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常 [requestId={}]: {}", requestId, message);
        R<Void> r = R.fail(40001, message);
        r.setRequestId(requestId);
        return r;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String requestId = generateRequestId();
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 [requestId={}]: {}", requestId, message);
        R<Void> r = R.fail(40001, "参数校验失败: " + message);
        r.setRequestId(requestId);
        return r;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String requestId = generateRequestId();
        String message = "缺少必填参数: " + e.getParameterName();
        log.warn("缺少必填参数 [requestId={}]: {}", requestId, e.getParameterName());
        R<Void> r = R.fail(40002, message);
        r.setRequestId(requestId);
        return r;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String requestId = generateRequestId();
        String message = "请求方法不支持: " + e.getMethod();
        log.warn("请求方法不支持 [requestId={}]: {}", requestId, e.getMethod());
        R<Void> r = R.fail(40003, message);
        r.setRequestId(requestId);
        return r;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleException(Exception e) {
        String requestId = generateRequestId();
        log.error("系统内部错误 [requestId={}]", requestId, e);
        R<Void> r = R.fail(50001, "系统内部错误");
        r.setRequestId(requestId);
        return r;
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
