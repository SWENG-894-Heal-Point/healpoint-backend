package edu.psgv.healpointbackend.common.state;

import edu.psgv.healpointbackend.model.Role;
import edu.psgv.healpointbackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DatastoreTest {
    private Datastore datastore;
    private Role role;
    private User existingUser;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setDescription("Test User");
        existingUser = new User("existing.user@email.com", "Test@123", role);
        existingUser.setToken("JwtToken123");

        datastore = new Datastore();
        datastore.clearOnlineUsers();
        datastore.addUser(existingUser);
    }

    @Test
    void addUser_userIsAddedToOnlineUsers() {
        assertNull(datastore.getUserByEmail("new.user@email.com"));

        User newUser = new User("new.user@email.com", "Test@123", role);
        newUser.setToken("JwtToken456");
        datastore.addUser(newUser);
        assertEquals(newUser, datastore.getUserByEmail("new.user@email.com"));
    }

    @Test
    void addUser_invalidEmail_throwsIllegalArgumentException() {
        User newUser = new User(null, "Test@123", role);
        newUser.setToken("JwtToken456");
        assertThrows(IllegalArgumentException.class, () -> datastore.addUser(newUser));

        User anotherUser = new User(" ", "Test@123", role);
        newUser.setToken("JwtToken456");
        assertThrows(IllegalArgumentException.class, () -> datastore.addUser(anotherUser));
    }

    @Test
    void addUser_invalidToken_throwsIllegalArgumentException() {
        User newUser = new User("new.user@email.com", "Test@123", role);
        assertThrows(IllegalArgumentException.class, () -> datastore.addUser(newUser));

        newUser.setToken(" ");
        assertThrows(IllegalArgumentException.class, () -> datastore.addUser(newUser));
    }

    @Test
    void removeUser_userIsRemovedFromOnlineUsers() {
        assertEquals(existingUser, datastore.getUserByEmail("existing.user@email.com"));
        datastore.removeUser(existingUser);
        assertNull(datastore.getUserByEmail("existing.user@email.com"));
    }

    @Test
    void clearOnlineUsers_allUsersAreRemoved() {
        User newUser = new User("new.user@email.com", "Test@123", role);
        newUser.setToken("JwtToken789");

        datastore.addUser(newUser);
        assertNotNull(datastore.getUserByEmail("new.user@email.com"));
        assertNotNull(datastore.getUserByEmail("existing.user@email.com"));

        datastore.clearOnlineUsers();
        assertNull(datastore.getUserByEmail("new.user@email.com"));
        assertNull(datastore.getUserByEmail("existing.user@email.com"));
    }

    @Test
    void getUserByEmail_userNotFound_returnsNull() {
        assertNull(datastore.getUserByEmail("unknown.user@email.com"));
    }

    @Test
    void getUserByEmail_returnsUserWithCaseInsensitiveMatchingEmail() {
        assertNotNull(datastore.getUserByEmail("existing.user@email.com"));
        assertNotNull(datastore.getUserByEmail("Existing.User@email.com"));
        assertEquals(existingUser, datastore.getUserByEmail("Existing.User@email.com"));
    }

    @Test
    void getUserByToken_returnsUserWithMatchingToken() {
        User newUser = new User("new.user@email.com", "Test@123", role);
        newUser.setToken("sampleToken123");
        datastore.addUser(newUser);

        assertNotNull(datastore.getUserByToken("sampleToken123"));
        assertEquals(newUser, datastore.getUserByToken("sampleToken123"));
    }

    @Test
    void getUserByToken_returnsNullIfTokenNotFound() {
        assertNull(datastore.getUserByToken("notoken"));
    }
}