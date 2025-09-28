package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.dto.UpdateProfileDto;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service class for handling user profile operations such as retrieval and update.
 * Supports both Patient and Doctor profiles.
 *
 * @author Mahfuzur Rahman
 */
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final Datastore datastore;

    /**
     * Constructs a ProfileService with required repositories and datastore.
     *
     * @param userRepository    repository for User entities
     * @param patientRepository repository for Patient entities
     * @param doctorRepository  repository for Doctor entities
     * @param datastore         datastore for user session management
     */
    public ProfileService(UserRepository userRepository, PatientRepository patientRepository,
                          DoctorRepository doctorRepository, Datastore datastore) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.datastore = datastore;
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

    /**
     * Updates the profile information for a user.
     * Handles updates for Patient and Doctor profiles based on user role.
     *
     * @param dto the data transfer object containing updated profile information
     * @return the updated email address of the user
     * @throws EntityNotFoundException if the user or profile is not found
     */
    public String updateUserProfile(UpdateProfileDto dto) {
        String email = dto.getEmail();
        LOGGER.info("Updating profile for email={}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    LOGGER.warn("Update failed: no account found for email={}", email);
                    return new EntityNotFoundException("No account associated with this email address.");
                });

        user.setEmail(dto.getEmail());
        userRepository.save(user);
        LOGGER.debug("Updated base user record for email={}", dto.getEmail());

        User loggedUser = datastore.getUserByToken(dto.getToken());
        if (loggedUser != null) {
            LOGGER.debug("Refreshing user in datastore for email={} token={}", dto.getEmail(), dto.getToken());
            datastore.removeUser(loggedUser);
            loggedUser.setEmail(dto.getEmail());
            datastore.addUser(loggedUser);
        }

        String roleDesc = user.getRole().getDescription();
        LOGGER.debug("Processing profile update for role={} email={}", roleDesc, email);

        switch (roleDesc.toUpperCase()) {
            case Roles.PATIENT -> updatePatientProfile(user, dto);
            case Roles.DOCTOR -> updateDoctorProfile(user, dto);
            default -> LOGGER.info("No profile update needed for role: {}", roleDesc);
        }

        return user.getEmail();
    }

    /**
     * Updates the Patient profile for the given user.
     *
     * @param user the User entity
     * @param dto  the data transfer object containing updated profile information
     * @throws EntityNotFoundException if the patient profile is not found
     */
    private void updatePatientProfile(User user, UpdateProfileDto dto) {
        LOGGER.debug("Updating Patient profile for email={}", user.getEmail());
        Patient patient = patientRepository.findById(user.getId())
                .orElseThrow(() -> {
                    LOGGER.error("Patient profile not found for email={}", user.getEmail());
                    return new EntityNotFoundException("Patient profile not found for user email " + user.getEmail());
                });

        patient.setGender(dto.getGender());
        patient.setPhone(dto.getPhone());
        patient.setStreetAddress(dto.getStreetAddress());
        patient.setCity(dto.getCity());
        patient.setState(dto.getState());
        patient.setZipCode(dto.getZipCode());
        patient.setInsuranceProvider(dto.getInsuranceProvider());
        patient.setInsuranceId(dto.getInsuranceId());

        patientRepository.save(patient);
        LOGGER.info("Updated Patient profile for email={}", user.getEmail());
    }

    /**
     * Updates the Doctor profile for the given user.
     *
     * @param user the User entity
     * @param dto  the data transfer object containing updated profile information
     * @throws EntityNotFoundException if the doctor profile is not found
     */
    private void updateDoctorProfile(User user, UpdateProfileDto dto) {
        LOGGER.debug("Updating Doctor profile for email={}", user.getEmail());
        Doctor doctor = doctorRepository.findById(user.getId())
                .orElseThrow(() -> {
                    LOGGER.error("Doctor profile not found for email={}", user.getEmail());
                    return new EntityNotFoundException("Doctor profile not found for user email " + user.getEmail());
                });

        doctor.setGender(dto.getGender());
        doctor.setPhone(dto.getPhone());
        doctor.setMedicalDegree(dto.getMedicalDegree());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setYearsOfExperience(dto.getExperience());
        doctor.setNpiNumber(dto.getNpiNumber());
        doctor.setLanguages(dto.getLanguages());

        doctorRepository.save(doctor);
        LOGGER.info("Updated Doctor profile for email={}", user.getEmail());
    }
}
