package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.dto.AppointmentCountDto;
import edu.psgv.healpointbackend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


/**
 * Repository interface for Appointment entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    /**
     * Retrieves appointment counts grouped by doctor and appointment date.
     *
     * @return a list of AppointmentCountDto containing doctor ID, appointment date, and count of appointments
     */
    @Query("""
                SELECT
                    a.doctor.id AS doctorId,
                    a.appointmentDate AS date,
                    COUNT(a) AS appointmentCount
                FROM Appointment a
                GROUP BY a.doctor.id, a.appointmentDate
                ORDER BY a.appointmentDate, a.doctor.id
            """)
    List<AppointmentCountDto> getAppointmentCounts();

    /**
     * Finds appointments by doctor ID and appointment date.
     *
     * @param doctorId        the ID of the doctor
     * @param appointmentDate the date of the appointments
     * @return a list of Appointment entities matching the criteria
     */
    List<Appointment> findByDoctorIdAndAppointmentDate(Integer doctorId, LocalDate appointmentDate);
}
