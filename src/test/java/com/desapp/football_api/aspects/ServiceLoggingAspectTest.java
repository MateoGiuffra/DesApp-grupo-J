package com.desapp.football_api.aspects;

import com.desapp.football_api.model.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Tag("unit")
class ServiceLoggingAspectTest {

    private ServiceLoggingAspect serviceLoggingAspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceLoggingAspect = new ServiceLoggingAspect();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logServiceAccess_shouldLogMethodExecution() throws Throwable {
        // Given
        User user = new User();
        user.setId(1L);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        when(proceedingJoinPoint.proceed()).thenReturn("testResult");

        // When
        Object result = serviceLoggingAspect.logEndpointAccess(proceedingJoinPoint);

        // Then
        assertEquals("testResult", result);
    }
}
