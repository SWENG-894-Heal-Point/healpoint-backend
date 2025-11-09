package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.Role;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.RoleRepository;
import edu.psgv.healpointbackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Data seeder for loading users into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute user loading logic
 * when the application starts in the "test" profile. It reads user data
 * from a JSON file and saves it to the database using the UserRepository.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(20)
public class UserDataSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new UserDataSeeder with the specified ObjectMapper, RoleRepository, and UserRepository.
     *
     * @param objectMapper   the ObjectMapper for JSON processing
     * @param roleRepository the repository for Role entities
     * @param userRepository the repository for User entities
     */
    public UserDataSeeder(ObjectMapper objectMapper, RoleRepository roleRepository, UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Runs the user data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during user loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load users from JSON file and save to database
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Users.json").getInputStream());
        for (JsonNode node : root) {
            Role role = roleRepository.findById(node.get("RoleID").asInt()).orElseThrow(() -> new IllegalArgumentException("Invalid role."));
            User newUser = new User(node.get("Email").asText(), node.get("Password").asText(), role);
            userRepository.save(newUser);
        }

        // Log all loaded users
        LOGGER.info("*** Loaded Users ***");
        userRepository.findAll().forEach(user -> LOGGER.info("{} User: {}, Role: {}", user.getId(), user.getEmail(), user.getRole().getDescription()));
    }
}
