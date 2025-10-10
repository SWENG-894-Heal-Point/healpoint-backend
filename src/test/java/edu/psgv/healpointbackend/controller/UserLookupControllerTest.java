package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.UserLookupDto;
import edu.psgv.healpointbackend.model.PatientProfile;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.ProfileGetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserLookupControllerTest extends AbstractTestBase {

    @Mock
    private ProfileGetService profileGetService;
    @Mock
    private AccessManager accessManager;

    @InjectMocks
    private UserLookupController controller;

    @Test
    void getDoctorProfile_validEmail_returnsProfile() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileGetService.getUserProfile("doctor@example.com", Roles.DOCTOR)).thenReturn(ResponseEntity.ok("DoctorProfile"));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("DoctorProfile", response.getBody());
    }

    @Test
    void getDoctorProfile_serviceThrowsSecurityException_returns401() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileGetService.getUserProfile("doctor@example.com", Roles.DOCTOR)).thenThrow(new SecurityException("Forbidden"));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Forbidden", response.getBody());
    }

    @Test
    void getDoctorProfile_serviceThrowsRuntimeException_returns400() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileGetService.getUserProfile("doctor@example.com", Roles.DOCTOR)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Unexpected error", response.getBody());
    }


    @Test
    void getDoctorProfile_patientUserRequestedAsDoctor_returns401() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("patient@example.com");

        when(profileGetService.getUserProfile("patient@example.com", Roles.DOCTOR))
                .thenReturn(ResponseEntity.status(401).body("No DOCTOR account associated with this email address."));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("No DOCTOR account associated with this email address.", response.getBody());
    }

    @Test
    void getPatientProfile_doctorRoleAllowed_returnsProfile() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");
        request.setToken("token");

        when(accessManager.enforceRoleBasedAccess(anyList(), eq("token"))).thenReturn(mock(User.class));
        when(profileGetService.getUserProfile("doctor@example.com", Roles.PATIENT))
                .thenReturn(ResponseEntity.ok("DoctorProfile"));

        ResponseEntity<Object> response = controller.getPatientProfile(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("DoctorProfile", response.getBody());
    }

    @Test
    void getPatientProfile_patientRoleNotAllowed_returns401() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("patient@example.com");
        request.setToken("token");

        doThrow(new SecurityException("Access denied"))
                .when(accessManager).enforceRoleBasedAccess(anyList(), eq("token"));

        ResponseEntity<Object> response = controller.getPatientProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void getPatientProfile_doctorUserRequestedAsPatient_returns401() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");
        request.setToken("token");

        when(accessManager.enforceRoleBasedAccess(anyList(), eq("token"))).thenReturn(mock(User.class));
        when(profileGetService.getUserProfile("doctor@example.com", Roles.PATIENT))
                .thenReturn(ResponseEntity.status(401).body("No PATIENT account associated with this email address."));

        ResponseEntity<Object> response = controller.getPatientProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("No PATIENT account associated with this email address.", response.getBody());
    }

    @Test
    void getPatientProfile_serviceThrowsRuntimeException_returns400() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("patient@example.com");
        request.setToken("token");

        when(accessManager.enforceRoleBasedAccess(anyList(), eq("token"))).thenReturn(mock(User.class));
        when(profileGetService.getUserProfile("patient@example.com", Roles.PATIENT)).thenThrow(new RuntimeException("Unexpected failure"));

        ResponseEntity<Object> response = controller.getPatientProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Unexpected failure", response.getBody());
    }

    @Test
    void getAllPatients_validDoctorToken_returnsOkResponse() {
        // Arrange
        PatientProfile p1 = mockPatientProfile("John", "john@example.com");
        PatientProfile p2 = mockPatientProfile("Bob", "bob@example.com");

        String token = "validToken";
        ArrayList<PatientProfile> mockProfiles = new ArrayList<>(List.of(p1, p2));
        when(profileGetService.getAllPatients()).thenReturn(mockProfiles);
        when(accessManager.getEmployeeGroup()).thenReturn(List.of("doctor"));
        when(accessManager.enforceRoleBasedAccess(anyList(), eq(token))).thenReturn(mock(User.class));

        // Act
        ResponseEntity<Object> response = controller.getAllPatients(token);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockProfiles, response.getBody());
        verify(accessManager).enforceRoleBasedAccess(any(), eq(token));
        verify(profileGetService).getAllPatients();
    }

    @Test
    void getAllPatients_invalidOrErrorCases_returnsProperErrorResponses() {
        String token = "invalidToken";

        // --- Case 1: SecurityException (unauthorized)
        doThrow(new SecurityException("Unauthorized")).when(accessManager).enforceRoleBasedAccess(any(), eq(token));
        when(accessManager.getEmployeeGroup()).thenReturn(List.of("patient"));

        ResponseEntity<Object> responseUnauthorized = controller.getAllPatients(token);
        assertEquals(401, responseUnauthorized.getStatusCode().value());
        assertEquals("Unauthorized", responseUnauthorized.getBody());

        // --- Case 2: Generic Exception (unexpected)
        reset(accessManager); // reset mocks for next scenario
        when(accessManager.getEmployeeGroup()).thenReturn(List.of("doctor"));
        doThrow(new RuntimeException("Unexpected error"))
                .when(accessManager).enforceRoleBasedAccess(any(), eq("errorToken"));

        ResponseEntity<Object> responseGeneric = controller.getAllPatients("errorToken");
        assertEquals(400, responseGeneric.getStatusCode().value());
        assertEquals("Unexpected error", responseGeneric.getBody());
    }
}
