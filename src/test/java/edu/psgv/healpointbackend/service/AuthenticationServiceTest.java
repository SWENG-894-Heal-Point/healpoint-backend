package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.dto.AuthenticationFormDto;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.repository.UserRepository;
import edu.psgv.healpointbackend.utilities.JwtUtil;
import edu.psgv.healpointbackend.utilities.PasswordUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private Datastore fakeDatastore;
    @Mock
    private JwtUtil jwtUtil;
    private MockedStatic<PasswordUtils> passwordUtilsStatic;

    private AuthenticationService authService;

    private final String EMAIL = "foo@bar.com";
    private final String RAW_PW = "secret";
    private final String HASHED_PW = "hashed-secret";
    private final String TOKEN = "jwt-token-123";

    @BeforeEach
    void setUp() {
        // static mock for PasswordUtils only
        passwordUtilsStatic = mockStatic(PasswordUtils.class);

        // inject BOTH fakeDatastore and jwtUtil
        authService = new AuthenticationService(userRepository, jwtUtil, fakeDatastore);
    }

    @AfterEach
    void tearDown() {
        passwordUtilsStatic.close();
    }

    @Test
    void authenticateUser_emailNotFound_returns401() {
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.empty());

        AuthenticationFormDto form = new AuthenticationFormDto();
        form.setEmail(EMAIL);
        form.setPassword(RAW_PW);

        ResponseEntity<String> resp = authService.authenticateUser(form);

        assertEquals(401, resp.getStatusCode().value());
        assertEquals("No account associated with this email address.",
                resp.getBody());
    }

    @Test
    void authenticateUser_inactiveUser_returns403() {
        User u = new User(EMAIL, HASHED_PW, null);
        u.setIsActive(false);
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(u));

        AuthenticationFormDto form = new AuthenticationFormDto();
        form.setEmail(EMAIL);
        form.setPassword(RAW_PW);

        ResponseEntity<String> resp = authService.authenticateUser(form);

        assertEquals(403, resp.getStatusCode().value());
        assertEquals("This account is inactive. Please contact support.",
                resp.getBody());
    }

    @Test
    void authenticateUser_badPassword_returns401() {
        User u = new User(EMAIL, HASHED_PW, null);
        u.setIsActive(true);
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(u));

        passwordUtilsStatic
                .when(() -> PasswordUtils.verifyPassword(RAW_PW, HASHED_PW))
                .thenReturn(false);

        AuthenticationFormDto form = new AuthenticationFormDto();
        form.setEmail(EMAIL);
        form.setPassword(RAW_PW);

        ResponseEntity<String> resp = authService.authenticateUser(form);

        assertEquals(401, resp.getStatusCode().value());
        assertEquals("Incorrect password. Please try again.", resp.getBody());
    }

    @Test
    void authenticateUser_existingDatastoreUser_returnsStoredToken() {
        User fromRepo = new User(EMAIL, HASHED_PW, null);
        fromRepo.setIsActive(true);
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(fromRepo));

        passwordUtilsStatic
                .when(() -> PasswordUtils.verifyPassword(RAW_PW, HASHED_PW))
                .thenReturn(true);

        User stored = new User(EMAIL, HASHED_PW, null);
        stored.setToken(TOKEN);
        when(fakeDatastore.getUserByEmail(EMAIL)).thenReturn(stored);

        AuthenticationFormDto form = new AuthenticationFormDto();
        form.setEmail(EMAIL);
        form.setPassword(RAW_PW);

        ResponseEntity<String> resp = authService.authenticateUser(form);

        assertEquals(200, resp.getStatusCode().value());
        assertEquals(TOKEN, resp.getBody());
        verify(jwtUtil, never()).generateToken(anyString());
        verify(fakeDatastore, never()).addUser(any());
    }

    @Test
    void authenticateUser_newUser_generatesAndStoresToken() {
        User fromRepo = new User(EMAIL, HASHED_PW, null);
        fromRepo.setIsActive(true);
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(fromRepo));

        passwordUtilsStatic
                .when(() -> PasswordUtils.verifyPassword(RAW_PW, HASHED_PW))
                .thenReturn(true);

        when(fakeDatastore.getUserByEmail(EMAIL)).thenReturn(null);
        when(jwtUtil.generateToken(EMAIL)).thenReturn(TOKEN);

        AuthenticationFormDto form = new AuthenticationFormDto();
        form.setEmail(EMAIL);
        form.setPassword(RAW_PW);

        ResponseEntity<String> resp = authService.authenticateUser(form);

        assertEquals(200, resp.getStatusCode().value());
        assertEquals(TOKEN, resp.getBody());

        verify(jwtUtil).generateToken(EMAIL);
        verify(fakeDatastore).addUser(argThat(u ->
                EMAIL.equals(u.getEmail()) &&
                        TOKEN.equals(u.getToken())
        ));
    }

    @Test
    void authenticateUser_repositoryThrowsException_returns500() {
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenThrow(new RuntimeException("db is down"));

        AuthenticationFormDto form = new AuthenticationFormDto();
        form.setEmail(EMAIL);
        form.setPassword(RAW_PW);

        ResponseEntity<String> resp = authService.authenticateUser(form);

        assertEquals(500, resp.getStatusCode().value());
        assertTrue(resp.getBody().contains("db is down"));
    }

    @Test
    void logoutUser_existingToken_removesFromDatastore() {
        User u = new User(EMAIL, HASHED_PW, null);
        u.setToken(TOKEN);
        when(fakeDatastore.getUserByToken(TOKEN)).thenReturn(u);

        authService.logoutUser(TOKEN);
        verify(fakeDatastore).removeUser(u);
    }

    @Test
    void logoutUser_nonexistentToken_doesNothing() {
        when(fakeDatastore.getUserByToken(TOKEN)).thenReturn(null);
        authService.logoutUser(TOKEN);
        verify(fakeDatastore, never()).removeUser(any());
    }
}
