package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.dto.AuthenticationFormDto;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.UserRepository;
import edu.psgv.healpointbackend.utilities.JwtUtil;
import edu.psgv.healpointbackend.utilities.PasswordUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service class responsible for handling user authentication and logout operations.
 * Integrates with the user repository, JWT utilities, and manages online user state.
 *
 * @author Mahfuzur Rahman
 */
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Datastore datastore;

    /**
     * Constructs the AuthenticationService with the required UserRepository.
     * Initializes JwtUtil and Datastore instances.
     *
     * @param userRepository the user repository
     * @param jwtUtil        the JWT utility
     * @param datastore      the datastore instance
     */
    public AuthenticationService(UserRepository userRepository, JwtUtil jwtUtil, Datastore datastore) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.datastore = datastore;
    }

    /**
     * Authenticates a user based on the provided authentication form data.
     * Validates credentials, checks account status, and issues a JWT token if successful.
     * If the user is already online, returns the existing token.
     *
     * @param authenticationFormDto the authentication form data
     * @return a ResponseEntity containing the JWT token or an error message
     */
    public ResponseEntity<String> authenticateUser(AuthenticationFormDto authenticationFormDto) {
        String email = authenticationFormDto.getEmail();
        LOGGER.info("Authentication attempt for email: {}", email);
        try {
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
            if (userOpt.isEmpty()) {
                LOGGER.warn("Authentication failed: no account found for email {}", email);
                return ResponseEntity.status(401).body("No account associated with this email address.");
            }

            User user = userOpt.get();
            if (Boolean.FALSE.equals(user.getIsActive())) {
                LOGGER.warn("Authentication failed: inactive account for email {}", email);
                return ResponseEntity.status(403).body("This account is inactive. Please contact support.");
            }

            boolean passwordMatch = PasswordUtils.verifyPassword(authenticationFormDto.getPassword(), user.getPassword());
            if (!passwordMatch) {
                LOGGER.warn("Authentication failed: incorrect password for email {}", email);
                return ResponseEntity.status(401).body("Incorrect password. Please try again.");
            }

            User existingUser = datastore.getUserByEmail(user.getEmail());
            if (existingUser != null) {
                LOGGER.info("User {} already authenticated. Returning existing token.", email);
                return ResponseEntity.ok(existingUser.getToken());
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getDescription());
            user.setToken(token);
            datastore.addUser(user);

            LOGGER.info("Authentication successful for email {}. Token issued.", email);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during authentication for {}: {}", authenticationFormDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /**
     * Logs out a user by removing them from the online users datastore using their token.
     *
     * @param token the JWT token of the user to log out
     */
    public void logoutUser(String token) {
        LOGGER.info("Attempting to log out user");

        User user = datastore.getUserByToken(token);
        if (user != null) {
            datastore.removeUser(user);
            LOGGER.info("User {} successfully logged out.", user.getEmail());
        } else {
            LOGGER.warn("Logout attempt failed: no user found with the provided token.");
        }
    }
}
