package com.zhongjitang.common.log.aspect;

import com.zhongjitang.common.core.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求日志切面
 * <p>
 * 拦截所有Controller方法，记录请求方法、路径、参数、响应码、耗时和操作人。
 * </p>
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 环绕通知：拦截所有Controller方法
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = "";
        String uri = "";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            method = request.getMethod();
            uri = request.getRequestURI();
        }

        // 获取操作人
        String operator = UserContext.getUsername();
        if (operator == null) {
            operator = "anonymous";
        }

        Object result;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[API] {} {} | operator={} | elapsed={}ms",
                    method, uri, operator, elapsed);
        }
    }
}
