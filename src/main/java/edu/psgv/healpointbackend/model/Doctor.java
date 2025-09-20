package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Represents a doctor in the system.
 * Maps to the "Doctors" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Entity
@Table(name = "Doctors", schema = "dbo")
public class Doctor {

    // Required by JPA
    protected Doctor() {
    }

    // Custom constructors
    @Builder
    public Doctor(Integer id, String firstName, String lastName, LocalDate dateOfBirth,
                  String gender, String phone, String medicalDegree, String specialty,
                  String npiNumber, Integer yearsOfExperience, String languages) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.medicalDegree = medicalDegree;
        this.specialty = specialty;
        this.npiNumber = npiNumber;
        this.yearsOfExperience = yearsOfExperience;
        this.languages = languages;
    }

    @Id
    @Column(name = "DoctorID", nullable = false)
    private Integer id;

    @Column(name = "FirstName", nullable = false, length = 100)
    private String firstName;

    @Column(name = "LastName", nullable = false, length = 100)
    private String lastName;

    @Column(name = "DateOfBirth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "Gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "Phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "MedicalDegree", nullable = false, length = 20)
    private String medicalDegree;

    @Column(name = "Specialty", nullable = false, length = 150)
    private String specialty;

    @Column(name = "NIPNumber", nullable = false, length = 50, unique = true)
    private String npiNumber;

    @Setter
    @Column(name = "YearsOfExperience")
    private Integer yearsOfExperience;

    @Setter
    @Column(name = "Languages", length = 50)
    private String languages;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
