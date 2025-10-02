package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.common.validation.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO for changing password, includes old password, new password, and confirmation of new password.
 * Extends TokenDto to include a token for authentication.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class NewPasswordDto extends TokenDto {
    @NotBlank
    String oldPassword;

    @PasswordPolicy(message = "Your password must be at least 8 characters long and include a symbol, uppercase letter, lowercase letter, and number.")
    String newPassword;

    @NotBlank
    String confirmNewPassword;
}
