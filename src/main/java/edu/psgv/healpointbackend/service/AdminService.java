package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.dto.UserDto;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service class for admin-related user management.
 * <p>
 * Provides methods to retrieve all users in the system.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final ProfileGetService profileGetService;

    /**
     * Constructs a new AdminService with required repositories and services.
     *
     * @param userRepository    the repository for user operations
     * @param profileGetService the service for fetching profile information
     */
    public AdminService(UserRepository userRepository, ProfileGetService profileGetService) {
        this.userRepository = userRepository;
        this.profileGetService = profileGetService;
    }

    /**
     * Retrieves all users in the system.
     * <p>
     * Combines users from different roles including doctors, patients, and other user types.
     * </p>
     *
     * @return a list of UserDto objects representing all users
     */
    public List<UserDto> getAllUsers() {
        LOGGER.info("Fetching all users for admin");
        List<UserDto> users = new ArrayList<>();

        users.addAll(profileGetService.getAllDoctors().stream().map(d -> new UserDto(
                d.getId(), d.getEmail(), d.getRole(), d.getIsActive(), d.getFirstName(), d.getLastName(),
                Optional.of(d.getDateOfBirth()), d.getGender())).toList());

        users.addAll(profileGetService.getAllPatients().stream().map(p -> new UserDto(
                p.getId(), p.getEmail(), p.getRole(), p.getIsActive(), p.getFirstName(), p.getLastName(),
                Optional.of(p.getDateOfBirth()), p.getGender())).toList());

        users.addAll(
                userRepository.findAll().stream().filter(u -> {
                    String role = u.getRole().getDescription();
                    return !role.equals(Roles.DOCTOR) && !role.equals(Roles.PATIENT);
                }).map(u -> new UserDto(u.getId(), u.getEmail(), u.getRole().getDescription(), u.getIsActive(), "Internal",
                        "User", Optional.empty(), "unknown")).toList()
        );

        LOGGER.debug("Total users fetched for admin: {}", users.size());
        users.sort(Comparator.comparing(UserDto::id));
        return users;
    }

    /**
     * Deactivates or activates a user account based on the provided user ID.
     *
     * @param userId   the ID of the user whose account is to be deactivated or activated
     * @param isActive true to activate the account, false to deactivate
     * @throws IllegalArgumentException if the user with the given ID does not exist
     */
    public void accountDeactivation(int userId, boolean isActive) {
        LOGGER.info("Deactivating account for user ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
            LOGGER.error("User not found for account deactivation, user ID: {}", userId);
            return new IllegalArgumentException("User not found.");
        });

        user.setIsActive(isActive);
        userRepository.save(user);
        LOGGER.info("User (ID: {}) account is {}.", userId, isActive ? "activated" : "deactivated");
    }
}
