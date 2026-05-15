package Utils;

import Entites.User;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static User currentUser;
    private static final List<Runnable> sessionListeners = new ArrayList<>();

    private SessionManager() {
    }

    public static void setCurrentUser(User User) {
        currentUser = User;
        notifySessionListeners();
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
        notifySessionListeners();
    }

    public static void addSessionListener(Runnable listener) {
        if (listener != null && !sessionListeners.contains(listener)) {
            sessionListeners.add(listener);
        }
    }

    private static void notifySessionListeners() {
        for (Runnable listener : new ArrayList<>(sessionListeners)) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                System.err.println("[SESSION] Listener ignore : " + e.getMessage());
            }
        }
    }
}


