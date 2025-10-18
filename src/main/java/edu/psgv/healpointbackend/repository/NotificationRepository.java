package edu.psgv.healpointbackend.repository;

import edu.psgv.healpointbackend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Repository interface for Notification entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.
 *
 * @author Mahfuzur Rahman
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    /**
     * Finds notifications by user ID.
     *
     * @param userId the ID of the user
     * @return a list of notifications associated with the user
     */
    List<Notification> findByUserId(Integer userId);
}