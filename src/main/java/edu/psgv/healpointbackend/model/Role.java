package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


/**
 * Represents a role in the system.
 * Maps to the "Roles" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Entity
@Table(name = "Roles", schema = "dbo")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Integer id;

    @Getter
    @Setter
    @Column(name = "RoleDescription", nullable = false, length = 32)
    private String description;

    // getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}