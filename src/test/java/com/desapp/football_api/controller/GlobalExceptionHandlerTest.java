package com.desapp.football_api.controller;

import com.desapp.football_api.controller.handler.ErrorResponse;
import com.desapp.football_api.controller.handler.GlobalExceptionHandler;
import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.generic.NotFoundException;
import com.desapp.football_api.exceptions.generic.UnauthorizedException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returns404() {
        ResponseEntity<ErrorResponse> resp = handler.handleResourceNotFound(new NotFoundException("x"));
        assertEquals(404, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
    }

    @Test
    void handleBadRequest_returns400() {
        ResponseEntity<ErrorResponse> resp = handler.handleBadRequest(new BadRequestException("bad"));
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void handleUnauthorized_returns401() {
        ResponseEntity<ErrorResponse> resp = handler.handleBadRequest(new UnauthorizedException());
        assertEquals(401, resp.getStatusCode().value());
    }

    @Test
    void handleGenericException_returns500() {
        ResponseEntity<ErrorResponse> resp = handler.handleGenericException(new RuntimeException("boom"), null);
        assertEquals(500, resp.getStatusCode().value());
    }
}
