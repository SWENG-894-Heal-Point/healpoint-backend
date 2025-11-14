package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.dto.PrescriptionDto;
import edu.psgv.healpointbackend.model.Notification;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Prescription;
import edu.psgv.healpointbackend.model.PrescriptionItem;
import edu.psgv.healpointbackend.repository.NotificationRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.PrescriptionRepository;
import edu.psgv.healpointbackend.utilities.IoHelper;
import edu.psgv.healpointbackend.utilities.PrescriptionDiffUtil;
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
    private final NotificationRepository notificationRepository;
    private final PrescriptionDiffUtil prescriptionDiffUtil;


    /**
     * Constructs a new PrescriptionService with required repositories.
     *
     * @param prescriptionRepository the repository for prescription operations
     * @param patientRepository      the repository for patient operations
     */
    public PrescriptionService(PrescriptionRepository prescriptionRepository, PatientRepository patientRepository,
                               NotificationRepository notificationRepository, PrescriptionDiffUtil prescriptionDiffUtil) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.notificationRepository = notificationRepository;
        this.prescriptionDiffUtil = prescriptionDiffUtil;
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

        if (prescriptionDto.getPrescriptionItems() != null) {
            String report = prescriptionDiffUtil.diffReport(prescription.getPrescriptionItems(), prescriptionDto.getPrescriptionItems());
            if (!IoHelper.isNullOrEmpty(report)) {
                Notification notification = Notification.builder().message(report).recipientId(patientId).build();
                notificationRepository.save(notification);
            }

            LOGGER.debug("Adding {} prescription items for patientId={}", prescriptionDto.getPrescriptionItems().size(), patientId);
            prescription.getPrescriptionItems().clear();
            prescription.getPrescriptionItems().addAll(prescriptionDto.getPrescriptionItems());
        }

        prescriptionRepository.save(prescription);
        LOGGER.info("Prescription upsert operation completed for patientId={}", patientId);
    }

    /**
     * Requests a prescription refill for the specified patient and medications.
     * <p>
     * Validates patient existence and checks that all requested medications exist in the current prescription.
     * Creates a notification for the doctor's group if successful.
     * </p>
     *
     * @param patientId   the ID of the patient requesting the refill
     * @param medications the list of medication names to be refilled
     * @throws IllegalArgumentException if the patient does not exist or if any medication is not found in the existing prescription
     */
    public void requestPrescriptionRefill(int patientId, List<String> medications) {
        LOGGER.info("Starting refill operation for patientId={}", patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    LOGGER.error("Patient with ID {} not found during refill operation", patientId);
                    return new IllegalArgumentException("Patient with ID " + patientId + " not found");
                });

        Prescription prescription = prescriptionRepository.findByPatientId(patientId)
                .orElseThrow(() -> {
                    LOGGER.error("No existing prescription found for patientId={} during refill operation", patientId);
                    return new IllegalArgumentException("No existing prescription found for patientId=" + patientId);
                });

        if (!allMedicationsExist(prescription, medications)) {
            LOGGER.warn("One or more medications not found in existing prescription for patientId={}", patientId);
            throw new IllegalArgumentException("One or more medications not found in existing prescription");
        }

        String message = String.format("%s, %s (ID: %d) requested a refill for %s",
                patient.getLastName(), patient.getFirstName(), patient.getId(), String.join(", ", medications));
        Notification notification = Notification.builder().userId(patientId).message(message).recipientGroup("Doctor").build();

        notificationRepository.save(notification);
        LOGGER.info("Refill request notification created for patientId={}", patientId);
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

    /**
     * Checks if all requested medications exist in the current prescription.
     * <p>
     * Comparison is case-insensitive and ignores leading/trailing whitespace.
     * </p>
     *
     * @param prescription the existing Prescription object
     * @param medications  the list of medication names to check
     * @return true if all medications exist, false otherwise
     */
    private boolean allMedicationsExist(Prescription prescription, List<String> medications) {
        List<String> existingMedications = prescription.getPrescriptionItems().stream()
                .map(item -> item.getMedication().trim().toLowerCase())
                .toList();

        List<String> notFound = medications.stream()
                .map(med -> med.trim().toLowerCase())
                .filter(med -> !existingMedications.contains(med))
                .toList();

        return notFound.isEmpty();
    }
}
