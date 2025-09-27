package edu.psgv.healpointbackend.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PatientProfile extends Patient {
    public PatientProfile(Patient patient, String email, String role) {
        this.email = email;
        this.role = role;
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
}
