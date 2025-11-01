package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;


/**
 * Data Transfer Object for looking up a user by email, includes an authentication token.
 *
 * @author Mahfuzur Rahman
 */
public class UserLookupDto extends TokenDto {
    @Getter
    @Setter
    @Email
    String email;
}