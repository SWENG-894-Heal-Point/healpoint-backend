package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

public class UserLookupDto extends TokenDto {
    @Getter
    @Setter
    @Email
    String email;
}