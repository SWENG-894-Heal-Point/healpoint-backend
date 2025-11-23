package edu.psgv.healpointbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.ScheduleDto;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.model.WorkDay;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.ScheduleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class ScheduleControllerTest extends AbstractTestBase {

    @Mock
    private ScheduleManager scheduleManager;

    @Mock
    private AccessManager accessManager;

    @InjectMocks
    private ScheduleController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDoctorSchedule_validInput_returnsSchedule() {
        String token = "validToken";
        int doctorId = 5;

        WorkDay wd1 = WorkDay.builder().id(1).build();
        WorkDay wd2 = WorkDay.builder().id(2).build();

        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 10);
        when(accessManager.enforceRoleBasedAccess(any(), eq(token))).thenReturn(mockUser);
        when(scheduleManager.getWorkDaysByDoctorId(doctorId)).thenReturn(List.of(wd1, wd2));

        ResponseEntity<Object> response = controller.getDoctorSchedule(token, doctorId);

        assertEquals(200, response.getStatusCode().value());
        List<WorkDay> result = (List<WorkDay>) response.getBody();
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
    }

    @Test
    void getDoctorSchedule_exceptions_handledProperly() {
        int doctorId = 5;

        // SecurityException path
        String badToken = "badToken";
        when(accessManager.enforceRoleBasedAccess(any(), eq(badToken))).thenThrow(new SecurityException("Invalid token"));

        ResponseEntity<Object> securityResponse = controller.getDoctorSchedule(badToken, doctorId);
        assertEquals(401, securityResponse.getStatusCode().value());
        assertEquals("Invalid token", securityResponse.getBody());

        // Unexpected exception path
        String validToken = "validToken";
        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 10);
        when(accessManager.enforceRoleBasedAccess(any(), eq(validToken))).thenReturn(mockUser);
        when(scheduleManager.getWorkDaysByDoctorId(doctorId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> errorResponse = controller.getDoctorSchedule(validToken, doctorId);

        assertEquals(400, errorResponse.getStatusCode().value());
        assertEquals("Unexpected error", errorResponse.getBody());

        // IllegalArgumentException path
        doThrow(new IllegalArgumentException("Invalid doctor id")).when(scheduleManager).getWorkDaysByDoctorId(doctorId + 1);

        ResponseEntity<Object> illegalResponse = controller.getDoctorSchedule(validToken, doctorId + 1);

        assertEquals(400, illegalResponse.getStatusCode().value());
        assertEquals("Invalid doctor id", illegalResponse.getBody());
    }

    @Test
    void upsertSchedule_validInput_returnsSuccess() {
        String token = "validToken";
        int doctorId = 8;

        WorkDay wd = WorkDay.builder().id(100).build();
        ScheduleDto dto = mockScheduleDto(token, doctorId, List.of(wd));

        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 10);
        when(accessManager.enforceRoleBasedAccess(any(), eq(token))).thenReturn(mockUser);

        ResponseEntity<Object> response = controller.upsertSchedule(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Schedule upserted successfully.", response.getBody());
    }

    @Test
    void upsertSchedule_exceptions_handledProperly() throws JsonProcessingException {
        // SecurityException
        ScheduleDto dto1 = mockScheduleDto("badToken", 5, List.of());
        when(accessManager.enforceRoleBasedAccess(any(), eq("badToken"))).thenThrow(new SecurityException("Invalid token"));

        ResponseEntity<Object> securityResponse = controller.upsertSchedule(dto1);
        assertEquals(401, securityResponse.getStatusCode().value());
        assertEquals("Invalid token", securityResponse.getBody());

        // Unexpected exception
        String validToken = "validToken";
        ScheduleDto dto2 = mockScheduleDto(validToken, 6, List.of());

        User mockUser = mockUser("admin@test.com", Roles.ADMIN, 10);
        when(accessManager.enforceRoleBasedAccess(any(), eq(validToken))).thenReturn(mockUser);
        doThrow(new RuntimeException("Unexpected error")).when(scheduleManager).upsertWorkDays(6, List.of());

        ResponseEntity<Object> errorResponse = controller.upsertSchedule(dto2);
        assertEquals(400, errorResponse.getStatusCode().value());
        assertEquals("Unexpected error", errorResponse.getBody());

        // IllegalArgumentException
        ScheduleDto dto3 = mockScheduleDto(validToken, 9, List.of());

        doThrow(new IllegalArgumentException("Invalid work days")).when(scheduleManager).upsertWorkDays(9, List.of());

        ResponseEntity<Object> illegalResponse = controller.upsertSchedule(dto3);
        assertEquals(400, illegalResponse.getStatusCode().value());
        assertEquals("Invalid work days", illegalResponse.getBody());
    }

    private ScheduleDto mockScheduleDto(String token, int doctorId, List<WorkDay> workDays) {
        ScheduleDto dto = new ScheduleDto();
        dto.setToken(token);
        dto.setDoctorId(doctorId);
        dto.setWorkDays(workDays);
        return dto;
    }
}
