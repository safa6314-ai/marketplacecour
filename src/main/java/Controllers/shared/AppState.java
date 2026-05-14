package Controllers.shared;

public final class AppState {
    private static boolean adminMode = true;
    private static String currentModule = "forum";
    private static String currentSection = "posts";

    private AppState() {
    }

    public static boolean isAdminMode() {
        return adminMode;
    }

    public static void setAdminMode(boolean adminMode) {
        AppState.adminMode = adminMode;
    }

    public static String getCurrentModule() {
        return currentModule;
    }

    public static void setCurrentModule(String currentModule) {
        AppState.currentModule = currentModule;
    }

    public static String getCurrentSection() {
        return currentSection;
    }

    public static void setCurrentSection(String currentSection) {
        AppState.currentSection = currentSection;
    }
}
