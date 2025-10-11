package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Prescription;
import edu.psgv.healpointbackend.model.PrescriptionItem;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.PrescriptionRepository;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;


/**
 * Utility class to load test prescriptions from a JSON file into the database.
 * <p>
 * This class reads prescription data from a JSON file located in the classpath
 * and saves it to the database using the provided repositories.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
public class TestPrescriptionLoader {
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new TestPrescriptionLoader with required repositories and object mapper.
     *
     * @param prescriptionRepository the repository for prescription operations
     * @param patientRepository      the repository for patient operations
     * @param objectMapper           the ObjectMapper for JSON processing
     */
    public TestPrescriptionLoader(PrescriptionRepository prescriptionRepository, PatientRepository patientRepository, ObjectMapper objectMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Loads prescriptions from a JSON file and saves them to the database.
     *
     * @throws IOException if there is an error reading the JSON file
     */
    public void loadPrescriptions() throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Prescriptions.json").getInputStream());
        for (JsonNode node : root) {
            Patient patient = patientRepository.findById(node.get("patientId").asInt()).orElse(null);

            Prescription prescription = new Prescription();
            prescription.setPatient(patient);
            prescription.setInstruction(node.get("instruction").asText());

            JsonNode itemsNode = node.get("prescriptionItems");
            List<PrescriptionItem> items = objectMapper.convertValue(
                    itemsNode, new TypeReference<List<PrescriptionItem>>() {
                    }
            );
            prescription.setPrescriptionItems(items);
            prescriptionRepository.save(prescription);
        }
    }
}
