package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Entity representing a medical prescription.
 * <p>
 * Contains fields for patient association, prescription items, instructions, and timestamps for creation and updates.
 * Utilizes JPA annotations for ORM mapping and Lombok for boilerplate code reduction.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Entity
@Table(name = "Prescriptions", schema = "dbo")
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PrescriptionID")
    private Integer id;

    @Setter
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PatientID", nullable = false)
    private Patient patient;

    @Setter
    @Column(name = "Instruction", length = 512)
    private String instruction;

    @Setter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "prescriptionId", nullable = false)
    private List<PrescriptionItem> prescriptionItems = new ArrayList<>();

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
