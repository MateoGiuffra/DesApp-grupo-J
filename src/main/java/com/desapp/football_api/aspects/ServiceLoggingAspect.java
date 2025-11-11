package com.desapp.football_api.aspects;

import com.desapp.football_api.model.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Pointcut("execution(* com.desapp.football_api.services.impl.*.*(..))")
    public void serviceLoggingPointcut() {
    }

    @Around("serviceLoggingPointcut()")
    public Object logEndpointAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        long responseTime = end - start;

        Signature signature = joinPoint.getSignature();
        String methodName = signature.getName();

        String params = Arrays.toString(joinPoint.getArgs());

        String userId = "Anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            User user = (User) auth.getPrincipal();
            userId = "ID:" + user.getId();
        }

        log.info("User: {} | Method: {}({}) | Execution Time: {}ms", userId, methodName, params, responseTime);

        return result;
    }
}
