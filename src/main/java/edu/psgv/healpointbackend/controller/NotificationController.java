package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.model.Notification;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for managing notifications.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class NotificationController {
    private final NotificationService notificationService;
    private final AccessManager accessManager;

    /**
     * Constructs a new NotificationController with required services.
     *
     * @param notificationService the service for notification operations
     * @param accessManager       the service for access control
     */
    public NotificationController(NotificationService notificationService, AccessManager accessManager) {
        this.notificationService = notificationService;
        this.accessManager = accessManager;
    }

    /**
     * Retrieves all notifications for the authenticated user.
     *
     * @param token the authentication token
     * @return ResponseEntity containing the list of notifications or an error message
     */
    @GetMapping("/api/get-my-notifications")
    public ResponseEntity<Object> getMyNotifications(@Valid @RequestParam String token) {
        try {
            User requestor = accessManager.enforceOwnershipBasedAccess(token);
            LOGGER.info("Fetching notifications for user ID: {}", requestor.getId());

            List<Notification> notifications = notificationService.getAllNotificationsByUser(requestor);
            LOGGER.info("Successfully retrieved {} notifications for user ID: {}", notifications.size(), requestor.getId());
            return ResponseEntity.ok(notifications);
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized access attempt with token: {}", token, e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }
}
