package com.zhongjitang.system.aspect;

import com.zhongjitang.common.core.context.UserContext;
import com.zhongjitang.system.annotation.AuditLog;
import com.zhongjitang.system.domain.dto.AuditLogCreateRequest;
import com.zhongjitang.system.service.IAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final IAuditLogService auditLogService;

    @Around("@annotation(com.zhongjitang.system.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLogAnnotation = method.getAnnotation(AuditLog.class);

        String operation = auditLogAnnotation.value();
        String module = auditLogAnnotation.module();
        String httpMethod = "";
        String ip = "";
        String userAgent = "";

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            httpMethod = request.getMethod();
            ip = getClientIp(request);
            userAgent = request.getHeader("User-Agent");
        }

        Long userId = UserContext.getUserId();
        String userName = UserContext.getUsername();

        Object result;
        Integer status = 0;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = 1;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            try {
                AuditLogCreateRequest request = new AuditLogCreateRequest();
                request.setUserId(userId);
                request.setUserName(userName);
                request.setOperation(operation);
                request.setMethod(httpMethod);
                request.setModule(module);
                request.setIp(ip);
                request.setUserAgent(userAgent);
                request.setDuration(duration);
                request.setStatus(status);
                auditLogService.log(request);
            } catch (Exception e) {
                log.error("记录审计日志失败", e);
            }
        }
        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
