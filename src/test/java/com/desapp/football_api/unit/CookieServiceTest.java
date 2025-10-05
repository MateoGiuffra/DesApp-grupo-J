package com.desapp.football_api.unit;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.generic.UnauthorizedException;
import com.desapp.football_api.service.CookieService;
import com.desapp.football_api.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CookieServiceTest {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    CookieService cookieService;

    @Test
    void createCookieToResponse_addsHttpOnlyCookie_withGeneratedJwt() {
        when(jwtUtil.generateToken("john"))
                .thenReturn("jwt-token-123");

        // Force secureCookie=false to have deterministic assertion
        ReflectionTestUtils.setField(cookieService, "secureCookie", false);

        cookieService.createCookieToResponse(response, "john");

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        Cookie cookie = captor.getValue();
        assertEquals("jwt", cookie.getName());
        assertEquals("jwt-token-123", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
        assertEquals("/", cookie.getPath());
    }

    @Test
    void clearCookieFromResponse_addsExpiredCookie() {
        ReflectionTestUtils.setField(cookieService, "secureCookie", false);

        cookieService.clearCookieFromResponse(response);

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        Cookie cookie = captor.getValue();
        assertEquals("jwt", cookie.getName());
        assertNull(cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    void validateTokenAlreadyLogged_validToken_throwsBadRequest() {
        when(jwtUtil.validateToken("abc")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> cookieService.validateTokenAlreadyLogged("abc"));
    }

    @Test
    void validateTokenAlreadyLogged_nullOrInvalid_doesNotThrow() {
        assertDoesNotThrow(() -> cookieService.validateTokenAlreadyLogged(null));
        when(jwtUtil.validateToken("abc")).thenReturn(false);
        assertDoesNotThrow(() -> cookieService.validateTokenAlreadyLogged("abc"));
    }

    @Test
    void validateToken_invalidNonNull_throwsUnauthorized() {
        when(jwtUtil.validateToken("bad")).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> cookieService.validateToken("bad"));
    }

    @Test
    void validateToken_nullOrValid_doesNotThrow() {
        assertDoesNotThrow(() -> cookieService.validateToken(null));
        when(jwtUtil.validateToken("ok")).thenReturn(true);
        assertDoesNotThrow(() -> cookieService.validateToken("ok"));
    }
}
