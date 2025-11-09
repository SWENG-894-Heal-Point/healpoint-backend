package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


/**
 * Data seeder for loading patients into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute patient loading logic
 * when the application starts in the "test" profile. It reads patient data
 * from a JSON file and saves it to the database using the PatientRepository.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(30)
public class PatientDataSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final PatientRepository patientRepository;

    /**
     * Constructs a new PatientDataSeeder with the specified ObjectMapper and PatientRepository.
     *
     * @param objectMapper      the ObjectMapper for JSON processing
     * @param patientRepository the repository for Patient entities
     */
    public PatientDataSeeder(ObjectMapper objectMapper, PatientRepository patientRepository) {
        this.objectMapper = objectMapper;
        this.patientRepository = patientRepository;
    }

    /**
     * Runs the patient data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during patient loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load patients from JSON file and save to database
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Patients.json").getInputStream());
        for (JsonNode node : root) {
            Patient patient = Patient.builder()
                    .id(node.get("PatientID").asInt())
                    .firstName(node.get("FirstName").asText())
                    .lastName(node.get("LastName").asText())
                    .dateOfBirth(LocalDate.parse(node.get("DateOfBirth").asText()))
                    .gender(node.get("Gender").asText())
                    .phone(node.get("Phone").asText())
                    .streetAddress(node.get("StreetAddress").asText())
                    .city(node.get("City").asText())
                    .state(node.get("State").asText())
                    .zipCode(node.get("ZipCode").asText())
                    .insuranceId(node.get("InsuranceID").asText())
                    .insuranceProvider(node.get("InsuranceProvider").asText())
                    .build();
            patientRepository.save(patient);
        }
    }
}
