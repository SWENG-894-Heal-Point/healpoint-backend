package edu.psgv.healpointbackend.service;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;

import edu.psgv.healpointbackend.utilities.IoHelper;
import edu.psgv.healpointbackend.utilities.PasswordUtils;
import edu.psgv.healpointbackend.dto.RegistrationFormDto;
import edu.psgv.healpointbackend.repository.*;
import edu.psgv.healpointbackend.model.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * Service class for handling user registration and existence checks.
 *
 * @author Mahfuzur Rahman
 */
@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final RoleRepository roleRepository;
    private final EmployeeAccountRepository employeeAccountRepository;

    /**
     * Constructs a RegistrationService with required repositories.
     *
     * @param userRepository    repository for user entities
     * @param patientRepository repository for patient entities
     * @param doctorRepository  repository for doctor entities
     * @param roleRepository    repository for role entities
     */
    public RegistrationService(UserRepository userRepository, PatientRepository patientRepository, DoctorRepository doctorRepository, RoleRepository roleRepository, EmployeeAccountRepository employeeAccountRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.roleRepository = roleRepository;
        this.employeeAccountRepository = employeeAccountRepository;
    }

    /**
     * Checks if a user exists by email.
     *
     * @param email the email address to check
     * @return true if user exists, false otherwise
     * @throws IllegalArgumentException if the email is invalid
     */
    public Boolean checkIfUserExists(String email) {
        try {
            String validEmail = IoHelper.validateString(email);
            LOGGER.debug("Checking if user exists with email: {}", validEmail);

            boolean exists = userRepository.findByEmail(validEmail).isPresent();
            LOGGER.info("User existence check for {}: {}", validEmail, exists);

            return exists;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid email provided for existence check: {}", email, e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Registers a new user based on the provided registration form data.
     *
     * @param request the registration form data
     * @return ResponseEntity containing registration result message and status code
     */
    public ResponseEntity<String> registerUser(RegistrationFormDto request) {
        LOGGER.info("Starting registration for email: {}", request.getEmail());
        boolean passwordMatches = request.getPassword().equals(request.getConfirmPassword());
        if (!passwordMatches) {
            LOGGER.warn("Registration failed — passwords do not match for email: {}", request.getEmail());
            return ResponseEntity.status(400).body("Passwords do not match");
        }

        try {
            boolean userExists = checkIfUserExists(request.getEmail());
            if (userExists) {
                LOGGER.warn("Registration failed — user already exists: {}", request.getEmail());
                return ResponseEntity.status(409).body("User already exists");
            }

            LOGGER.debug("Fetching role: {}", request.getRole());
            Role role = roleRepository.findByDescription(request.getRole())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role description"));

            EmployeeAccount employeeAccount = null;
            if (!role.getDescription().equalsIgnoreCase(Roles.PATIENT.toString())) {
                employeeAccount = employeeAccountRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("The provided employee email does not exist in the system"));

                LOGGER.debug("Employee account found for email: {}", request.getEmail());
            }

            LOGGER.debug("Creating user entity for email: {}", request.getEmail());
            String hashedPassword = PasswordUtils.hashPassword(request.getPassword());
            User newUser = new User(request.getEmail(), hashedPassword, role);
            userRepository.save(newUser);
            LOGGER.info("User saved with ID: {}", newUser.getId());

            if (employeeAccount != null) {
                employeeAccount.setId(newUser.getId());
                employeeAccountRepository.save(employeeAccount);
                LOGGER.info("Linked EmployeeAccount ID: {} with User ID: {}", employeeAccount.getId(), newUser.getId());
            }

            if (role.getDescription().equalsIgnoreCase(Roles.PATIENT.toString())) {
                LOGGER.info("Creating Patient profile for user ID: {}", newUser.getId());
                patientRepository.save(createPatient(newUser, request));
            } else if (role.getDescription().equalsIgnoreCase(Roles.DOCTOR.toString())) {
                LOGGER.info("Creating Doctor profile for user ID: {}", newUser.getId());
                doctorRepository.save(createDoctor(newUser, request));
            }

            LOGGER.info("Registration completed successfully for email: {}", request.getEmail());
            return ResponseEntity.status(200).body("User registered successfully");
        } catch (Exception e) {
            LOGGER.error("Unexpected error during registration for {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred during registration: " + e.getMessage());
        }
    }

    /**
     * Creates a Patient entity from the user and registration form data.
     *
     * @param user    the user entity
     * @param request the registration form data
     * @return a Patient entity populated with the provided data
     */
    private Patient createPatient(User user, RegistrationFormDto request) {
        LOGGER.debug("Building Patient entity for user ID: {}", user.getId());
        Patient newPatient = Patient.builder()
                .id(user.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .build();

        if (!IoHelper.isNullOrEmpty(request.getInsuranceProvider()) && !IoHelper.isNullOrEmpty(request.getInsuranceId())) {
            LOGGER.debug("Adding insurance info for Patient ID: {}", user.getId());
            newPatient.setInsuranceProvider(request.getInsuranceProvider());
            newPatient.setInsuranceId(request.getInsuranceId());
        }

        return newPatient;
    }

    /**
     * Creates a Doctor entity from the user and registration form data.
     *
     * @param user    the user entity
     * @param request the registration form data
     * @return a Doctor entity populated with the provided data
     */
    private Doctor createDoctor(User user, RegistrationFormDto request) {
        LOGGER.debug("Building Doctor entity for user ID: {}", user.getId());
        Doctor newDoctor = Doctor.builder()
                .id(user.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .medicalDegree(request.getMedicalDegree())
                .specialty(request.getSpecialty())
                .npiNumber(request.getNpiNumber())
                .build();

        if (!IoHelper.isNullOrEmpty(request.getLanguages())) {
            LOGGER.debug("Adding languages for Doctor ID: {}", user.getId());
            newDoctor.setLanguages(request.getLanguages());
        }
        if (request.getExperience() != null && request.getExperience() >= 0) {
            LOGGER.debug("Adding years of experience for Doctor ID: {}", user.getId());
            newDoctor.setYearsOfExperience(request.getExperience());
        }

        return newDoctor;
    }
}
