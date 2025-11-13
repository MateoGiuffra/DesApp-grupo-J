package com.desapp.football_api.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Tag("unit")
class ServiceCachingAspectMoreTest {

    static class VoidService {
        public void doWork(String x) {}
        public String mayReturnNull(String x) { return null; }
    }

    @Test
    void cacheAround_cachesNullResults() throws Throwable {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        ServiceCachingAspect aspect = new ServiceCachingAspect(cacheManager);

        Method method = VoidService.class.getMethod("mayReturnNull", String.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getReturnType()).thenReturn(String.class);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{"k"});
        when(pjp.proceed()).thenReturn(null);

        Object first = aspect.cacheAround(pjp);
        assertEquals(null, first);

        // Second invocation should return cached null without proceeding
        reset(pjp);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{"k"});
        when(pjp.proceed()).thenThrow(new AssertionError("Proceed should not be called on cache hit for null"));

        Object second = aspect.cacheAround(pjp);
        assertEquals(null, second);
    }

    @Test
    void cacheAround_doesNotCacheVoidReturningMethods() throws Throwable {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        ServiceCachingAspect aspect = new ServiceCachingAspect(cacheManager);

        Method method = VoidService.class.getMethod("doWork", String.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getReturnType()).thenReturn(Void.TYPE);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{"x"});

        // Should simply proceed and not throw
        aspect.cacheAround(pjp);
        verify(pjp, times(1)).proceed();
    }
}
