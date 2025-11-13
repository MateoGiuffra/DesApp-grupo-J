package com.desapp.football_api.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Tag("unit")
class ServiceCachingAspectTest {

    static class DummyService {
        public int sum(int a, int b) { return a + b; }
    }

    @Test
    void cacheAround_cachesResultOnSecondInvocation() throws Throwable {
        // Arrange
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        ServiceCachingAspect aspect = new ServiceCachingAspect(cacheManager);

        DummyService target = new DummyService();
        Method method = DummyService.class.getMethod("sum", int.class, int.class);

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getReturnType()).thenReturn(int.class);
        when(signature.getDeclaringType()).thenReturn(DummyService.class);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{2, 3});
        when(pjp.proceed()).thenReturn(5);

        // Act first call: should compute and cache
        Object first = aspect.cacheAround(pjp);
        assertEquals(5, first);
        // Configure proceed to throw if called again to ensure cache is used
        reset(pjp);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{2, 3});
        when(pjp.proceed()).thenThrow(new AssertionError("Proceed should not be called on cache hit"));

        // Act second call: should hit cache and not call proceed
        Object second = aspect.cacheAround(pjp);
        assertEquals(5, second);
    }
}
