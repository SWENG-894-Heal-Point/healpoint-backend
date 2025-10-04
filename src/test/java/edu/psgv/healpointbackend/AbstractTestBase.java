package edu.psgv.healpointbackend;

import edu.psgv.healpointbackend.dto.NewPasswordDto;
import edu.psgv.healpointbackend.model.Role;
import edu.psgv.healpointbackend.model.User;
import org.springframework.test.util.ReflectionTestUtils;

public abstract class AbstractTestBase {
    protected User mockUser(String email, String roleDescription) {
        Role role = new Role();
        String hashedPassword = "hashedPassword";
        role.setDescription(roleDescription);
        User user = new User(email, hashedPassword, role);
        return user;
    }

    protected User mockUser(Integer id, String email, String roleDesc) {
        Role role = new Role();
        String hashedPassword = "hashedPassword";
        role.setDescription(roleDesc);
        User user = new User(email, hashedPassword, role);

        if (id != null) {
            ReflectionTestUtils.setField(user, "id", id);
        }

        return user;
    }

    protected NewPasswordDto mockPasswordDto(String token, String oldPassword, String newPassword, String confirmPassword) {
        NewPasswordDto dto = new NewPasswordDto();
        dto.setToken(token);
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmNewPassword(confirmPassword);
        return dto;
    }
}
