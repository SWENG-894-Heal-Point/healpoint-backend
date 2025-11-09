package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.Doctor;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


/**
 * Data seeder for loading doctors into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute doctor loading logic
 * when the application starts in the "test" profile. It reads doctor data
 * from a JSON file and saves it to the database using the DoctorRepository.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(25)
public class DoctorDataSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final DoctorRepository doctorRepository;

    /**
     * Constructs a new DoctorDataSeeder with the specified ObjectMapper and DoctorRepository.
     *
     * @param objectMapper     the ObjectMapper for JSON processing
     * @param doctorRepository the repository for Doctor entities
     */
    public DoctorDataSeeder(ObjectMapper objectMapper, DoctorRepository doctorRepository) {
        this.objectMapper = objectMapper;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Runs the doctor data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during doctor loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load doctors from JSON file and save to database
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Doctors.json").getInputStream());
        for (JsonNode node : root) {
            Doctor doctor = Doctor.builder()
                    .id(node.get("DoctorID").asInt())
                    .firstName(node.get("FirstName").asText())
                    .lastName(node.get("LastName").asText())
                    .dateOfBirth(LocalDate.parse(node.get("DateOfBirth").asText()))
                    .gender(node.get("Gender").asText())
                    .phone(node.get("Phone").asText())
                    .medicalDegree(node.get("MedicalDegree").asText())
                    .specialty(node.get("Specialty").asText())
                    .npiNumber(node.get("NIPNumber").asText())
                    .experience(node.get("Experience").asInt())
                    .languages(node.get("Languages").asText())
                    .build();
            doctorRepository.save(doctor);
        }
    }
}
