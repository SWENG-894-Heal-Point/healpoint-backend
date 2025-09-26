package edu.psgv.healpointbackend.utilities;

import static edu.psgv.healpointbackend.HealpointBackendApplication.CONFIG_READER;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;


/**
 * Utility class for generating JSON Web Tokens (JWT) for user authentication.
 * Uses a secret key from the application configuration to sign tokens.
 *
 * @author Mahfuzur Rahman
 */
public class JwtUtil {
    private String secret = CONFIG_READER.get("jwtSecretKey");
    private long validityInMs = 3600_000;

    /**
     * Generates a JWT for the specified email.
     *
     * @param email the email address to include as the subject in the token
     * @return a signed JWT as a String
     */
    public String generateToken(String email) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
