package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.AvailableAppointmentDatesDto;
import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.AppointmentAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AppointmentAvailabilityControllerTest {
    @Mock
    private AccessManager accessManager;

    @Mock
    private AppointmentAvailabilityService appointmentAvailabilityService;

    @InjectMocks
    private AppointmentAvailabilityController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAvailableAppointmentDates_validToken_returnsOk() {
        String token = "valid-token";
        List<AvailableAppointmentDatesDto> expectedList = List.of(mock(AvailableAppointmentDatesDto.class));
        when(appointmentAvailabilityService.getAvailableAppointmentDates()).thenReturn(expectedList);

        ResponseEntity<Object> response = controller.getAvailableAppointmentDates(token);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedList, response.getBody());
        verify(accessManager).enforceOwnershipBasedAccess(token);
        verify(appointmentAvailabilityService).getAvailableAppointmentDates();
    }

    @Test
    void getAvailableAppointmentDates_invalidOrServiceError_returnsCorrectResponses() {
        // --- Case 1: Unauthorized token (SecurityException) ---
        String badToken = "unauthorized-token";
        doThrow(new SecurityException("Access denied")).when(accessManager).enforceOwnershipBasedAccess(badToken);

        ResponseEntity<Object> unauthorizedResponse = controller.getAvailableAppointmentDates(badToken);
        assertEquals(401, unauthorizedResponse.getStatusCode().value());
        assertEquals("Access denied", unauthorizedResponse.getBody());
        verify(accessManager).enforceOwnershipBasedAccess(badToken);
        verifyNoInteractions(appointmentAvailabilityService);

        // --- Case 2: Service throws generic exception ---
        String goodToken = "valid-token";
        when(accessManager.enforceOwnershipBasedAccess(goodToken)).thenReturn(new User(null, null, null));
        when(appointmentAvailabilityService.getAvailableAppointmentDates()).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> badRequestResponse = controller.getAvailableAppointmentDates(goodToken);
        assertEquals(400, badRequestResponse.getStatusCode().value());
        assertEquals("Unexpected error", badRequestResponse.getBody());
        verify(accessManager).enforceOwnershipBasedAccess(goodToken);
        verify(appointmentAvailabilityService).getAvailableAppointmentDates();
    }

    @Test
    void getAvailableAppointmentSlots_validToken_returnsOk() {
        String token = "valid-token";
        LocalDate date = LocalDate.now();
        List<Integer> doctorIds = List.of(1, 2);
        List<AvailableAppointmentSlotsDto> expectedSlots = List.of(mock(AvailableAppointmentSlotsDto.class));

        when(appointmentAvailabilityService.getAvailableAppointmentSlots(date, doctorIds)).thenReturn(expectedSlots);

        ResponseEntity<Object> response = controller.getAvailableAppointmentSlots(token, date, doctorIds);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedSlots, response.getBody());
        verify(accessManager).enforceOwnershipBasedAccess(token);
        verify(appointmentAvailabilityService).getAvailableAppointmentSlots(date, doctorIds);
    }

    @Test
    void getAvailableAppointmentSlots_invalidOrServiceError_returnsCorrectResponses() {
        LocalDate date = LocalDate.now();
        List<Integer> doctorIds = List.of(1);

        // Case 1: SecurityException -> 401
        String badToken = "unauthorized-token";
        doThrow(new SecurityException("Access denied")).when(accessManager).enforceOwnershipBasedAccess(badToken);

        ResponseEntity<Object> unauthorized = controller.getAvailableAppointmentSlots(badToken, date, doctorIds);
        assertEquals(401, unauthorized.getStatusCode().value());
        assertEquals("Access denied", unauthorized.getBody());
        verify(accessManager).enforceOwnershipBasedAccess(badToken);
        verifyNoInteractions(appointmentAvailabilityService);

        // Case 2: Generic Exception -> 400
        String goodToken = "valid-token";
        // enforceOwnershipBasedAccess returns a non-void value, so return a User instead of using doNothing()
        when(accessManager.enforceOwnershipBasedAccess(goodToken)).thenReturn(new User(null, null, null));
        when(appointmentAvailabilityService.getAvailableAppointmentSlots(date, doctorIds))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> badRequest = controller.getAvailableAppointmentSlots(goodToken, date, doctorIds);
        assertEquals(400, badRequest.getStatusCode().value());
        assertEquals("Unexpected error", badRequest.getBody());
        verify(accessManager).enforceOwnershipBasedAccess(goodToken);
        verify(appointmentAvailabilityService).getAvailableAppointmentSlots(date, doctorIds);
    }
}