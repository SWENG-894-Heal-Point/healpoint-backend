package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;


/**
 * Entity representing an item in a medical prescription.
 * <p>
 * Contains fields for medication details such as item number, medication name, dosage, frequency, duration, and fills left.
 * Utilizes JPA annotations for ORM mapping and Lombok for boilerplate code reduction.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
@Entity
@Table(name = "PrescriptionItem", schema = "dbo")
public class PrescriptionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Positive
    @Column(name = "ItemNumber", nullable = false)
    private Integer itemNumber;

    @NotBlank
    @Column(name = "Medication", nullable = false, length = 50)
    private String medication;

    @Positive
    @Column(name = "Dosage", nullable = false)
    private Integer dosage;

    @Positive
    @Column(name = "Frequency", nullable = false)
    private Integer frequency;

    @Positive
    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @PositiveOrZero
    @Column(name = "FillsLeft", nullable = false)
    private Integer fillsLeft;
}
