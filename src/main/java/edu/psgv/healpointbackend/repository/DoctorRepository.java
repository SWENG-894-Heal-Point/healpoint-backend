package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Repository interface for Doctor entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    /**
     * Finds a doctor by their unique ID.
     *
     * @param id the ID of the doctor to find
     * @return an Optional containing the found Doctor, or empty if not found
     */
    Optional<Doctor> findById(Integer id);
}
