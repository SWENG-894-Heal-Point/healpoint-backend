package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Repository interface for Patient entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    /**
     * Finds a patient by their ID.
     *
     * @param id the ID of the patient
     * @return an Optional containing the found patient or empty if not found
     */
    Optional<Patient> findById(Integer id);
}

