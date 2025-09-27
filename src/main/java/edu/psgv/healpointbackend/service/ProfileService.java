package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.dto.UpdateProfileDto;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final Datastore datastore;

    public ProfileService(UserRepository userRepository, PatientRepository patientRepository,
                          DoctorRepository doctorRepository, Datastore datastore) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.datastore = datastore;
    }

    public ResponseEntity<Object> getUserProfile(String email, String targetRole) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public String updateUserProfile(UpdateProfileDto dto) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    private void updatePatientProfile(User user, UpdateProfileDto dto) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    private void updateDoctorProfile(User user, UpdateProfileDto dto) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
