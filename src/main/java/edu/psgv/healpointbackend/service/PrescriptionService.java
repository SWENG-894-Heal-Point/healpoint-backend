package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Prescription;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service class for managing patient prescriptions.
 * <p>
 * Provides methods to retrieve and upsert prescriptions associated with patients.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;

    /**
     * Constructs a new PrescriptionService with required repositories.
     *
     * @param prescriptionRepository the repository for prescription operations
     * @param patientRepository      the repository for patient operations
     */
    public PrescriptionService(PrescriptionRepository prescriptionRepository, PatientRepository patientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Retrieves the prescription for a given patient.
     * <p>
     * If no prescription exists, returns a new Prescription instance with the patient set.
     * </p>
     *
     * @param patientId the ID of the patient
     * @return the Prescription object
     * @throws IllegalArgumentException if the patient does not exist
     */
    public Prescription getPrescription(Integer patientId) {
        LOGGER.info("Fetching prescription for patientId={}", patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    LOGGER.warn("Patient with ID {} not found", patientId);
                    return new IllegalArgumentException("Patient with ID " + patientId + " not found");
                });

        Prescription prescription = prescriptionRepository.findByPatientId(patientId).orElse(null);
        if (prescription == null) {
            LOGGER.info("No existing prescription found for patientId={}. Creating new prescription instance.", patientId);
            prescription = new Prescription();
            prescription.setPatient(patient);
        } else {
            LOGGER.debug("Existing prescription found for patientId={}", patientId);
        }

        LOGGER.info("Returning prescription for patientId={}", patientId);
        return prescription;
    }
}
