package edu.psgv.healpointbackend.common.state;

import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.utilities.IoHelper;

import java.util.ArrayList;
import java.util.List;

public class Datastore {
    private static final Datastore INSTANCE = new Datastore();
    private static List<User> onlineUsers;

    private Datastore() {
        onlineUsers = new ArrayList<>();
    }

    public static Datastore getInstance() {
        return INSTANCE;
    }

    public void addUser(User user) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeUser(User user) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void clearOnlineUsers() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public User getUserByEmail(String email) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public User getUserByToken(String token) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}