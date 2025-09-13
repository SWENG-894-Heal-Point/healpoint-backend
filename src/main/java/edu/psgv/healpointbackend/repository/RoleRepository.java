package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Repository interface for Role entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * Finds a role by its description.
     *
     * @param description the description of the role
     * @return an Optional containing the found role, or empty if not found
     */
    Optional<Role> findByDescription(String description);
}