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
     * Finds notifications by recipient ID.
     *
     * @param recipientId the ID of the recipient
     * @return a list of notifications associated with the recipient
     */
    List<Notification> findByRecipientId(Integer recipientId);

    /**
     * Finds notifications by recipient group.
     *
     * @param recipientGroup the group of recipients
     * @return a list of notifications associated with the recipient group
     */
    List<Notification> findByRecipientGroup(String recipientGroup);
}