package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * Entity representing a notification.
 * <p>
 * Contains fields for user association, recipient details, message content, read status, and timestamps for creation and updates.
 * Utilizes JPA annotations for ORM mapping and Lombok for boilerplate code reduction.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
@Entity
@Table(name = "Notifications", schema = "dbo")
public class Notification {

    // Required by JPA
    protected Notification() {
    }

    // Custom constructors
    @Builder
    public Notification(Integer userId, Integer recipientId, String recipientGroup, String message) {
        this.userId = userId;
        this.recipientId = recipientId;
        this.recipientGroup = recipientGroup;
        this.message = message;
        this.isRead = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Integer id;

    @Column(name = "UserID")
    private Integer userId;

    @Column(name = "RecipientID")
    private Integer recipientId;

    @Column(name = "RecipientGroup", length = 100)
    private String recipientGroup;

    @Column(name = "Message", nullable = false, length = 500)
    private String message;

    @Column(name = "IsRead", nullable = false)
    private Boolean isRead = false;

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
