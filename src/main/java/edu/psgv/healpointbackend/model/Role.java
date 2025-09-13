package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Roles", schema = "dbo")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Integer id;

    @Column(name = "RoleDescription", nullable = false, length = 32)
    private String description;

    // getters/setters ...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}