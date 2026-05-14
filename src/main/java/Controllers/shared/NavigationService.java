package Controllers.shared;

public final class NavigationService {
    private static AppShellController shellController;

    private NavigationService() {
    }

    static void registerShell(AppShellController controller) {
        shellController = controller;
    }

    public static void navigate(String fxmlPath, String module) {
        if (shellController == null) {
            return;
        }
        shellController.loadPage(fxmlPath, module);
    }

    public static void refreshChrome() {
        if (shellController != null) {
            shellController.refreshChrome();
        }
    }

    public static void switchMode() {
        AppState.setAdminMode(!AppState.isAdminMode());
        if (shellController != null) {
            shellController.loadDefaultForCurrentMode();
        }
    }
}
