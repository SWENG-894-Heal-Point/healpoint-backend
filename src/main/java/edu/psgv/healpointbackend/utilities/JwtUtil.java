package edu.psgv.healpointbackend.utilities;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import static edu.psgv.healpointbackend.HealpointBackendApplication.CONFIG_READER;


/**
 * Utility class for generating JSON Web Tokens (JWT) for user authentication.
 * Uses a secret key from the application configuration to sign tokens.
 *
 * @author Mahfuzur Rahman
 */
@Component
public class JwtUtil {

    /**
     * Generates a JWT for the specified email.
     *
     * @param email the email address to include as the subject in the token
     * @return a signed JWT as a String
     */
    public String generateToken(String email, String role) {
        String secret = CONFIG_READER.get("jwtSecretKey");
        long validityInMs = 3600_000;

        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }
}
