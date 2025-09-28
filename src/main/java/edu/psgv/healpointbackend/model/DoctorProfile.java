package edu.psgv.healpointbackend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorProfile extends Doctor {
    public DoctorProfile(Doctor doctor, String email, String role) {
        this.email = email;
        this.role = role;
        this.setId(doctor.getId());
        this.setFirstName(doctor.getFirstName());
        this.setLastName(doctor.getLastName());
        this.setDateOfBirth(doctor.getDateOfBirth());
        this.setGender(doctor.getGender());
        this.setPhone(doctor.getPhone());
        this.setMedicalDegree(doctor.getMedicalDegree());
        this.setSpecialty(doctor.getSpecialty());
        this.setNpiNumber(doctor.getNpiNumber());
        this.setYearsOfExperience(doctor.getYearsOfExperience());
        this.setLanguages(doctor.getLanguages());
    }

    private String email;
    private String role;
}
