package Controllers.shared;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class TopBarController {
    @FXML private Label contextLabel;
    @FXML private Button switchModeButton;

    private AppShellController shell;

    void setShell(AppShellController shell) {
        this.shell = shell;
    }

    @FXML
    void switchMode() {
        NavigationService.switchMode();
    }

    void refresh() {
        String module = AppState.getCurrentModule();
        String label = switch (module) {
            case "dashboard" -> "Dashboard";
            case "abonnement" -> "Abonnements";
            case "cours" -> "Cours";
            case "marketplace" -> "Marketplace";
            case "events" -> "Events";
            case "quiz" -> "Quiz";
            default -> "Forum";
        };
        contextLabel.setText(label + " - " + (AppState.isAdminMode() ? "Admin" : "Client"));
        switchModeButton.setText(AppState.isAdminMode() ? "Switch To Client View" : "Switch To Admin View");
    }
}
