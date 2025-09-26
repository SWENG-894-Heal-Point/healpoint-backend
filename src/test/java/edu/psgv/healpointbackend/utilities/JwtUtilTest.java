package edu.psgv.healpointbackend.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Date;


class JwtUtilTest {
    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "testSuperStrongSecretKey1234567890";
    private static final long TEST_VALIDITY = 3600_000L;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();

        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, TEST_SECRET);

        Field validityField = JwtUtil.class.getDeclaredField("validityInMs");
        validityField.setAccessible(true);
        validityField.setLong(jwtUtil, TEST_VALIDITY);
    }

    @Test
    void generateToken_shouldContainEmailAndValidTimestamps()  throws InterruptedException {
        String email = "user@example.com";

        // mark time window
        Date before = new Date();
        Thread.sleep(1000);
        String token = jwtUtil.generateToken(email);
        Date after = new Date();

        // parse back
        Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Jws<Claims> parsed = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);

        Claims body = parsed.getBody();
        Date issuedAt = body.getIssuedAt();
        Date expiration = body.getExpiration();

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(email, body.getSubject());
        assertTrue((issuedAt.compareTo(before) >= 0 && issuedAt.compareTo(after) <= 0),"issuedAt should be stamped between before and after generation");

        long delta = expiration.getTime() - issuedAt.getTime();
        assertEquals(TEST_VALIDITY, delta, 100, "expiration minus issuedAt should be within 100ms of validityInMs");
    }

    @Test
    void generateToken_differentEmails_returnsDifferentTokens() {
        String a = "a@example.com";
        String b = "b@example.com";

        String tA = jwtUtil.generateToken(a);
        String tB = jwtUtil.generateToken(b);

        assertNotEquals(tA, tB, "Tokens for different subjects must differ");
    }
}