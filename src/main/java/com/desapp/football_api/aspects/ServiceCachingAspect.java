package com.desapp.football_api.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Centralized caching aspect for service layer methods.
 * <p>
 * Includes all public methods under com.desapp.football_api.service package
 * and excludes those annotated with @NonCacheable.
 */
@Aspect
@Component
public class ServiceCachingAspect {

    private final CacheManager cacheManager;
    private static final String DEFAULT_CACHE_NAME = "serviceCache";
    private static final Logger logger = LoggerFactory.getLogger(ServiceCachingAspect.class);
    public ServiceCachingAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Around advice applying caching to service methods except those with @NonCacheable.
     * Pointcut explanation:
     * - execution(public * com.desapp.football_api.service..*(..)) -> matches any public method in the service package and subpackages.
     * - && !@annotation(com.desapp.football_api.aspects.NonCacheable) -> excludes methods explicitly marked as non-cacheable.
     */
    @Around("execution(public * com.desapp.football_api.service..*(..)) " +
            "&& !@annotation(com.desapp.football_api.aspects.NonCacheable) " +
            "&& !@within(com.desapp.football_api.aspects.NonCacheable)")
    public Object cacheAround(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        // Do not attempt to cache void-returning methods
        if (signature.getReturnType() == Void.TYPE) {
            return pjp.proceed();
        }

        Cache cache = resolveCache();
        if (cache == null) {
            // If no cache is available, just proceed
            return pjp.proceed();
        }

        logger.info("Caching aspect triggered for method: {}", pjp.getSignature());

        Object key = buildKey(method, pjp.getArgs());

        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper != null) {
            return wrapper.get();
        }

        Object result = pjp.proceed();
        // Cache null results as well to avoid repeated calls for absent data
        cache.put(key, result);
        return result;
    }

    private Cache resolveCache() {
        return cacheManager.getCache(DEFAULT_CACHE_NAME);
    }

    private Object buildKey(Method method, Object[] args) {
        String argsKey = Arrays.deepToString(args);
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();
        return className + "#" + methodName + "(" + argsKey + ")";
    }
}
