package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.model.Role;
import edu.psgv.healpointbackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AccessManagerTest {

    private Datastore datastore;
    private AccessManager accessManager;

    @BeforeEach
    void setUp() {
        datastore = mock(Datastore.class);
        accessManager = new AccessManager(datastore);
    }

    private User mockUser(String email, String roleDescription) {
        Role role = new Role();
        String hashedPassword = "hashedPassword";
        role.setDescription(roleDescription);
        User user = new User(email, hashedPassword, role);
        return user;
    }

    @Test
    void enforceRoleBasedAccess_AdminAllowed() {
        User admin = mockUser("admin@example.com", "ADMIN");
        when(datastore.getUserByToken("valid-token")).thenReturn(admin);

        assertDoesNotThrow(() ->
                accessManager.enforceRoleBasedAccess(accessManager.getSaGroup(), "valid-token")
        );
    }

    @Test
    void enforceRoleBasedAccess_DoctorDeniedInSaGroup() {
        User doctor = mockUser("doctor@example.com", "DOCTOR");
        when(datastore.getUserByToken("token-doctor")).thenReturn(doctor);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                accessManager.enforceRoleBasedAccess(accessManager.getSaGroup(), "token-doctor")
        );
        assertEquals("Access denied: You do not have the required permissions.", ex.getMessage());
    }

    @Test
    void enforceRoleBasedAccess_NullUserDenied() {
        when(datastore.getUserByToken("invalid-token")).thenReturn(null);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                accessManager.enforceRoleBasedAccess(accessManager.getEmployeeGroup(), "invalid-token")
        );
        assertEquals("Access denied: You do not have the required permissions.", ex.getMessage());
    }

    @Test
    void enforceOwnershipBasedAccess_ReturnsEmailForValidUser() {
        User patient = mockUser("patient@example.com", "PATIENT");
        when(datastore.getUserByToken("token-patient")).thenReturn(patient);

        String email = accessManager.enforceOwnershipBasedAccess("token-patient");
        assertEquals("patient@example.com", email);
    }

    @Test
    void enforceOwnershipBasedAccess_NullUserDenied() {
        when(datastore.getUserByToken("bad-token")).thenReturn(null);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                accessManager.enforceOwnershipBasedAccess("bad-token")
        );
        assertEquals("Access denied: User not authenticated or authorized.", ex.getMessage());
    }
}
