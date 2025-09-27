package edu.psgv.healpointbackend.common.state;

import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.utilities.IoHelper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Singleton class that manages the state of online users in the application.
 * Provides methods to add, remove, clear, and retrieve users by email or token.
 *
 * @author Mahfuzur Rahman
 */
@Component
public class Datastore {
    private final List<User> onlineUsers = new CopyOnWriteArrayList<>();

    /**
     * Adds a user to the list of online users.
     * Throws IllegalArgumentException if the user's email or token is null or empty.
     *
     * @param user the User to add
     */
    public void addUser(User user) {
        if (IoHelper.isNullOrEmpty(user.getEmail()) || IoHelper.isNullOrEmpty(user.getToken())) {
            throw new IllegalArgumentException("User email and token must not be null or empty.");
        }
        onlineUsers.add(user);
    }

    /**
     * Removes a user from the list of online users.
     *
     * @param user the User to remove
     */
    public void removeUser(User user) {
        onlineUsers.remove(user);
    }

    /**
     * Clears the list of online users.
     */
    public void clearOnlineUsers() {
        onlineUsers.clear();
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address to search for
     * @return the User with the specified email, or null if not found
     */
    public User getUserByEmail(String email) {
        if (!IoHelper.isNullOrEmpty(email)) {
            for (User user : onlineUsers) {
                if (user.getEmail().equalsIgnoreCase(email)) {
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a user by their authentication token.
     *
     * @param token the token to search for
     * @return the User with the specified token, or null if not found
     */
    public User getUserByToken(String token) {
        if (!IoHelper.isNullOrEmpty(token)) {
            for (User user : onlineUsers) {
                if (user.getToken().equals(token)) {
                    return user;
                }
            }
        }
        return null;
    }
}