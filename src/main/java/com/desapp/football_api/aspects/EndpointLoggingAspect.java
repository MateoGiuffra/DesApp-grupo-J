package com.desapp.football_api.aspects;

import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.service.EndpointLogService;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@AllArgsConstructor
public class EndpointLoggingAspect {
    private final EndpointLogService endpointLogService;
    private static final Logger logger = LoggerFactory.getLogger(EndpointLoggingAspect.class);

    @Pointcut("execution(* com.desapp.football_api.controller.web_services.*.*(..))")
    public void endpointLoggingPointcut() {
    }

    @Around("endpointLoggingPointcut()")
    public Object logEndpointAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return result;

        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(attrs.getRequest());
        Integer statusCode = attrs.getResponse() != null ? attrs.getResponse().getStatus() : 0;
        long responseTime = end - start;

        EndpointLog endpointLog = new EndpointLog(requestWrapper, responseTime, statusCode, auth);
        try {
            endpointLogService.save(endpointLog);
        } catch (Exception e) {
            logger.warn("Failed to persist endpoint log: {}", e.getMessage());
        }
        logger.info(endpointLog.toString());

        return result;
    }

}
