package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkDayRepository extends JpaRepository<WorkDay, Integer> {
    List<WorkDay> findByDoctorId(Integer doctorId);
    Optional<WorkDay> findByDoctorIdAndDayName(Integer doctorId, String dayName);
}
