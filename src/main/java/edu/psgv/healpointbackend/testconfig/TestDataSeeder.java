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

@Component
@Profile("test")
public class TestDataSeeder implements CommandLineRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final EmployeeAccountRepository employees;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final ObjectMapper objectMapper;

    public TestDataSeeder(RoleRepository roles, UserRepository users, EmployeeAccountRepository employees,
                          DoctorRepository doctors, PatientRepository patients, ObjectMapper objectMapper) {
        this.roles = roles;
        this.users = users;
        this.employees = employees;
        this.doctors = doctors;
        this.patients = patients;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Roles
        if (roles.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/Roles.json").getInputStream();
            List<Role> roles = objectMapper.readValue(inputStream, new TypeReference<List<Role>>() {
            });
            this.roles.saveAll(roles);
        }

        // Seed Users
        if (users.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/Users.json").getInputStream();
            List<User> users = objectMapper.readValue(inputStream, new TypeReference<List<User>>() {
            });
            this.users.saveAll(users);
        }

        // Seed Employees
        if (employees.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/EmployeeAccounts.json").getInputStream();
            List<EmployeeAccount> employees = objectMapper.readValue(inputStream, new TypeReference<List<EmployeeAccount>>() {
            });
            this.employees.saveAll(employees);
        }

        // Seed Doctors
        if (doctors.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/Doctors.json").getInputStream();
            List<Doctor> doctors = objectMapper.readValue(inputStream, new TypeReference<List<Doctor>>() {
            });
            this.doctors.saveAll(doctors);
        }

        // Seed Patients
        if (patients.count() == 0) {
            InputStream inputStream = new ClassPathResource("src/main/resources/testdata/Patients.json").getInputStream();
            List<Patient> patients = objectMapper.readValue(inputStream, new TypeReference<List<Patient>>() {
            });
            this.patients.saveAll(patients);
        }
    }
}
