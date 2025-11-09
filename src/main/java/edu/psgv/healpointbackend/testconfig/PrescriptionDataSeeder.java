package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Prescription;
import edu.psgv.healpointbackend.model.PrescriptionItem;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.PrescriptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Data seeder for loading prescriptions into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute prescription loading logic
 * when the application starts in the "test" profile. It reads prescription data
 * from a JSON file and saves it to the database using the PrescriptionRepository.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(35)
public class PrescriptionDataSeeder implements CommandLineRunner {
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new PrescriptionDataSeeder with the specified repositories and ObjectMapper.
     *
     * @param prescriptionRepository the repository for Prescription entities
     * @param patientRepository      the repository for Patient entities
     * @param objectMapper           the ObjectMapper for JSON processing
     */
    public PrescriptionDataSeeder(PrescriptionRepository prescriptionRepository, PatientRepository patientRepository, ObjectMapper objectMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Runs the prescription data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during prescription loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load prescriptions from JSON file and save to database
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Prescriptions.json").getInputStream());
        for (JsonNode node : root) {
            Patient patient = patientRepository.findById(node.get("patientId").asInt()).orElse(null);

            Prescription prescription = new Prescription();
            prescription.setPatient(patient);
            prescription.setInstruction(node.get("instruction").asText());

            JsonNode itemsNode = node.get("prescriptionItems");
            List<PrescriptionItem> items = objectMapper.convertValue(itemsNode, new TypeReference<List<PrescriptionItem>>() {});
            prescription.setPrescriptionItems(items);
            prescriptionRepository.save(prescription);
        }
    }
}
