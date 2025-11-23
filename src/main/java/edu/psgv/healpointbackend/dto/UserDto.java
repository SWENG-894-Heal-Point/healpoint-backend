package edu.psgv.healpointbackend.dto;

import java.time.LocalDate;
import java.util.Optional;


/**
 * Data Transfer Object representing a user in the system.
 *
 * @param id          the unique identifier of the user
 * @param email       the email address of the user
 * @param role        the role of the user (e.g., Doctor, Patient)
 * @param firstName   optional first name of the user
 * @param lastName    optional last name of the user
 * @param dateOfBirth optional date of birth of the user
 * @param gender      optional gender of the user
 * @author Mahfuzur Rahman
 */
public record UserDto(Integer id, String email, String role, Boolean isActive, String firstName, String lastName,
                      Optional<LocalDate> dateOfBirth, String gender) {
}
