package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.dto.PrescriptionDto;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Prescription;
import edu.psgv.healpointbackend.model.PrescriptionItem;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Upserts (creates or updates) a prescription for a given patient.
     * <p>
     * Validates the patient existence and checks for duplicate medications before saving.
     * </p>
     *
     * @param prescriptionDto the DTO containing prescription data
     * @throws IllegalArgumentException if the patient does not exist or if there are duplicate medications
     */
    public void upsertPrescription(PrescriptionDto prescriptionDto) {
        int patientId = prescriptionDto.getPatientId();
        LOGGER.info("Starting upsert operation for prescription of patientId={}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    LOGGER.error("Patient with ID {} not found during upsert operation", patientId);
                    return new IllegalArgumentException("Patient with ID " + patientId + " not found");
                });

        List<String> duplicates = findDuplicateMedications(prescriptionDto.getPrescriptionItems());
        if (!duplicates.isEmpty()) {
            LOGGER.warn("Duplicate medications detected for patientId={}: {}", patientId, duplicates);
            throw new IllegalArgumentException("Duplicate medications found: " + String.join(", ", duplicates));
        }

        Prescription prescription = prescriptionRepository.findByPatientId(patientId).orElse(new Prescription());

        prescription.setPatient(patient);
        prescription.setInstruction(prescriptionDto.getInstruction());

        prescription.getPrescriptionItems().clear();
        if (prescriptionDto.getPrescriptionItems() != null) {
            LOGGER.debug("Adding {} prescription items for patientId={}", prescriptionDto.getPrescriptionItems().size(), patientId);
            prescription.getPrescriptionItems().addAll(prescriptionDto.getPrescriptionItems());
        }

        prescriptionRepository.save(prescription);
        LOGGER.info("Prescription upsert operation completed for patientId={}", patientId);
    }

    /**
     * Finds duplicate medications in the list of prescription items.
     * <p>
     * Comparison is case-insensitive and ignores leading/trailing whitespace.
     * </p>
     *
     * @param items the list of PrescriptionItem objects
     * @return a list of duplicate medication names
     */
    private List<String> findDuplicateMedications(List<PrescriptionItem> items) {
        Map<String, Long> medicationCounts = items.stream()
                .map(item -> item.getMedication().trim().toLowerCase())
                .collect(Collectors.groupingBy(med -> med, Collectors.counting()));

        return medicationCounts.entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).toList();
    }
}
