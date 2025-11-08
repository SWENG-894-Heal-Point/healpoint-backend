package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.ScheduleAppointmentDto;
import edu.psgv.healpointbackend.dto.UpdateAppointmentDto;
import edu.psgv.healpointbackend.model.Appointment;
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

import java.util.Arrays;
import java.util.List;

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
    private User requestor;
    private UpdateAppointmentDto updateAppointmentDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        updateAppointmentDto = new UpdateAppointmentDto();
        updateAppointmentDto.setToken("validToken");
        updateAppointmentDto.setAppointmentId(1);
        requestor = mockUser(TEST_EMAIL, Roles.DOCTOR, 5);
    }

    @Test
    void getMyAppointments_validToken_returnsAppointments() {
        String token = "validToken";
        User user = mockUser(TEST_EMAIL, Roles.PATIENT, 10);

        List<Appointment> expectedAppointments = Arrays.asList(mock(Appointment.class), mock(Appointment.class), mock(Appointment.class));

        when(accessManager.enforceOwnershipBasedAccess(token)).thenReturn(user);
        when(appointmentService.getAllAppointmentsByUser(user)).thenReturn(expectedAppointments);

        ResponseEntity<Object> response = controller.getMyAppointments(token);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedAppointments, response.getBody());
    }

    @Test
    void getMyAppointments_exceptions_handledProperly() {
        // Case 1: SecurityException
        String badToken = "badToken";
        when(accessManager.enforceOwnershipBasedAccess(badToken)).thenThrow(new SecurityException("Invalid token"));

        ResponseEntity<Object> unauthorizedResponse = controller.getMyAppointments(badToken);

        assertEquals(401, unauthorizedResponse.getStatusCode().value());
        assertEquals("Invalid token", unauthorizedResponse.getBody());

        // Case 2: Unexpected Exception
        String validToken = "validToken";
        User mockUser = mockUser(TEST_EMAIL, Roles.DOCTOR, 5);

        when(accessManager.enforceOwnershipBasedAccess(validToken)).thenReturn(mockUser);
        when(appointmentService.getAllAppointmentsByUser(mockUser)).thenThrow(new RuntimeException("DB down"));

        ResponseEntity<Object> errorResponse = controller.getMyAppointments(validToken);

        assertEquals(500, errorResponse.getStatusCode().value());
        assertEquals("An unexpected error occurred.", errorResponse.getBody());
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

    @Test
    void updateAppointment_validInput_successResponse() {
        when(accessManager.enforceOwnershipBasedAccess(updateAppointmentDto.getToken())).thenReturn(requestor);

        ResponseEntity<Object> response = controller.updateAppointment(updateAppointmentDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Appointment updated successfully.", response.getBody());
        verify(appointmentService).updateAppointment(updateAppointmentDto, requestor);
    }

    @Test
    void updateAppointment_exceptions_returnProperResponses() {
        // SecurityException path
        when(accessManager.enforceOwnershipBasedAccess(updateAppointmentDto.getToken()))
                .thenThrow(new SecurityException("Unauthorized"));
        ResponseEntity<Object> securityResponse = controller.updateAppointment(updateAppointmentDto);
        assertEquals(401, securityResponse.getStatusCode().value());
        assertEquals("Unauthorized", securityResponse.getBody());

        // IllegalArgumentException path
        reset(accessManager);
        when(accessManager.enforceOwnershipBasedAccess(updateAppointmentDto.getToken())).thenReturn(requestor);
        doThrow(new IllegalArgumentException("Invalid data")).when(appointmentService)
                .updateAppointment(updateAppointmentDto, requestor);
        ResponseEntity<Object> illegalArgResponse = controller.updateAppointment(updateAppointmentDto);
        assertEquals(400, illegalArgResponse.getStatusCode().value());
        assertEquals("Invalid data", illegalArgResponse.getBody());

        // Generic Exception path
        reset(appointmentService);
        when(accessManager.enforceOwnershipBasedAccess(updateAppointmentDto.getToken())).thenReturn(requestor);
        doThrow(new RuntimeException("DB error")).when(appointmentService).updateAppointment(updateAppointmentDto, requestor);
        ResponseEntity<Object> genericResponse = controller.updateAppointment(updateAppointmentDto);
        assertEquals(500, genericResponse.getStatusCode().value());
        assertEquals("An unexpected error occurred.", genericResponse.getBody());
    }
}