package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.EmployeeAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for EmployeeAccount entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
public interface EmployeeAccountRepository extends JpaRepository<EmployeeAccount, Integer> {
    /**
     * Finds an employee account by email.
     *
     * @param email the email of the employee account
     * @return an Optional containing the found EmployeeAccount, or empty if not found
     */
    Optional<EmployeeAccount> findByEmailIgnoreCase(String email);
}
