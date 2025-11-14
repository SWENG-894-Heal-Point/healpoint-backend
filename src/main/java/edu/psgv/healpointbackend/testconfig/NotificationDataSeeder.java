package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.Notification;
import edu.psgv.healpointbackend.repository.NotificationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;


/**
 * Data seeder for loading notifications into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute notification loading logic
 * when the application starts in the "test" profile. It reads notification data
 * from a JSON file and saves it to the database using the NotificationRepository.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(50)
public class NotificationDataSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;

    /**
     * Constructs a new NotificationDataSeeder with the specified ObjectMapper and NotificationRepository.
     *
     * @param objectMapper           the ObjectMapper for JSON processing
     * @param notificationRepository the repository for Notification entities
     */
    public NotificationDataSeeder(ObjectMapper objectMapper, NotificationRepository notificationRepository) {
        this.objectMapper = objectMapper;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Runs the notification data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during notification loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load notifications from JSON file
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Notifications.json").getInputStream());
        for (JsonNode node : root) {
            Notification notification = Notification.builder().message(node.get("message").asText()).build();

            if (node.has("userId") && !node.get("userId").isNull()) {
                notification.setRecipientId(Integer.parseInt(node.get("userId").asText()));
            }

            if (node.has("recipientId") && !node.get("recipientId").isNull()) {
                notification.setRecipientId(Integer.parseInt(node.get("recipientId").asText()));
            } else if (node.has("recipientGroup") && !node.get("recipientGroup").isNull()) {
                notification.setRecipientGroup(node.get("recipientGroup").asText());
            }

            notificationRepository.save(notification);
        }
    }
}
