package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Represents a patient in the system.
 * Maps to the "Patients" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Entity
@Table(name = "Patients", schema = "dbo")
public class Patient {

    // Required by JPA
    protected Patient() { }

    // Custom constructors
    public Patient(Integer id, String name, String dateOfBirth, String phone, String gender, String address) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = LocalDate.parse(dateOfBirth);
        this.phone = phone;
        this.gender = gender;
        this.address = address;
    }

    public Patient(Integer id, String name, String dateOfBirth, String phone, String gender, String address, String insuranceId, String insuranceProvider) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = LocalDate.parse(dateOfBirth);
        this.phone = phone;
        this.gender = gender;
        this.address = address;
        this.insuranceId = insuranceId;
        this.insuranceProvider = insuranceProvider;
    }

    @Id
    @Column(name = "PatientID", nullable = false)
    private Integer id;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    @Column(name = "DateOfBirth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "Phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "Gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "Address", nullable = false, length = 255)
    private String address;

    @Column(name = "InsuranceID", length = 50)
    private String insuranceId;

    @Column(name = "InsuranceProvider", length = 100)
    private String insuranceProvider;

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

    // getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = LocalDate.parse(dateOfBirth);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(String insuranceId) {
        this.insuranceId = insuranceId;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
