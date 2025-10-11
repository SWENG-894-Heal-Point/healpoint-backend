package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.PrescriptionDto;
import edu.psgv.healpointbackend.model.Prescription;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class PrescriptionControllerTest extends AbstractTestBase {
    @Mock
    private PrescriptionService prescriptionService;

    @Mock
    private AccessManager accessManager;

    @InjectMocks
    private PrescriptionController controller;

    private PrescriptionDto dto;
    private Prescription mockPrescription;
    private List<String> allowedRoles;
    private List<String> doctorOnlyRole;

    @BeforeEach
    void setUp() {
        dto = new PrescriptionDto();
        dto.setPatientId(1);
        dto.setToken("validToken");

        mockPrescription = new Prescription();
        doctorOnlyRole = List.of("Doctor");
        allowedRoles = List.of("Doctor", "Admin", "Support_Staff");
    }

    @Test
    void getPrescription_validAccess_returnsPrescription() {
        // Role-based access case
        when(accessManager.getEmployeeGroup()).thenReturn(allowedRoles);
        when(accessManager.enforceRoleBasedAccess(allowedRoles, "validToken")).thenReturn(mockUser("doctor@email.com"));
        when(prescriptionService.getPrescription(1)).thenReturn(mockPrescription);

        ResponseEntity<Object> response = controller.getPrescription("validToken", 1);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mockPrescription, response.getBody());
        verify(accessManager).enforceRoleBasedAccess(allowedRoles, "validToken");
    }

    @Test
    void getPrescription_patientIdZero_enforcesOwnershipAccess() {
        // Ownership-based access case
        User mockUser = mockUser(9, "test@email.com", "patient");

        when(accessManager.enforceOwnershipBasedAccess("token123")).thenReturn(mockUser);
        when(prescriptionService.getPrescription(9)).thenReturn(mockPrescription);

        ResponseEntity<Object> response = controller.getPrescription("token123", 0);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mockPrescription, response.getBody());
        verify(accessManager).enforceOwnershipBasedAccess("token123");
    }

    @Test
    void getPrescription_allExceptions_returnsProperErrorResponses() {
        when(accessManager.getEmployeeGroup()).thenReturn(allowedRoles);

        // 1. SecurityException path
        when(accessManager.enforceRoleBasedAccess(allowedRoles, "badToken")).thenThrow(new SecurityException("Access denied"));

        ResponseEntity<Object> secResponse = controller.getPrescription("badToken", 5);
        assertEquals(401, secResponse.getStatusCode().value());
        assertEquals("Access denied", secResponse.getBody());

        // 2. Generic Exception path
        when(accessManager.enforceRoleBasedAccess(allowedRoles, "okToken")).thenReturn(mockUser("ok@email.com"));
        when(prescriptionService.getPrescription(5)).thenThrow(new RuntimeException("DB failure"));

        ResponseEntity<Object> exResponse = controller.getPrescription("okToken", 5);
        assertEquals(400, exResponse.getStatusCode().value());
        assertEquals("DB failure", exResponse.getBody());
    }

    @Test
    void upsertPrescription_authorizedUser_returnsOkResponse() {
        when(accessManager.getDoctorOnlyGroup()).thenReturn(doctorOnlyRole);
        when(accessManager.enforceRoleBasedAccess(doctorOnlyRole, "validToken")).thenReturn(mockUser("ok@email.com"));
        when(prescriptionService.getPrescription(1)).thenReturn(mockPrescription);

        ResponseEntity<Object> response = controller.upsertPrescription(dto);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mockPrescription, response.getBody());
        verify(prescriptionService).upsertPrescription(dto);
    }

    @Test
    void upsertPrescription_securityException_returnsUnauthorized() {
        when(accessManager.getDoctorOnlyGroup()).thenReturn(doctorOnlyRole);
        when(accessManager.enforceRoleBasedAccess(doctorOnlyRole, "invalidToken"))
                .thenThrow(new SecurityException("Unauthorized"));

        PrescriptionDto dto2 = new PrescriptionDto();
        dto2.setPatientId(1);
        dto2.setToken("invalidToken");

        ResponseEntity<Object> response = controller.upsertPrescription(dto2);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Unauthorized", response.getBody());
        verify(prescriptionService, never()).upsertPrescription(any());
    }

    @Test
    void upsertPrescription_genericException_returnsSaveFailed() {
        when(accessManager.getDoctorOnlyGroup()).thenReturn(doctorOnlyRole);
        when(accessManager.enforceRoleBasedAccess(doctorOnlyRole, "validToken")).thenReturn(mockUser("doctor@email.com"));
        doThrow(new RuntimeException("Save failed")).when(prescriptionService).upsertPrescription(dto);

        ResponseEntity<Object> response = controller.upsertPrescription(dto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Save failed", response.getBody());
    }

    @Test
    void upsertPrescription_missingRequiredFields_returnsBadRequest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        String jsonPayload = "{\"instruction\": \"Take one daily\"}";

        mockMvc.perform(post("/api/create-or-update-prescription")
                        .contentType("application/json")
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }
}