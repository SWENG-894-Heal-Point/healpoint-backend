package edu.psgv.healpointbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.dto.TokenDto;
import edu.psgv.healpointbackend.dto.UpdateProfileDto;
import edu.psgv.healpointbackend.dto.UserLookupDto;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    @Mock
    private ProfileService profileService;
    @Mock
    private AccessManager accessManager;
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProfileController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
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
    void getPatientProfile_doctorRoleAllowed_returnsProfile() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");
        request.setToken("token");

        doNothing().when(accessManager).enforceRoleBasedAccess(anyList(), eq("token"));
        when(profileService.getUserProfile("doctor@example.com", Roles.PATIENT))
                .thenReturn(ResponseEntity.ok("DoctorProfile"));

        ResponseEntity<Object> response = controller.getPatientProfile(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("DoctorProfile", response.getBody());
    }

    @Test
    void updateUserProfile_invalidInput_returns400() {
        UpdateProfileDto request = new UpdateProfileDto();
        request.setToken("token");
        request.setEmail("bad@example.com");

        when(accessManager.enforceOwnershipBasedAccess("token"))
                .thenReturn("bad@example.com");
        doThrow(new RuntimeException("Validation failed"))
                .when(profileService).updateUserProfile(request);

        ResponseEntity<Object> response = controller.updateUserProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Validation failed", response.getBody());
    }

    @Test
    void getUserProfile_validToken_returnsProfile() {
        TokenDto request = new TokenDto();
        request.setToken("token");

        when(accessManager.enforceOwnershipBasedAccess("token"))
                .thenReturn("patient@example.com");
        when(profileService.getUserProfile("patient@example.com", null))
                .thenReturn(ResponseEntity.ok("PatientProfile"));

        ResponseEntity<Object> response = controller.getUserProfile(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("PatientProfile", response.getBody());
    }

    @Test
    void updateUserProfile_attemptToChangeName_ignoredByDto() {
        String jsonPayload = """
                {
                  "token": "token",
                  "email": "doctor@example.com",
                  "firstName": "Changed"
                }
                """;

        assertThrows(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class, () -> {
            objectMapper.readValue(jsonPayload, UpdateProfileDto.class);
        });
    }

    @Test
    void updateUserProfile_attemptToChangeDob_returns404() {
        String jsonPayload = """
                {
                  "token": "token",
                  "email": "patient@example.com",
                  "dateOfBirth": "2003-09-12"
                }
                """;

        assertThrows(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class, () -> {
            objectMapper.readValue(jsonPayload, UpdateProfileDto.class);
        });
    }

    @Test
    void updateUserProfile_validPhoneUpdate_returnsUpdatedProfile() {
        UpdateProfileDto request = new UpdateProfileDto();
        request.setToken("token");
        request.setEmail("doctor@example.com");
        request.setPhone("987-654-3210");

        when(accessManager.enforceOwnershipBasedAccess("token"))
                .thenReturn("doctor@example.com");
        when(profileService.updateUserProfile(request))
                .thenReturn("doctor@example.com");
        when(profileService.getUserProfile("doctor@example.com", null))
                .thenReturn(ResponseEntity.ok("UpdatedDoctorProfile"));

        ResponseEntity<Object> response = controller.updateUserProfile(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("UpdatedDoctorProfile", response.getBody());
    }

    @Test
    void getUserProfile_invalidToken_returns401() {
        TokenDto request = new TokenDto();
        request.setToken("bad-token");

        when(accessManager.enforceOwnershipBasedAccess("bad-token"))
                .thenThrow(new SecurityException("Access denied"));

        ResponseEntity<Object> response = controller.getUserProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void getUserProfile_serviceThrowsRuntimeException_returns400() {
        TokenDto request = new TokenDto();
        request.setToken("token");

        when(accessManager.enforceOwnershipBasedAccess("token"))
                .thenReturn("user@example.com");
        when(profileService.getUserProfile("user@example.com", null))
                .thenThrow(new RuntimeException("DB failure"));

        ResponseEntity<Object> response = controller.getUserProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("DB failure", response.getBody());
    }

    @Test
    void updateUserProfile_accessDenied_returns401() {
        UpdateProfileDto request = new UpdateProfileDto();
        request.setToken("bad-token");
        request.setEmail("user@example.com");

        when(accessManager.enforceOwnershipBasedAccess("bad-token"))
                .thenThrow(new SecurityException("Unauthorized"));

        ResponseEntity<Object> response = controller.updateUserProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Unauthorized", response.getBody());
    }

    @Test
    void updateUserProfile_serviceThrowsRuntimeException_returns400() {
        UpdateProfileDto request = new UpdateProfileDto();
        request.setToken("token");
        request.setEmail("user@example.com");

        when(accessManager.enforceOwnershipBasedAccess("token"))
                .thenReturn("user@example.com");
        when(profileService.updateUserProfile(request))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> response = controller.updateUserProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void getDoctorProfile_validEmail_returnsProfile() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileService.getUserProfile("doctor@example.com", Roles.DOCTOR))
                .thenReturn(ResponseEntity.ok("DoctorProfile"));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("DoctorProfile", response.getBody());
    }

    @Test
    void getDoctorProfile_serviceThrowsSecurityException_returns401() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileService.getUserProfile("doctor@example.com", Roles.DOCTOR))
                .thenThrow(new SecurityException("Forbidden"));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Forbidden", response.getBody());
    }

    @Test
    void getDoctorProfile_serviceThrowsRuntimeException_returns400() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileService.getUserProfile("doctor@example.com", Roles.DOCTOR))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void getPatientProfile_serviceThrowsRuntimeException_returns400() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("patient@example.com");
        request.setToken("token");

        doNothing().when(accessManager).enforceRoleBasedAccess(anyList(), eq("token"));
        when(profileService.getUserProfile("patient@example.com", Roles.PATIENT))
                .thenThrow(new RuntimeException("Unexpected failure"));

        ResponseEntity<Object> response = controller.getPatientProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Unexpected failure", response.getBody());
    }

    @Test
    void getDoctorProfile_patientUserRequestedAsDoctor_returns401() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("patient@example.com");

        when(profileService.getUserProfile("patient@example.com", Roles.DOCTOR))
                .thenReturn(ResponseEntity.status(401).body("No DOCTOR account associated with this email address."));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("No DOCTOR account associated with this email address.", response.getBody());
    }

    @Test
    void getPatientProfile_doctorUserRequestedAsPatient_returns401() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");
        request.setToken("token");

        doNothing().when(accessManager).enforceRoleBasedAccess(anyList(), eq("token"));
        when(profileService.getUserProfile("doctor@example.com", Roles.PATIENT))
                .thenReturn(ResponseEntity.status(401).body("No PATIENT account associated with this email address."));

        ResponseEntity<Object> response = controller.getPatientProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("No PATIENT account associated with this email address.", response.getBody());
    }
}
