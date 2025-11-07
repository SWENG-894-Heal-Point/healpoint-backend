package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.EmployeeAccount;
import edu.psgv.healpointbackend.repository.EmployeeAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Data seeder for loading employee accounts into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute employee account loading logic
 * when the application starts in the "test" profile. It reads employee account data
 * from a JSON file and saves it to the database using the EmployeeAccountRepository.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(15)
public class EmployeeAccountDataSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final EmployeeAccountRepository employeeAccountRepository;

    /**
     * Constructs a new EmployeeAccountDataSeeder with the specified ObjectMapper and EmployeeAccountRepository.
     *
     * @param objectMapper              the ObjectMapper for JSON processing
     * @param employeeAccountRepository the repository for EmployeeAccount entities
     */
    public EmployeeAccountDataSeeder(ObjectMapper objectMapper, EmployeeAccountRepository employeeAccountRepository) {
        this.objectMapper = objectMapper;
        this.employeeAccountRepository = employeeAccountRepository;
    }

    /**
     * Runs the employee account data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during employee account loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load employee accounts from JSON file and save to database
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/EmployeeAccounts.json").getInputStream());
        for (JsonNode node : root) {
            EmployeeAccount employeeAccount = new EmployeeAccount(node.get("Email").asText(), node.get("UserID").asInt());
            employeeAccountRepository.save(employeeAccount);
        }

        // Log all loaded employee accounts
        LOGGER.info("*** Loaded Employee Accounts ***");
        employeeAccountRepository.findAll().forEach(account -> LOGGER.info("{} Employee Email: {}", account.getId(), account.getEmail()));
    }
}
