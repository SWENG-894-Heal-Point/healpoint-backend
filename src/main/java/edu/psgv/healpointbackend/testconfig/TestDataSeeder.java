package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Seeds test data into the database when the application is run with the "test" profile.
 * <p>
 * This class implements {@link CommandLineRunner} to execute data seeding logic
 * after the application context is loaded. It checks if each repository is empty
 * before seeding data from corresponding JSON files located in the "test-data" directory.
 * </p>
 *
 * <p>
 * The following entities are seeded:
 * <ul>
 *   <li>Roles</li>
 *   <li>Users</li>
 *   <li>EmployeeAccounts</li>
 *   <li>Doctors</li>
 *   <li>Patients</li>
 * </ul>
 * </p>
 *
 * <p>
 * JSON files should be placed in "test-data/" and named as follows:
 * <ul>
 *   <li>Roles.json</li>
 *   <li>Users.json</li>
 *   <li>EmployeeAccounts.json</li>
 *   <li>Doctors.json</li>
 *   <li>Patients.json</li>
 * </ul>
 * </p>
 *
 * @author Mahfuzur Rahman
 * @see CommandLineRunner
 * @see Profile
 * @see ObjectMapper
 * @see ClassPathResource
 */
@Component
@Profile("test")
public class TestDataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new TestDataSeeder with the given repositories and object mapper.
     *
     * @param roleRepository            the RoleRepository for accessing role data
     * @param userRepository            the UserRepository for accessing user data
     * @param employeeAccountRepository the EmployeeAccountRepository for accessing employee account data
     * @param doctorRepository          the DoctorRepository for accessing doctor data
     * @param patientRepository         the PatientRepository for accessing patient data
     * @param objectMapper              the ObjectMapper for JSON processing
     */
    public TestDataSeeder(RoleRepository roleRepository, UserRepository userRepository, EmployeeAccountRepository employeeAccountRepository,
                          DoctorRepository doctorRepository, PatientRepository patientRepository, ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.employeeAccountRepository = employeeAccountRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Runs the data seeding process after the application context is loaded.
     * <p>
     * This method checks if each repository is empty and, if so, reads data from
     * corresponding JSON files and saves it to the database.
     * </p>
     *
     * @param args command line arguments (not used)
     * @throws IOException if an error occurs during file reading or JSON parsing
     */
    @Override
    public void run(String... args) throws Exception {
        loadRoles();
        loadUsers();
        loadEmployeeAccounts();
        loadDoctors();
        loadPatients();

        // print all roles and users
        roleRepository.findAll().forEach(role -> LOGGER.info("{} Role: {}", role.getId(), role.getDescription()));
        userRepository.findAll().forEach(user -> LOGGER.info("{} User: {}, Role: {}", user.getId(), user.getEmail(), user.getRole().getDescription()));
    }

    /**
     * Loads roles from the "Roles.json" file and saves them to the database.
     * <p>
     * This method reads the JSON file, parses each role, and saves it using the Role
     * repository. It assumes the JSON structure contains a "RoleDescription" field.
     * </p>
     *
     * @throws IOException if an error occurs during file reading or JSON parsing
     */
    private void loadRoles() throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Roles.json").getInputStream());
        for (JsonNode node : root) {
            Role role = new Role();
            role.setDescription(node.get("RoleDescription").asText());
            roleRepository.save(role);
        }
    }

    /**
     * Loads users from the "Users.json" file and saves them to the database.
     * <p>
     * This method reads the JSON file, parses each user, associates it with a role,
     * and saves it using the User repository. It assumes the JSON structure contains
     * "Email", "Password", and "RoleID" fields.
     * </p>
     *
     * @throws IOException if an error occurs during file reading or JSON parsing
     */
    private void loadUsers() throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Users.json").getInputStream());
        for (JsonNode node : root) {
            Role role = roleRepository.findById(node.get("RoleID").asInt())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role."));
            User newUser = new User(node.get("Email").asText(), node.get("Password").asText(), role);
            userRepository.save(newUser);
        }
    }

    /**
     * Loads employee accounts from the "EmployeeAccounts.json" file and saves them to the database.
     * <p>
     * This method reads the JSON file, parses each employee account, and saves it using the
     * EmployeeAccount repository. It assumes the JSON structure contains "Email" and "UserID" fields.
     * </p>
     *
     * @throws IOException if an error occurs during file reading or JSON parsing
     */
    private void loadEmployeeAccounts() throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/EmployeeAccounts.json").getInputStream());
        for (JsonNode node : root) {
            EmployeeAccount employeeAccount = new EmployeeAccount(node.get("Email").asText(), node.get("UserID").asInt());
            employeeAccountRepository.save(employeeAccount);
        }
    }

    /**
     * Loads doctors from the "Doctors.json" file and saves them to the database.
     * <p>
     * Reads each doctor entry from the JSON file, constructs a Doctor entity using the builder pattern,
     * and persists it using the DoctorRepository. Assumes the JSON structure contains fields:
     * "DoctorID", "FirstName", "LastName", "DateOfBirth", "Gender", "Phone", "MedicalDegree",
     * "Specialty", "NIPNumber", "YearsOfExperience", and "Languages".
     * </p>
     *
     * @throws IOException if an error occurs during file reading or JSON parsing
     */
    private void loadDoctors() throws IOException {
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
                    .yearsOfExperience(node.get("YearsOfExperience").asInt())
                    .languages(node.get("Languages").asText())
                    .build();
            doctorRepository.save(doctor);
        }
    }

    /**
     * Loads patients from the "Patients.json" file and saves them to the database.
     * <p>
     * Reads each patient entry from the JSON file, constructs a Patient entity using the builder pattern,
     * and persists it using the PatientRepository. Assumes the JSON structure contains fields:
     * "PatientID", "FirstName", "LastName", "DateOfBirth", "Gender", "Phone", "StreetAddress",
     * "City", "State", "ZipCode", "InsuranceID", and "InsuranceProvider".
     * </p>
     *
     * @throws IOException if an error occurs during file reading or JSON parsing
     */
    private void loadPatients() throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Patients.json").getInputStream());
        for (JsonNode node : root) {
            Patient patient = Patient.builder()
                    .id(node.get("PatientID").asInt())
                    .firstName(node.get("FirstName").asText())
                    .lastName(node.get("LastName").asText())
                    .dateOfBirth(LocalDate.parse(node.get("DateOfBirth").asText()))
                    .gender(node.get("Gender").asText())
                    .phone(node.get("Phone").asText())
                    .streetAddress(node.get("StreetAddress").asText())
                    .city(node.get("City").asText())
                    .state(node.get("State").asText())
                    .zipCode(node.get("ZipCode").asText())
                    .insuranceId(node.get("InsuranceID").asText())
                    .insuranceProvider(node.get("InsuranceProvider").asText())
                    .build();
            patientRepository.save(patient);
        }
    }
}
