package edu.psgv.healpointbackend.model;

import lombok.Getter;
import lombok.Setter;


/**
 * Represents a patient's profile, extending the Patient entity.
 * Includes additional fields for email and role.
 * This class is not mapped to a database table.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class PatientProfile extends Patient {

    /**
     * Constructs a PatientProfile from a Patient entity, email, and role.
     *
     * @param patient the Patient entity
     * @param email   the email of the patient
     * @param role    the role of the patient
     */
    public PatientProfile(Patient patient, String email, String role, Boolean isActive) {
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.setId(patient.getId());
        this.setFirstName(patient.getFirstName());
        this.setLastName(patient.getLastName());
        this.setGender(patient.getGender());
        this.setDateOfBirth(patient.getDateOfBirth());
        this.setPhone(patient.getPhone());
        this.setStreetAddress(patient.getStreetAddress());
        this.setCity(patient.getCity());
        this.setState(patient.getState());
        this.setZipCode(patient.getZipCode());
        this.setInsuranceProvider(patient.getInsuranceProvider());
        this.setInsuranceId(patient.getInsuranceId());
    }

    private String email;
    private String role;
    private Boolean isActive;
}
