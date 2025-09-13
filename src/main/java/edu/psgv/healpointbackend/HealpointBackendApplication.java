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
    CommandLineRunner demo(UserRepository users, RoleRepository roles) {
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
            //Check if user already exists
            if (!users.findByEmail(newUser.getEmail()).isPresent()) {
                users.save(newUser);
            } else {
                System.out.println("User with email " + newUser.getEmail() + " already exists.");
            }

            // List all users
            users.findAll().forEach(user ->
                    System.out.println(user.getId() + " | " + user.getEmail() + " | " + user.getRole().getDescription())
            );
        };
    }
}
