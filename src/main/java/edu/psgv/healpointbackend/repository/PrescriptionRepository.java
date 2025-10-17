package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Repository interface for Prescription entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {
    /**
     * Finds a prescription by patient ID.
     *
     * @param patientId the ID of the patient
     * @return an Optional containing the found Prescription, or empty if not found
     */
    Optional<Prescription> findByPatientId(Integer patientId);
}
