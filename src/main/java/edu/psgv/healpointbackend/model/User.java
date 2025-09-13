package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a user in the system.
 * Maps to the "Users" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Entity
@Table(name = "Users", schema = "dbo")
public class User {

    // Required by JPA
    protected User() {
    }

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

    @Column(name = "Email", nullable = false, length = 256, unique = true)
    private String email;

    @Column(name = "Password", nullable = false, length = 256)
    private String password;

    @ManyToOne(optional = false)
    @JoinColumn(name = "RoleID", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_Roles"))
    private Role role;

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

    // getters/setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

