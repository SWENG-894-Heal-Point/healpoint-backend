package edu.psgv.healpointbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.dto.NewPasswordDto;
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

    private NewPasswordDto mockPasswordDto(String token, String oldPassword, String newPassword, String confirmPassword) {
        NewPasswordDto dto = new NewPasswordDto();
        dto.setToken(token);
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmNewPassword(confirmPassword);
        return dto;
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
    void getUserProfile_validToken_returnsProfile() {
        TokenDto request = new TokenDto();
        request.setToken("token");

        when(accessManager.enforceOwnershipBasedAccess("token")).thenReturn("patient@example.com");
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

        when(accessManager.enforceOwnershipBasedAccess("token")).thenReturn("doctor@example.com");
        when(profileService.updateUserProfile(request, "doctor@example.com")).thenReturn("doctor@example.com");
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

        when(accessManager.enforceOwnershipBasedAccess("bad-token")).thenThrow(new SecurityException("Access denied"));

        ResponseEntity<Object> response = controller.getUserProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void getUserProfile_serviceThrowsRuntimeException_returns400() {
        TokenDto request = new TokenDto();
        request.setToken("token");

        when(accessManager.enforceOwnershipBasedAccess("token")).thenReturn("user@example.com");
        when(profileService.getUserProfile("user@example.com", null)).thenThrow(new RuntimeException("DB failure"));

        ResponseEntity<Object> response = controller.getUserProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("DB failure", response.getBody());
    }

    @Test
    void updateUserProfile_accessDenied_returns401() {
        UpdateProfileDto request = new UpdateProfileDto();
        request.setToken("bad-token");
        request.setEmail("user@example.com");

        when(accessManager.enforceOwnershipBasedAccess("bad-token")).thenThrow(new SecurityException("Unauthorized"));

        ResponseEntity<Object> response = controller.updateUserProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Unauthorized", response.getBody());
    }

    @Test
    void updateUserProfile_serviceThrowsRuntimeException_returns400() {
        UpdateProfileDto request = new UpdateProfileDto();
        request.setToken("token");
        request.setEmail("user@example.com");

        when(accessManager.enforceOwnershipBasedAccess("token")).thenReturn("user@example.com");
        when(profileService.updateUserProfile(request, "user@example.com")).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> response = controller.updateUserProfile(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void updateMyPassword_validInput_returnsOkResponse() {
        // Arrange
        NewPasswordDto dto = mockPasswordDto("token", "oldPass", "newPass", "newPass");
        when(accessManager.enforceOwnershipBasedAccess("token")).thenReturn("test@example.com");

        // Act
        ResponseEntity<String> response = controller.updateMyPassword(dto);

        // Assert
        verify(profileService).updatePassword(dto);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Password updated successfully.", response.getBody());
    }

    @Test
    void updateMyPassword_invalidInputs_returnsProperResponses() {
        NewPasswordDto dto = mockPasswordDto("token", "oldPass", "newPass", "newPass");
        NewPasswordDto dto2 = mockPasswordDto("token2", "oldPass", "newPass", "newPass");
        NewPasswordDto dto3 = mockPasswordDto("token3", "oldPass", "newPass", "newPass");

        // --- Case 1: SecurityException (unauthorized) ---
        when(accessManager.enforceOwnershipBasedAccess("token")).thenThrow(new SecurityException("Unauthorized"));
        ResponseEntity<String> res1 = controller.updateMyPassword(dto);
        assertEquals(401, res1.getStatusCode().value());
        assertEquals("Unauthorized", res1.getBody());

        // --- Case 2: IllegalArgumentException (bad request) ---
        when(accessManager.enforceOwnershipBasedAccess("token2")).thenReturn("test@example.com");
        doThrow(new IllegalArgumentException("Mismatch")).when(profileService).updatePassword(dto2);
        ResponseEntity<String> res2 = controller.updateMyPassword(dto2);
        assertEquals(400, res2.getStatusCode().value());
        assertEquals("Mismatch", res2.getBody());

        // --- Case 3: Generic Exception (unexpected error) ---
        doThrow(new RuntimeException("Unexpected")).when(profileService).updatePassword(dto3);
        ResponseEntity<String> res3 = controller.updateMyPassword(dto3);
        assertEquals(400, res3.getStatusCode().value());
        assertEquals("Unexpected", res3.getBody());
    }

    @Test
    void getDoctorProfile_validEmail_returnsProfile() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileService.getUserProfile("doctor@example.com", Roles.DOCTOR)).thenReturn(ResponseEntity.ok("DoctorProfile"));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("DoctorProfile", response.getBody());
    }

    @Test
    void getDoctorProfile_serviceThrowsSecurityException_returns401() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileService.getUserProfile("doctor@example.com", Roles.DOCTOR)).thenThrow(new SecurityException("Forbidden"));

        ResponseEntity<Object> response = controller.getDoctorProfile(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Forbidden", response.getBody());
    }

    @Test
    void getDoctorProfile_serviceThrowsRuntimeException_returns400() {
        UserLookupDto request = new UserLookupDto();
        request.setEmail("doctor@example.com");

        when(profileService.getUserProfile("doctor@example.com", Roles.DOCTOR)).thenThrow(new RuntimeException("Unexpected error"));

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
        when(profileService.getUserProfile("patient@example.com", Roles.PATIENT)).thenThrow(new RuntimeException("Unexpected failure"));

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
