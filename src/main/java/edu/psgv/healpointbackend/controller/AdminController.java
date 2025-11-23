package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.AccountDeactivationDto;
import edu.psgv.healpointbackend.dto.NewPasswordDto;
import edu.psgv.healpointbackend.dto.UserDto;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.AdminService;
import edu.psgv.healpointbackend.service.ProfileUpdateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for handling admin-related user management operations.
 * <p>
 * Provides endpoints for retrieving all users and updating users' accounts.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class AdminController {
    private final AdminService adminService;
    private final ProfileUpdateService profileUpdateService;
    private final AccessManager accessManager;

    /**
     * Constructs an AdminController with required services.
     *
     * @param adminService  service for admin operations
     * @param accessManager service for access control
     */
    public AdminController(AdminService adminService, ProfileUpdateService profileUpdateService, AccessManager accessManager) {
        this.adminService = adminService;
        this.profileUpdateService = profileUpdateService;
        this.accessManager = accessManager;
    }

    /**
     * Endpoint to retrieve all users in the system.
     *
     * @param token authentication token of the requester
     * @return ResponseEntity with the list of all users or error message
     */
    @GetMapping("/api/admin/get-all-users")
    public ResponseEntity<Object> getAllUsers(@RequestParam String token) {
        LOGGER.info("Received request to get all users");
        try {
            User requestor = accessManager.enforceRoleBasedAccess(accessManager.getSaGroup(), token);
            LOGGER.info("Role-based access granted for user: {}, role: {}", requestor.getEmail(), requestor.getRole().getDescription());

            List<UserDto> users = adminService.getAllUsers();
            LOGGER.info("Successfully retrieved all users");
            return ResponseEntity.ok(users);
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized attempt to get all users", e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving all users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to update a user's password by an admin.
     *
     * @param request DTO containing the target user ID, new password, and authentication token
     * @return ResponseEntity indicating success or failure of the operation
     */
    @PostMapping("/api/admin/update-user-password")
    public ResponseEntity<Object> updateUserPassword(@Valid @RequestBody NewPasswordDto request) {
        int targetUserId = request.getTargetUserId();
        LOGGER.info("Received request to update user password for userId={}", targetUserId);
        try {
            User requestor = accessManager.enforceRoleBasedAccess(accessManager.getSaGroup(), request.getToken());
            LOGGER.info("Role-based access granted for user: {}, role: {}", requestor.getEmail(), requestor.getRole().getDescription());

            profileUpdateService.adminUpdatePassword(request);
            LOGGER.info("Successfully updated password for userId={}", targetUserId);
            return ResponseEntity.ok("Password updated successfully.");
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized attempt to update user password for userId={}", targetUserId, e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid input for updating password for userId={}: {}", targetUserId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error updating password for userId={}: {}", targetUserId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to update a user's account status (active/inactive) by an admin.
     *
     * @param request DTO containing the target user ID, desired active status, and authentication token
     * @return ResponseEntity indicating success or failure of the operation
     */
    @PostMapping("/api/admin/account-status")
    public ResponseEntity<Object> updateUserStatus(@Valid @RequestBody AccountDeactivationDto request) {
        int targetUserId = request.getTargetUserId();
        LOGGER.info("Received request to update account status for userId={}", targetUserId);
        try {
            User requestor = accessManager.enforceRoleBasedAccess(accessManager.getSaGroup(), request.getToken());
            LOGGER.info("Role-based access granted for user: {}, role: {}", requestor.getEmail(), requestor.getRole().getDescription());

            adminService.accountDeactivation(targetUserId, request.getIsActive());
            LOGGER.info("Successfully updated account status for userId={}", targetUserId);
            return ResponseEntity.ok("Account status updated successfully.");
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized attempt to update account status for userId={}", targetUserId, e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid input for updating account status for userId={}: {}", targetUserId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error updating account status for userId={}: {}", targetUserId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
