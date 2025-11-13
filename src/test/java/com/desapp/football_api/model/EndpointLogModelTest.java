package com.desapp.football_api.model;

import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
class EndpointLogModelTest {

    @Test
    void constructor_populatesFields_fromRequestWrapper_withoutAuth() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sample");
        request.setRemoteAddr("10.0.0.1");
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);

        EndpointLog log = new EndpointLog(wrapper, 123L, 201, null);

        assertEquals("/api/sample", log.getRequestPath());
        assertEquals("POST", log.getHttpMethod());
        assertEquals("10.0.0.1", log.getRequestIp());
        assertEquals("123ms", log.getResponseTime());
        assertEquals(201, log.getStatusCode());
        assertNotNull(log.getTimestamp());
    }
}
