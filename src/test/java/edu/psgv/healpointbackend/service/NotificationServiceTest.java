package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.model.Notification;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class NotificationServiceTest extends AbstractTestBase {
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllNotificationsByUser_nonEmptyRepository_returnsExpectedNotifications() {
        User testUser = mockUser("user@test.com", Roles.DOCTOR, 46);
        List<Notification> mockNotificationById = new ArrayList<>(List.of(
                mockNotification("Notification 1", LocalDateTime.now().minusDays(10)),
                mockNotification("Notification 3", LocalDateTime.now().minusDays(5))
        ));
        List<Notification> mockNotificationByGroup = new ArrayList<>(List.of(
                mockNotification("Notification 2", LocalDateTime.now().minusDays(8)),
                mockNotification("Notification 4", LocalDateTime.now().minusDays(2))
        ));

        when(notificationRepository.findByRecipientId(testUser.getId())).thenReturn(mockNotificationById);
        when(notificationRepository.findByRecipientGroup(testUser.getRole().getDescription())).thenReturn(mockNotificationByGroup);

        List<Notification> result = notificationService.getAllNotificationsByUser(testUser);
        assertEquals(4, result.size());
        assertEquals("Notification 4", result.get(0).getMessage());
        assertEquals("Notification 3", result.get(1).getMessage());
        assertEquals("Notification 2", result.get(2).getMessage());
        assertEquals("Notification 1", result.get(3).getMessage());
    }

    private Notification mockNotification(String message, LocalDateTime createdAt) {
        Notification n = Notification.builder().message(message).build();
        n.setCreatedAt(createdAt);
        return n;
    }
}