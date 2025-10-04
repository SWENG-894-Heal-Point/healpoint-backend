package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service for retrieving user profiles based on email and role.
 * Handles fetching profiles for Patients and Doctors, returning appropriate responses.
 *
 * @author Mahfuzur Rahman
 */
@Service
public class ProfileGetService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Constructs a ProfileGetService with required repositories and datastore.
     *
     * @param userRepository    repository for User entities
     * @param patientRepository repository for Patient entities
     * @param doctorRepository  repository for Doctor entities
     */
    public ProfileGetService(UserRepository userRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Retrieves the profile for a user based on email and role.
     * Returns detailed profile for Patient or Doctor, or a message if not found.
     *
     * @param email      the email address of the user
     * @param targetRole the expected role of the user (optional)
     * @return ResponseEntity containing the profile or error message
     */
    public ResponseEntity<Object> getUserProfile(String email, String targetRole) {
        LOGGER.info("Fetching profile for email={} targetRole={}", email, targetRole);
        try {
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
            if (userOpt.isEmpty()) {
                LOGGER.warn("Profile retrieval failed: no account found for email={}", email);
                return ResponseEntity.status(401).body("No account associated with this email address.");
            }

            User user = userOpt.get();
            String roleDesc = user.getRole().getDescription();
            LOGGER.debug("Found user={} with role={}", email, roleDesc);

            if (targetRole != null && !targetRole.equalsIgnoreCase(roleDesc)) {
                LOGGER.warn("Role mismatch: requested={} actual={} for email={}", targetRole, roleDesc, email);
                return ResponseEntity.status(401).body(String.format("No %s account associated with this email address.", targetRole));
            }

            if (roleDesc.equalsIgnoreCase(Roles.PATIENT)) {
                Patient patient = patientRepository.findById(user.getId()).orElse(null);
                if (patient == null) {
                    LOGGER.error("Patient profile missing for email={}", email);
                    return ResponseEntity.status(404).body("Patient profile not found.");
                }
                LOGGER.info("Returning PatientProfile for email={}", email);
                return ResponseEntity.ok(new PatientProfile(patient, email, roleDesc));
            }

            if (roleDesc.equalsIgnoreCase(Roles.DOCTOR)) {
                Doctor doctor = doctorRepository.findById(user.getId()).orElse(null);
                if (doctor == null) {
                    LOGGER.error("Doctor profile missing for email={}", email);
                    return ResponseEntity.status(404).body("Doctor profile not found.");
                }
                LOGGER.info("Returning DoctorProfile for email={}", email);
                return ResponseEntity.ok(new DoctorProfile(doctor, email, roleDesc));
            }

            LOGGER.info("User with email={} has no detailed profile", email);
            return ResponseEntity.ok("This user does not have a profile.");
        } catch (Exception e) {
            LOGGER.error("Unexpected error during profile retrieval for {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    public ArrayList<PatientProfile> getAllPatients() {
        ArrayList<PatientProfile> profiles = new ArrayList<>();
        Iterable<Patient> patients = patientRepository.findAll();
        for (Patient patient : patients) {
            Optional<User> userOpt = userRepository.findById(patient.getId());
            userOpt.ifPresent(user -> profiles.add(new PatientProfile(patient, user.getEmail(), user.getRole().getDescription())));
        }
        return profiles;
    }
}
