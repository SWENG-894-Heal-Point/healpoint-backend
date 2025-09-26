package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;


/**
 * Represents a patient in the system.
 * Maps to the "Patients" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
@Entity
@Table(name = "Patients", schema = "dbo")
public class Patient {

    // Required by JPA
    protected Patient() { }

    // Custom constructors
    @Builder
    public Patient(Integer id, String firstName, String lastName, LocalDate dateOfBirth, String gender, String phone,
                   String streetAddress, String city, String state, String zipCode, String insuranceProvider, String insuranceId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.insuranceProvider = insuranceProvider;
        this.insuranceId = insuranceId;
    }

    @Id
    @Column(name = "PatientID", nullable = false)
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

    @Column(name = "StreetAddress", nullable = false, length = 255)
    private String streetAddress;

    @Column(name = "City", nullable = false, length = 100)
    private String city;

    @Column(name = "State", nullable = false, length = 30)
    private String state;

    @Column(name = "ZipCode", nullable = false, length = 10)
    private String zipCode;

    @Setter
    @Column(name = "InsuranceProvider", length = 100)
    private String insuranceProvider;

    @Setter
    @Column(name = "InsuranceID", length = 50)
    private String insuranceId;

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
