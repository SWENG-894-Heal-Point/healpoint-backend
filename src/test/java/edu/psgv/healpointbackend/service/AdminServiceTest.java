package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.UserDto;
import edu.psgv.healpointbackend.model.DoctorProfile;
import edu.psgv.healpointbackend.model.PatientProfile;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminServiceTest extends AbstractTestBase {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileGetService profileGetService;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getAllUsers_validData_returnsSortedUserList() {
        DoctorProfile doctor = mockDoctorProfile("Doctor", "doc@test.com");
        doctor.setId(1);
        doctor.setDateOfBirth(LocalDate.of(1980, 5, 15));
        doctor.setGender("Male");

        PatientProfile patient = mockPatientProfile("Patient", "pat@test.com");
        patient.setId(2);
        patient.setDateOfBirth(LocalDate.of(1990, 8, 20));
        patient.setGender("Female");

        User internal = mockUser("support@test.com", Roles.SUPPORT_STAFF, 3);

        when(profileGetService.getAllDoctors()).thenReturn(new ArrayList<>(List.of(doctor)));
        when(profileGetService.getAllPatients()).thenReturn(new ArrayList<>(List.of(patient)));
        when(userRepository.findAll()).thenReturn(List.of(internal));

        List<UserDto> result = adminService.getAllUsers();

        assertEquals(3, result.size());

        assertEquals(1, result.get(0).id());
        assertEquals(2, result.get(1).id());
        assertEquals(3, result.get(2).id());
    }

    @Test
    void accountDeactivation_validId_updatesActiveStatus() {
        int userId = 10;
        boolean isActive = false;

        User user = mockUser("pat@test.com", Roles.PATIENT, userId);
        user.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.accountDeactivation(userId, isActive);

        assertFalse(user.getIsActive());
        verify(userRepository).save(user);
    }

    @Test
    void accountDeactivation_invalidId_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> adminService.accountDeactivation(99, true));

        assertEquals("User not found.", ex.getMessage());
    }
}
