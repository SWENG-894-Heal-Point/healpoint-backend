package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.model.Notification;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationControllerTest extends AbstractTestBase {
    @Mock
    private AccessManager accessManager;

    @Mock
    private NotificationService service;

    @InjectMocks
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMyNotifications_validToken_returnsNotifications() {
        String token = "validToken";
        User testUser = mockUser("user@test.com", Roles.PATIENT, 48);

        List<Notification> expectedNotifications = Arrays.asList(mock(Notification.class), mock(Notification.class), mock(Notification.class));

        when(accessManager.enforceOwnershipBasedAccess(token)).thenReturn(testUser);
        when(service.getAllNotificationsByUser(testUser)).thenReturn(expectedNotifications);

        ResponseEntity<Object> response = controller.getMyNotifications(token);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedNotifications, response.getBody());
    }

    @Test
    void getMyNotifications_exceptions_handledProperly() {
        // Case 1: SecurityException
        String badToken = "badToken";
        when(accessManager.enforceOwnershipBasedAccess(badToken)).thenThrow(new SecurityException("Invalid token"));

        ResponseEntity<Object> unauthorizedResponse = controller.getMyNotifications(badToken);

        assertEquals(401, unauthorizedResponse.getStatusCode().value());
        assertEquals("Invalid token", unauthorizedResponse.getBody());

        // Case 2: Unexpected Exception
        String validToken = "validToken";
        User mockUser = mockUser("user@test.com", Roles.DOCTOR, 49);

        when(accessManager.enforceOwnershipBasedAccess(validToken)).thenReturn(mockUser);
        when(service.getAllNotificationsByUser(mockUser)).thenThrow(new RuntimeException("DB down"));

        ResponseEntity<Object> errorResponse = controller.getMyNotifications(validToken);

        assertEquals(500, errorResponse.getStatusCode().value());
        assertEquals("An unexpected error occurred.", errorResponse.getBody());
    }
}