package edu.psgv.healpointbackend.service;


import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.dto.AuthenticationFormDto;
import edu.psgv.healpointbackend.repository.UserRepository;

import edu.psgv.healpointbackend.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Datastore datastore;

    @Autowired
    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = new JwtUtil();
        this.datastore = Datastore.getInstance();
    }

    protected AuthenticationService(UserRepository userRepository, JwtUtil jwtUtil, Datastore datastore) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.datastore = datastore;
    }

    public ResponseEntity<String> authenticateUser(AuthenticationFormDto authenticationFormDto) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void logoutUser(String token) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
