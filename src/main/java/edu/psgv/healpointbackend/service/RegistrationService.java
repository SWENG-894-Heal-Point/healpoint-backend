package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.dto.RegistrationFormDto;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public RegistrationService(UserRepository userRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public Boolean checkIfUserExists(String email)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Boolean registerUser(RegistrationFormDto request)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
