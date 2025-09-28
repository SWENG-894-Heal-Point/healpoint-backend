package edu.psgv.healpointbackend.utilities;

import static edu.psgv.healpointbackend.HealpointBackendApplication.CONFIG_READER;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.Key;
import java.util.Date;


class JwtUtilTest {
    private JwtUtil jwtUtil;
    private static String testSecret;
    private static long testValidity;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        testSecret = CONFIG_READER.get("jwtSecretKey");
        testValidity = 3600_000L;
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
        Key key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Jws<Claims> parsed = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

        Claims body = parsed.getBody();
        Date issuedAt = body.getIssuedAt();
        Date expiration = body.getExpiration();

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(email, body.getSubject());
        assertTrue((issuedAt.compareTo(before) >= 0 && issuedAt.compareTo(after) <= 0),"issuedAt should be stamped between before and after generation");

        long delta = expiration.getTime() - issuedAt.getTime();
        assertEquals(testValidity, delta, 100, "expiration minus issuedAt should be within 100ms of validityInMs");
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