package Controllers.shared;

import javafx.fxml.FXML;

public class DashboardHubController {

    @FXML
    void openForumDashboard() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("posts");
        NavigationService.navigate("/post/AfficherPostAdmin.fxml", "forum");
    }

    @FXML
    void openQuizDashboard() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("dashboard");
        NavigationService.navigate("/AdminDashboard.fxml", "quiz");
    }

    @FXML
    void openAbonnementDashboard() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("dashboard");
        NavigationService.navigate("/GUI/AdminDashboard.fxml", "abonnement");
    }
}
