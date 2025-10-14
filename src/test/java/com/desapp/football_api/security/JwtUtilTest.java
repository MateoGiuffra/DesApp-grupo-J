package com.desapp.football_api.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class JwtUtilTest {

    @Test
    void generateAndValidateToken_withTestSecret_works() {
        JwtUtil jwtUtil = new JwtUtil();
        // base64 for a 64-byte key (HS512 requires a long key). We'll use 64 'a' characters and base64-encode it.
        String secretBase64 = java.util.Base64.getEncoder().encodeToString("a".repeat(64).getBytes());
        ReflectionTestUtils.setField(jwtUtil, "secret", secretBase64);

        String token = jwtUtil.generateToken("john");
        assertNotNull(token);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void getUsername_returnsSubject() {
        JwtUtil jwtUtil = new JwtUtil();
        String secretBase64 = java.util.Base64.getEncoder().encodeToString("b".repeat(64).getBytes());
        ReflectionTestUtils.setField(jwtUtil, "secret", secretBase64);

        String token = jwtUtil.generateToken("alice");
        String username = jwtUtil.getUsername(token);
        assertEquals("alice", username);
    }

    @Test
    void validateToken_withWrongSecret_returnsFalse() {
        JwtUtil jwtUtil1 = new JwtUtil();
        JwtUtil jwtUtil2 = new JwtUtil();
        String s1 = java.util.Base64.getEncoder().encodeToString("x".repeat(64).getBytes());
        String s2 = java.util.Base64.getEncoder().encodeToString("y".repeat(64).getBytes());
        ReflectionTestUtils.setField(jwtUtil1, "secret", s1);
        ReflectionTestUtils.setField(jwtUtil2, "secret", s2);

        String token = jwtUtil1.generateToken("bob");
        assertFalse(jwtUtil2.validateToken(token));
    }
}
