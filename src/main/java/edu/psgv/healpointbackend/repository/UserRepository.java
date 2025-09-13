package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Repository interface for User entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Finds a user by their email.
     *
     * @param email the email of the user
     * @return an Optional containing the found user, or empty if not found
     */
    Optional<User> findByEmail(String email);
}