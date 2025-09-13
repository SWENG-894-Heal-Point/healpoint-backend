package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


/**
 * Represents a doctor in the system.
 * Maps to the "Doctors" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Entity
@Table(name = "Doctors", schema = "dbo")
public class Doctor {

    // Required by JPA
    protected Doctor() {
    }

    // Custom constructors
    public Doctor(Integer id, String name, String phone, String gender, String speciality, String licenseNo) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.speciality = speciality;
        this.licenseNo = licenseNo;
    }

    public Doctor(Integer id, String name, String phone, String gender, String speciality, Integer yearsOfExperience, String licenseNo) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.speciality = speciality;
        this.yearsOfExperience = yearsOfExperience;
        this.licenseNo = licenseNo;
    }

    @Id
    @Column(name = "DoctorID", nullable = false)
    private Integer id;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    @Column(name = "Phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "Gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "Speciality", nullable = false, length = 150)
    private String speciality;

    @Column(name = "YearsOfExperience")
    private Integer yearsOfExperience;

    @Column(name = "LicenseNo", nullable = false, length = 50, unique = true)
    private String licenseNo;

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

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
