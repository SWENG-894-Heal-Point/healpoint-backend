package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.Role;
import edu.psgv.healpointbackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Data seeder for loading roles into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute role loading logic
 * when the application starts in the "test" profile. It reads role data
 * from a JSON file and saves it to the database using the RoleRepository.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(10)
public class RoleDataSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final RoleRepository roleRepository;

    /**
     * Constructs a new RoleDataSeeder with the specified ObjectMapper and RoleRepository.
     *
     * @param objectMapper   the ObjectMapper for JSON processing
     * @param roleRepository the repository for Role entities
     */
    public RoleDataSeeder(ObjectMapper objectMapper, RoleRepository roleRepository) {
        this.objectMapper = objectMapper;
        this.roleRepository = roleRepository;
    }

    /**
     * Runs the role data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during role loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load roles from JSON file and save to database
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Roles.json").getInputStream());
        for (JsonNode node : root) {
            Role role = new Role();
            role.setDescription(node.get("RoleDescription").asText());
            roleRepository.save(role);
        }

        // Log all loaded roles
        LOGGER.info("*** Loaded Roles ***");
        roleRepository.findAll().forEach(role -> LOGGER.info("{} Role: {}", role.getId(), role.getDescription()));
    }
}
