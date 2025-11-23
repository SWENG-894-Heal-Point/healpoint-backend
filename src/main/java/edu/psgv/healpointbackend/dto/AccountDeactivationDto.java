package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO for account deactivation or activation, includes target user ID and active status.
 * Extends TokenDto to include a token for authentication.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class AccountDeactivationDto extends TokenDto {
    @NotNull
    Integer targetUserId;

    @NotNull
    Boolean isActive;
}
