package edu.psgv.healpointbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents an employee account in the system.
 * Maps to the "EmployeeAccounts" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Getter
public class EmployeeAccount {
    // Required by JPA
    protected EmployeeAccount() {
    }

    @Id
    @Column(name = "Email", nullable = false, length = 256, unique = true)
    private String email;

    @Setter
    @Column(name = "UserID")
    private Integer id;

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
