package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * Represents a user in the system.
 * Maps to the "Users" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Entity
@Table(name = "Users", schema = "dbo")
public class User {

    // Required by JPA
    protected User() { }

    // Custom constructor
    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer id;

    @Setter
    @Column(name = "Email", nullable = false, length = 256, unique = true)
    private String email;

    @Setter
    @Column(name = "Password", nullable = false, length = 256)
    private String password;

    @ManyToOne(optional = false)
    @JoinColumn(name = "RoleID", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_Roles"))
    private Role role;

    @Setter
    @Transient
    private String token;

    @Setter
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

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

