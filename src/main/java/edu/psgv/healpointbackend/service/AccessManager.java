package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service responsible for enforcing access control based on user roles and ownership.
 * Provides methods to check if a user has the required permissions or ownership for an operation.
 *
 * @author Mahfuzur Rahman
 */
@Service
public class AccessManager {
    private final Datastore datastore;

    @Getter
    private final List<String> doctorOnlyGroup;

    @Getter
    private final List<String> saGroup;

    @Getter
    private final List<String> employeeGroup;

    /**
     * Constructs an AccessManager with the provided datastore.
     * Initializes role groups for access control.
     *
     * @param datastore the datastore used to retrieve user information
     */
    public AccessManager(Datastore datastore) {
        this.datastore = datastore;
        this.doctorOnlyGroup = List.of(Roles.DOCTOR);
        this.saGroup = List.of(Roles.ADMIN, Roles.SUPPORT_STAFF);
        this.employeeGroup = List.of(Roles.ADMIN, Roles.SUPPORT_STAFF, Roles.DOCTOR);
    }

    /**
     * Enforces role-based access control for the given access group and token.
     * Throws SecurityException if the user does not have the required role.
     *
     * @param accessGroup list of allowed role descriptions
     * @param token       authentication token of the requestor
     * @throws SecurityException if access is denied
     */
    public User enforceRoleBasedAccess(List<String> accessGroup, String token) {
        LOGGER.info("Enforcing role-based access for against allowedRoles={}", accessGroup);

        User requestor = datastore.getUserByToken(token);
        if (requestor == null || !accessGroup.contains(requestor.getRole().getDescription().toUpperCase())) {
            LOGGER.warn("Access denied for user: {}", requestor != null ? requestor.getEmail() : "unknown");
            throw new SecurityException("Access denied: You do not have the required permissions.");
        }
        return requestor;
    }

    /**
     * Enforces ownership-based access control for the given token.
     * Throws SecurityException if the user is not authenticated or authorized.
     *
     * @param token authentication token of the requestor
     * @return the email of the authenticated user
     * @throws SecurityException if access is denied
     */
    public User enforceOwnershipBasedAccess(String token) {
        LOGGER.info("Enforcing ownership-based access");

        User requestor = datastore.getUserByToken(token);
        if (requestor == null) {
            LOGGER.warn("Access denied: User not authenticated or authorized.");
            throw new SecurityException("Access denied: User not authenticated or authorized.");
        }

        LOGGER.info("Access granted for user: {}", requestor.getEmail());
        return requestor;
    }
}
