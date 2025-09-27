package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.common.validation.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;

public class NewPasswordDto extends TokenDto {
    @NotBlank
    String confirmOldPassword;

    @PasswordPolicy(message = "Your password must be at least 8 characters long and include a symbol, uppercase letter, lowercase letter, and number.")
    String newPassword;

    @NotBlank
    String confirmNewPassword;
}
