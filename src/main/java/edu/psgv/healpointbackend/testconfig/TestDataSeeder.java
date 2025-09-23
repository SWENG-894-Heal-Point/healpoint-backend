package edu.psgv.healpointbackend.testconfig;

import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * Seeds test data into the database when the application is run with the "test" profile.
 * <p>
 * This class implements {@link CommandLineRunner} to execute data seeding logic
 * after the application context is loaded. It checks if each repository is empty
 * before seeding data from corresponding JSON files located in the "testdata" directory.
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
 * JSON files should be placed in "src/main/resources/testdata/" and named as follows:
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
    private final RoleRepository roles;
    private final UserRepository users;
    private final EmployeeAccountRepository employees;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new TestDataSeeder with the given repositories and object mapper.
     *
     * @param roles        the RoleRepository for accessing role data
     * @param users        the UserRepository for accessing user data
     * @param employees    the EmployeeAccountRepository for accessing employee account data
     * @param doctors      the DoctorRepository for accessing doctor data
     * @param patients     the PatientRepository for accessing patient data
     * @param objectMapper the ObjectMapper for JSON processing
     */
    public TestDataSeeder(RoleRepository roles, UserRepository users, EmployeeAccountRepository employees,
                          DoctorRepository doctors, PatientRepository patients, ObjectMapper objectMapper) {
        this.roles = roles;
        this.users = users;
        this.employees = employees;
        this.doctors = doctors;
        this.patients = patients;
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
     * @throws Exception if an error occurs during file reading or data processing
     */
    @Override
    public void run(String... args) throws Exception {
        // Seed Roles
        if (roles.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/Roles.json").getInputStream();
            List<Role> roles = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            this.roles.saveAll(roles);
        }

        // Seed Users
        if (users.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/Users.json").getInputStream();
            List<User> users = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            this.users.saveAll(users);
        }

        // Seed Employees
        if (employees.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/EmployeeAccounts.json").getInputStream();
            List<EmployeeAccount> employees = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            this.employees.saveAll(employees);
        }

        // Seed Doctors
        if (doctors.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/Doctors.json").getInputStream();
            List<Doctor> doctors = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            this.doctors.saveAll(doctors);
        }

        // Seed Patients
        if (patients.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/Patients.json").getInputStream();
            List<Patient> patients = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            this.patients.saveAll(patients);
        }
    }
}
