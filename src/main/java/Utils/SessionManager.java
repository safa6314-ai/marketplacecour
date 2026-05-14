package Utils;

import Entites.User;

public class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User User) {
        currentUser = User;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }
}


