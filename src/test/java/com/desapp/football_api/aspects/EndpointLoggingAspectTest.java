package com.desapp.football_api.aspects;

import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.services.impl.EndpointLogServiceImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
class EndpointLoggingAspectTest {

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void logEndpointAccess_persistsEndpointLogAndReturnsResult() throws Throwable {
        // Arrange request/response context
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        ServletRequestAttributes attrs = new ServletRequestAttributes(request, response);
        RequestContextHolder.setRequestAttributes(attrs);

        // Mock dependencies
        EndpointLogServiceImpl endpointLogService = mock(EndpointLogServiceImpl.class);
        when(endpointLogService.save(any(EndpointLog.class))).thenAnswer(inv -> inv.getArgument(0));

        EndpointLoggingAspect aspect = new EndpointLoggingAspect(endpointLogService);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("OK");

        // Act
        Object result = aspect.logEndpointAccess(pjp);

        // Assert
        assertEquals("OK", result);
        ArgumentCaptor<EndpointLog> captor = ArgumentCaptor.forClass(EndpointLog.class);
        verify(endpointLogService, times(1)).save(captor.capture());
        EndpointLog saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("/api/test", saved.getRequestPath());
        assertEquals("GET", saved.getHttpMethod());
        assertEquals(200, saved.getStatusCode());
    }
}
