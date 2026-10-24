package com.jayesh.resume_analyzer.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final String OTHER_SECRET = "another-secret-key-that-is-also-32-bytes-long!!";

    @Test
    void generatedTokenIsValidAndCarriesTheEmail() {
        JwtUtil jwt = new JwtUtil(SECRET, 60_000);

        String token = jwt.generateToken("a@example.com", "CANDIDATE");

        assertTrue(jwt.isValid(token));
        assertEquals("a@example.com", jwt.extractEmail(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtUtil jwt = new JwtUtil(SECRET, 60_000);
        String token = jwt.generateToken("a@example.com", "CANDIDATE");

        // change the first character of the signature part
        int lastDot = token.lastIndexOf('.');
        char original = token.charAt(lastDot + 1);
        char changed = original == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, lastDot + 1) + changed + token.substring(lastDot + 2);

        assertFalse(jwt.isValid(tampered));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtUtil jwt = new JwtUtil(SECRET, -1_000); // expiry already in the past

        String token = jwt.generateToken("a@example.com", "CANDIDATE");

        assertFalse(jwt.isValid(token));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtUtil issuer = new JwtUtil(SECRET, 60_000);
        JwtUtil verifier = new JwtUtil(OTHER_SECRET, 60_000);

        String token = issuer.generateToken("a@example.com", "CANDIDATE");

        assertFalse(verifier.isValid(token));
    }

    @Test
    void garbageAndEmptyTokensAreRejected() {
        JwtUtil jwt = new JwtUtil(SECRET, 60_000);

        assertFalse(jwt.isValid("not-a-token"));
        assertFalse(jwt.isValid(""));
    }
}
