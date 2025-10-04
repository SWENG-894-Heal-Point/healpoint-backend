package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.dto.NewPasswordDto;
import edu.psgv.healpointbackend.dto.UpdateProfileDto;
import edu.psgv.healpointbackend.model.Doctor;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.UserRepository;
import edu.psgv.healpointbackend.utilities.PasswordUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service class for handling user profile operations such as retrieval and update.
 * Supports both Patient and Doctor profiles.
 *
 * @author Mahfuzur Rahman
 */
@Service
public class ProfileUpdateService {
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
    public ProfileUpdateService(UserRepository userRepository, PatientRepository patientRepository,
                                DoctorRepository doctorRepository, Datastore datastore) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.datastore = datastore;
    }

    /**
     * Updates the password for the authenticated user.
     *
     * @param dto the data transfer object containing the token, old password,
     *            new password, and confirmation of the new password
     * @throws SecurityException        if the user is not authenticated or the old password is incorrect
     * @throws IllegalArgumentException if the new password and its confirmation do not match
     */
    public void updatePassword(NewPasswordDto dto) {
        LOGGER.info("Password update attempt.");
        User user = datastore.getUserByToken(dto.getToken());
        if (user == null) {
            LOGGER.warn("Password update failed: user not authenticated or authorized.");
            throw new SecurityException("Access denied: User not authenticated or authorized.");
        }

        boolean verifyOldPassword = PasswordUtils.verifyPassword(dto.getOldPassword(), user.getPassword());
        if (!verifyOldPassword) {
            LOGGER.warn("Password update failed: incorrect old password for user {}", user.getEmail());
            throw new SecurityException("Incorrect old password. Please try again.");
        }

        boolean isSamePassword = dto.getNewPassword().equals(dto.getConfirmNewPassword());
        if (!isSamePassword) {
            LOGGER.warn("Password update failed: new password and confirmation do not match for user {}", user.getEmail());
            throw new IllegalArgumentException("The new password and its confirmation do not match.");
        }

        String newHashedPassword = PasswordUtils.hashPassword(dto.getNewPassword());
        user.setPassword(newHashedPassword);
        userRepository.save(user);
        datastore.updateUser(user);

        LOGGER.info("Password successfully updated for user {}", user.getEmail());
    }


    /**
     * Updates the profile information for a user.
     * Handles updates for Patient and Doctor profiles based on user role.
     *
     * @param dto the data transfer object containing updated profile information
     * @return the updated email address of the user
     * @throws EntityNotFoundException if the user or profile is not found
     */
    public String updateUserProfile(UpdateProfileDto dto, String requestorEmail) {
        LOGGER.info("Updating profile for email={}", requestorEmail);
        if (!requestorEmail.equalsIgnoreCase(dto.getEmail())) {
            if (userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
                LOGGER.warn("Update failed: You cannot update to this email address because it’s already in use. {}", dto.getEmail());
                throw new IllegalArgumentException("Update failed: You cannot update to this email address because it’s already in use.");
            }
        }

        User user = userRepository.findByEmailIgnoreCase(requestorEmail)
                .orElseThrow(() -> {
                    LOGGER.warn("Update failed: no account found for email={}", requestorEmail);
                    return new EntityNotFoundException("No account associated with this email address.");
                });

        user.setEmail(dto.getEmail());
        userRepository.save(user);
        LOGGER.debug("Updated base user record for email={}", dto.getEmail());

        User loggedUser = datastore.getUserByToken(dto.getToken());
        if (loggedUser != null) {
            LOGGER.debug("Refreshing user in datastore for email={} token={}", dto.getEmail(), dto.getToken());
            loggedUser.setEmail(dto.getEmail());
            datastore.updateUser(loggedUser);
        }

        String roleDesc = user.getRole().getDescription();
        LOGGER.debug("Processing profile update for role={} email={}", roleDesc, user.getEmail());

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
        doctor.setExperience(dto.getExperience());
        doctor.setNpiNumber(dto.getNpiNumber());
        doctor.setLanguages(dto.getLanguages());

        doctorRepository.save(doctor);
        LOGGER.info("Updated Doctor profile for email={}", user.getEmail());
    }
}
