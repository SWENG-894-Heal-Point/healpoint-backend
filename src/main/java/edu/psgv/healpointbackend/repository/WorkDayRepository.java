package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


/**
 * Repository interface for managing WorkDay entities.
 * Provides methods to perform CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
public interface WorkDayRepository extends JpaRepository<WorkDay, Integer> {
    /**
     * Finds all WorkDay entries for a specific doctor by their ID.
     *
     * @param doctorId the ID of the doctor
     * @return a list of WorkDay entries for the specified doctor
     */
    List<WorkDay> findByDoctorId(Integer doctorId);

    /**
     * Finds a WorkDay entry for a specific doctor by their ID and day name.
     *
     * @param doctorId the ID of the doctor
     * @param dayName  the name of the day (e.g., "Monday", "Tuesday")
     * @return an Optional containing the WorkDay entry if found, or empty if not found
     */
    Optional<WorkDay> findByDoctorIdAndDayName(Integer doctorId, String dayName);
}
