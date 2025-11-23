package edu.psgv.healpointbackend.model;

import lombok.Getter;
import lombok.Setter;


/**
 * Represents a doctor's profile, extending the Doctor entity.
 * Includes additional fields for email and role.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class DoctorProfile extends Doctor {

    /**
     * Constructs a DoctorProfile from a Doctor entity, email, and role.
     *
     * @param doctor the Doctor entity
     * @param email  the email of the doctor
     * @param role   the role of the doctor
     */
    public DoctorProfile(Doctor doctor, String email, String role, Boolean isActive) {
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.setId(doctor.getId());
        this.setFirstName(doctor.getFirstName());
        this.setLastName(doctor.getLastName());
        this.setDateOfBirth(doctor.getDateOfBirth());
        this.setGender(doctor.getGender());
        this.setPhone(doctor.getPhone());
        this.setMedicalDegree(doctor.getMedicalDegree());
        this.setSpecialty(doctor.getSpecialty());
        this.setNpiNumber(doctor.getNpiNumber());
        this.setExperience(doctor.getExperience());
        this.setLanguages(doctor.getLanguages());
    }

    private String email;
    private String role;
    private Boolean isActive;
}
