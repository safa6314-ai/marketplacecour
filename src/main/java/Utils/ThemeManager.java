package Utils;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.net.URL;
import java.util.prefs.Preferences;

public class ThemeManager {

    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    private static final String THEME_KEY = "artevia.theme";
    private static final Preferences preferences = Preferences.userNodeForPackage(ThemeManager.class);

    private ThemeManager() {
    }

    public static String getSavedTheme() {
        return preferences.get(THEME_KEY, LIGHT);
    }

    public static boolean isDarkMode() {
        return DARK.equalsIgnoreCase(getSavedTheme());
    }

    public static String toggleTheme(Scene scene) {
        String nextTheme = isDarkMode() ? LIGHT : DARK;
        applyTheme(scene, nextTheme, true);
        return nextTheme;
    }

    public static void applySavedTheme(Scene scene) {
        applyTheme(scene, getSavedTheme(), false);
    }

    public static void applySavedTheme(Parent parent) {
        applyTheme(parent, getSavedTheme());
    }

    public static void applyTheme(Scene scene, String theme, boolean animated) {
        if (scene == null) {
            return;
        }

        removeTheme(scene, "/light-theme.css");
        removeTheme(scene, "/dark-theme.css");

        String cssPath = DARK.equalsIgnoreCase(theme) ? "/dark-theme.css" : "/light-theme.css";
        URL cssUrl = ThemeManager.class.getResource(cssPath);

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Theme CSS introuvable : " + cssPath);
        }

        preferences.put(THEME_KEY, DARK.equalsIgnoreCase(theme) ? DARK : LIGHT);

        if (animated && scene.getRoot() != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(220), scene.getRoot());
            fade.setFromValue(0.82);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private static void removeTheme(Scene scene, String cssPath) {
        URL cssUrl = ThemeManager.class.getResource(cssPath);

        if (cssUrl != null) {
            scene.getStylesheets().remove(cssUrl.toExternalForm());
        }
    }

    public static void applyTheme(Parent parent, String theme) {
        if (parent == null) {
            return;
        }

        removeTheme(parent, "/light-theme.css");
        removeTheme(parent, "/dark-theme.css");

        String cssPath = DARK.equalsIgnoreCase(theme) ? "/dark-theme.css" : "/light-theme.css";
        URL cssUrl = ThemeManager.class.getResource(cssPath);

        if (cssUrl != null) {
            parent.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Theme CSS introuvable : " + cssPath);
        }
    }

    private static void removeTheme(Parent parent, String cssPath) {
        URL cssUrl = ThemeManager.class.getResource(cssPath);

        if (cssUrl != null) {
            parent.getStylesheets().remove(cssUrl.toExternalForm());
        }
    }
}



