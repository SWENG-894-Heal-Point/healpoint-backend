package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.model.Notification;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service class for managing notifications.
 * <p>
 * Provides methods to retrieve notifications associated with users.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    /**
     * Constructs a new NotificationService with required repository.
     *
     * @param notificationRepository the repository for notification operations
     */
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Retrieves all notifications for a given user.
     * <p>
     * Fetches notifications directly addressed to the user as well as those sent to the user's role group (if applicable).
     * </p>
     *
     * @param user the user whose notifications are to be fetched
     * @return a list of Notification objects
     */
    public List<Notification> getAllNotificationsByUser(User user) {
        int userId = user.getId();
        String role = user.getRole().getDescription();
        LOGGER.info("Fetching all notifications for user ID: {}, role: {}", userId, role);

        List<Notification> notifications = notificationRepository.findByRecipientId(userId);

        if (!role.equalsIgnoreCase(Roles.PATIENT)) {
            List<Notification> more = notificationRepository.findByRecipientGroup(role);
            notifications.addAll(more);
        }

        LOGGER.debug("Found {} notifications for user ID: {}", notifications.size(), userId);
        notifications.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
        return notifications;
    }
}
