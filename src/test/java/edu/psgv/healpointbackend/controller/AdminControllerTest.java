package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.AccountDeactivationDto;
import edu.psgv.healpointbackend.dto.NewPasswordDto;
import edu.psgv.healpointbackend.dto.UserDto;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.AdminService;
import edu.psgv.healpointbackend.service.ProfileUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class AdminControllerTest extends AbstractTestBase {
    @Mock
    private AdminService adminService;
    @Mock
    private ProfileUpdateService profileUpdateService;
    @Mock
    private AccessManager accessManager;
    @InjectMocks
    private AdminController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_validInput_returnsAllUsers() {
        String token = "validToken";
        UserDto dto1 = mockUserDto(1, Roles.ADMIN, "Admin");
        UserDto dto2 = mockUserDto(2, Roles.DOCTOR, "Doctor");
        UserDto dto3 = mockUserDto(3, Roles.PATIENT, "Patient");
        UserDto dto4 = mockUserDto(4, Roles.SUPPORT_STAFF, "Support");

        when(accessManager.enforceRoleBasedAccess(any(), eq(token))).thenReturn(mockUser("mock@test.com"));
        when(adminService.getAllUsers()).thenReturn(Arrays.asList(dto1, dto2, dto3, dto4));

        ResponseEntity<Object> response = controller.getAllUsers(token);

        assertEquals(200, response.getStatusCode().value());
        List<UserDto> users = (List<UserDto>) response.getBody();

        assertEquals(4, users.size());
        assertEquals("Admin", users.get(0).firstName());
        assertEquals("Doctor", users.get(1).firstName());
        assertEquals("Patient", users.get(2).firstName());
        assertEquals("Support", users.get(3).firstName());
    }

    @Test
    void getAllUsers_exceptions_handledProperly() {
        // Case 1: SecurityException
        String badToken = "badToken";
        when(accessManager.enforceRoleBasedAccess(any(), eq(badToken))).thenThrow(new SecurityException("Invalid token"));

        ResponseEntity<Object> unauthorizedResponse = controller.getAllUsers(badToken);

        assertEquals(401, unauthorizedResponse.getStatusCode().value());
        assertEquals("Invalid token", unauthorizedResponse.getBody());

        // Case 2: Unexpected Exception
        String validToken = "validToken";
        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 1);

        when(accessManager.enforceRoleBasedAccess(any(), eq(validToken))).thenReturn(mockUser);
        when(adminService.getAllUsers()).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> errorResponse = controller.getAllUsers(validToken);

        assertEquals(400, errorResponse.getStatusCode().value());
        assertEquals("Unexpected error", errorResponse.getBody());
    }

    @Test
    void updateUserPassword_validRequest_returnsSuccessResponse() {
        String token = "validToken";
        NewPasswordDto dto = mockNewPasswordDto(token, 5, "newPass");

        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 1);
        when(accessManager.enforceRoleBasedAccess(any(), eq(token))).thenReturn(mockUser);

        ResponseEntity<Object> response = controller.updateUserPassword(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Password updated successfully.", response.getBody());
    }

    @Test
    void updateUserPassword_exceptions_handledProperly() {
        // Case 1: SecurityException
        String badToken = "badToken";
        NewPasswordDto dto1 = mockNewPasswordDto(badToken, 2, "newPass");

        when(accessManager.enforceRoleBasedAccess(any(), eq(badToken))).thenThrow(new SecurityException("Invalid token"));

        ResponseEntity<Object> unauthorizedResponse = controller.updateUserPassword(dto1);

        assertEquals(401, unauthorizedResponse.getStatusCode().value());
        assertEquals("Invalid token", unauthorizedResponse.getBody());

        // Case 2: Unexpected Exception
        String validToken = "validToken";
        NewPasswordDto dto2 = mockNewPasswordDto(validToken, 2, "newPass");
        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 1);

        when(accessManager.enforceRoleBasedAccess(any(), eq(validToken))).thenReturn(mockUser);
        doThrow(new RuntimeException("Unexpected error")).when(profileUpdateService).adminUpdatePassword(dto2);
        ResponseEntity<Object> errorResponse = controller.updateUserPassword(dto2);

        assertEquals(400, errorResponse.getStatusCode().value());
        assertEquals("Unexpected error", errorResponse.getBody());

        // Cse 3: IllegalArgumentException
        NewPasswordDto dto3 = mockNewPasswordDto(validToken, 3, "newPass");
        doThrow(new IllegalArgumentException("Invalid Id")).when(profileUpdateService).adminUpdatePassword(dto3);

        ResponseEntity<Object> illegalArgResponse = controller.updateUserPassword(dto3);

        assertEquals(400, illegalArgResponse.getStatusCode().value());
        assertEquals("Invalid Id", illegalArgResponse.getBody());
    }

    @Test
    void updateUserStatus_validRequest_returnsSuccessResponse() {
        String token = "validToken";
        AccountDeactivationDto dto = mockAccountDeactivationDto(token, 8, true);

        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 1);
        when(accessManager.enforceRoleBasedAccess(any(), eq(token))).thenReturn(mockUser);

        ResponseEntity<Object> response = controller.updateUserStatus(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Account status updated successfully.", response.getBody());
    }

    @Test
    void updateUserStatus_exceptions_handledProperly() {
        // Case 1: SecurityException
        String badToken = "badToken";
        AccountDeactivationDto dto1 = mockAccountDeactivationDto(badToken, 2, false);

        when(accessManager.enforceRoleBasedAccess(any(), eq(badToken))).thenThrow(new SecurityException("Invalid token"));

        ResponseEntity<Object> unauthorizedResponse = controller.updateUserStatus(dto1);

        assertEquals(401, unauthorizedResponse.getStatusCode().value());
        assertEquals("Invalid token", unauthorizedResponse.getBody());

        // Case 2: Unexpected Exception
        String validToken = "validToken";
        AccountDeactivationDto dto2 = mockAccountDeactivationDto(validToken, 2, false);
        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 1);

        when(accessManager.enforceRoleBasedAccess(any(), eq(validToken))).thenReturn(mockUser);
        doThrow(new RuntimeException("Unexpected error")).when(adminService).accountDeactivation(2, false);
        ResponseEntity<Object> errorResponse = controller.updateUserStatus(dto2);

        assertEquals(400, errorResponse.getStatusCode().value());
        assertEquals("Unexpected error", errorResponse.getBody());

        // Cse 3: IllegalArgumentException
        AccountDeactivationDto dto3 = mockAccountDeactivationDto(validToken, 3, true);
        doThrow(new IllegalArgumentException("Invalid Id")).when(adminService).accountDeactivation(3, true);

        ResponseEntity<Object> illegalArgResponse = controller.updateUserStatus(dto3);

        assertEquals(400, illegalArgResponse.getStatusCode().value());
        assertEquals("Invalid Id", illegalArgResponse.getBody());
    }

    private AccountDeactivationDto mockAccountDeactivationDto(String token, int targetUserId, boolean active) {
        AccountDeactivationDto dto = new AccountDeactivationDto();
        dto.setToken(token);
        dto.setTargetUserId(targetUserId);
        dto.setIsActive(active);
        return dto;
    }

    private NewPasswordDto mockNewPasswordDto(String token, int targetUserId, String password) {
        NewPasswordDto dto = new NewPasswordDto();
        dto.setToken(token);
        dto.setTargetUserId(targetUserId);
        dto.setNewPassword(password);
        dto.setConfirmNewPassword(password);
        dto.setOldPassword("admin-reset");
        return dto;
    }
}