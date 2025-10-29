package edu.psgv.healpointbackend;

import edu.psgv.healpointbackend.dto.NewPasswordDto;
import edu.psgv.healpointbackend.dto.RefillMedicationsDto;
import edu.psgv.healpointbackend.model.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public abstract class AbstractTestBase {
    protected User mockUser(String email) {
        return mockUser(email, "user");
    }

    protected User mockUser(String email, String roleDescription) {
        Role role = new Role();
        String hashedPassword = "hashedPassword";
        role.setDescription(roleDescription);
        return new User(email, hashedPassword, role);
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

    protected Patient mockPatient(Integer id, String firstName, String lastName) {
        return Patient.builder().id(id).firstName(firstName).lastName(lastName).build();
    }

    protected PatientProfile mockPatientProfile(String firstName, String email) {
        Patient patient = mockPatient(null, firstName, "Example");
        return new PatientProfile(patient, email, "patient");
    }

    protected Doctor mockDoctor(Integer id, String firstName, String lastName) {
        return Doctor.builder().id(id).firstName(firstName).lastName(lastName).build();
    }

    protected DoctorProfile mockDoctorProfile(String firstName, String email) {
        Doctor doctor = mockDoctor(null, firstName, "Example");
        return new DoctorProfile(doctor, email, "doctor");
    }

    protected NewPasswordDto mockPasswordDto(String token, String oldPassword, String newPassword, String confirmPassword) {
        NewPasswordDto dto = new NewPasswordDto();
        dto.setToken(token);
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmNewPassword(confirmPassword);
        return dto;
    }

    protected RefillMedicationsDto mockRefillMedicationsDto(String token, List<String> medications) {
        RefillMedicationsDto dto = new RefillMedicationsDto();
        dto.setToken(token);
        dto.setMedications(medications);
        return dto;
    }
}
