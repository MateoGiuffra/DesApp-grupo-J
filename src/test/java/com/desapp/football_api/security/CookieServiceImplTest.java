package com.desapp.football_api.security;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.generic.UnauthorizedException;
import com.desapp.football_api.services.impl.CookieServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CookieServiceImplTest {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    CookieServiceImpl cookieServiceImpl;

    @Test
    void createCookieToResponse_addsHttpOnlyCookie_withGeneratedJwt() {
        when(jwtUtil.generateToken("john"))
                .thenReturn("jwt-token-123");

        // Force secureCookie=false to have deterministic assertion
        ReflectionTestUtils.setField(cookieServiceImpl, "secureCookie", false);

        cookieServiceImpl.createCookieToResponse(response, "john");

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
        ReflectionTestUtils.setField(cookieServiceImpl, "secureCookie", false);

        cookieServiceImpl.clearCookieFromResponse(response);

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
        assertThrows(BadRequestException.class, () -> cookieServiceImpl.validateTokenAlreadyLogged("abc"));
    }

    @Test
    void validateTokenAlreadyLogged_nullOrInvalid_doesNotThrow() {
        assertDoesNotThrow(() -> cookieServiceImpl.validateTokenAlreadyLogged(null));
        when(jwtUtil.validateToken("abc")).thenReturn(false);
        assertDoesNotThrow(() -> cookieServiceImpl.validateTokenAlreadyLogged("abc"));
    }

    @Test
    void validateToken_invalidNonNull_throwsUnauthorized() {
        when(jwtUtil.validateToken("bad")).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> cookieServiceImpl.validateToken("bad"));
    }

    @Test
    void validateToken_nullOrValid_doesNotThrow() {
        assertDoesNotThrow(() -> cookieServiceImpl.validateToken(null));
        when(jwtUtil.validateToken("ok")).thenReturn(true);
        assertDoesNotThrow(() -> cookieServiceImpl.validateToken("ok"));
    }
}
