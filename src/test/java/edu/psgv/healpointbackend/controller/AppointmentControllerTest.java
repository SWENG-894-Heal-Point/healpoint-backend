package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.ScheduleAppointmentDto;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentControllerTest extends AbstractTestBase {
    @Mock
    private AccessManager accessManager;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController controller;

    private final String TEST_EMAIL = "user@test.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void scheduleAppointment_validPatientToken_returnsOk() {
        User user = mockUser(TEST_EMAIL, Roles.PATIENT, 10);
        ScheduleAppointmentDto dto = new ScheduleAppointmentDto();
        dto.setToken("valid-token");

        when(accessManager.enforceOwnershipBasedAccess(dto.getToken())).thenReturn(user);

        ResponseEntity<Object> response = controller.scheduleAppointment(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Appointment scheduled successfully.", response.getBody());
        assertEquals(10, dto.getPatientId());
        verify(accessManager).enforceOwnershipBasedAccess(dto.getToken());
        verify(appointmentService).scheduleAppointment(dto);
    }

    @Test
    void scheduleAppointment_exceptions_returnProperResponses() {
        ScheduleAppointmentDto dto = new ScheduleAppointmentDto();
        dto.setToken("test-token");

        // --- SecurityException → 401 ---
        doThrow(new SecurityException("Unauthorized")).when(accessManager).enforceOwnershipBasedAccess("bad-token");
        dto.setToken("bad-token");
        ResponseEntity<Object> unauthorized = controller.scheduleAppointment(dto);
        assertEquals(401, unauthorized.getStatusCode().value());
        assertEquals("Unauthorized", unauthorized.getBody());
        verify(accessManager).enforceOwnershipBasedAccess("bad-token");

        // --- IllegalArgumentException → 400 ---
        dto.setToken("good-token");
        User user = mockUser(TEST_EMAIL, Roles.PATIENT, 20);
        when(accessManager.enforceOwnershipBasedAccess("good-token")).thenReturn(user);
        doThrow(new IllegalArgumentException("Invalid data")).when(appointmentService).scheduleAppointment(dto);

        ResponseEntity<Object> badRequest = controller.scheduleAppointment(dto);
        assertEquals(400, badRequest.getStatusCode().value());
        assertEquals("Invalid data", badRequest.getBody());
        verify(appointmentService).scheduleAppointment(dto);

        // --- Generic Exception → 500 ---
        dto.setToken("ok-token");
        when(accessManager.enforceOwnershipBasedAccess("ok-token")).thenReturn(user);
        doThrow(new RuntimeException("System failure")).when(appointmentService).scheduleAppointment(dto);

        ResponseEntity<Object> serverError = controller.scheduleAppointment(dto);
        assertEquals(500, serverError.getStatusCode().value());
        assertEquals("An unexpected error occurred.", serverError.getBody());
    }
}