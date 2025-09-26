package edu.psgv.healpointbackend.utilities;

import static edu.psgv.healpointbackend.HealpointBackendApplication.CONFIG_READER;


public class JwtUtil {
    private String secret = CONFIG_READER.get("jwtSecretKey");
    private long validityInMs = 3600_000;

    public String generateToken(String email) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
