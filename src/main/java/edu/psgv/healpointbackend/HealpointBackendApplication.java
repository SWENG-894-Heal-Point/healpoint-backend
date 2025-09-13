package edu.psgv.healpointbackend;

import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HealpointBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealpointBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(UserRepository users, RoleRepository roles, DoctorRepository doctors, PatientRepository patients) {
        return args -> {
            // List all roles
            roles.findAll().forEach(role ->
                    System.out.println(role.getId() + " | " + role.getDescription())
            );

            // Find Admin Role
            Role admin = roles.findByDescription("Admin")
                    .orElseThrow(() -> new IllegalStateException("Seed role 'Admin' first"));

            // Insert a User
            User newUser = new User("admin@example.com", "HashedPassword", admin);
            if (!users.findByEmail(newUser.getEmail()).isPresent()) {
                users.save(newUser);
            } else {
                System.out.println("User with email " + newUser.getEmail() + " already exists.");
            }

            // List all users
            users.findAll().forEach(user ->
                    System.out.println(user.getId() + " | " + user.getEmail() + " | " + user.getRole().getDescription())
            );

            // Insert a Doctor
            Doctor newDoctor = new Doctor(1, "John Doe, MD", "2155660565", "Male", "Cardiology", "example_license123");
            if (!doctors.findById(newDoctor.getId()).isPresent()) {
                doctors.save(newDoctor);
            } else {
                System.out.println("Doctor with ID " + newDoctor.getId() + " already exists.");
            }

            // List all doctors
            doctors.findAll().forEach(doctor ->
                    System.out.println(doctor.getId() + " | " + doctor.getName() + " | " + doctor.getSpeciality())
            );

            // Insert a Patient
            Patient newPatient = new Patient(2, "Test Patient", "1990-05-15", "2155652356", "Female","123 Main St, City, Country");
            if (!patients.findById(newPatient.getId()).isPresent()) {
                patients.save(newPatient);
            } else {
                System.out.println("Patient with ID " + newPatient.getId() + " already exists.");
            }

            // List all patients
            patients.findAll().forEach(patient ->
                    System.out.println(patient.getId() + " | " + patient.getName() + " | " + patient.getDateOfBirth())
            );
        };
    }
}
