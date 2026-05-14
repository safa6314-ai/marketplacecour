package Controllers.shared;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class AppShellController {
    @FXML private BorderPane appRoot;
    @FXML private BorderPane contentHost;
    @FXML private SidebarController sidebarController;
    @FXML private TopBarController topbarController;

    @FXML
    void initialize() {
        NavigationService.registerShell(this);
        sidebarController.setShell(this);
        topbarController.setShell(this);
        loadDefaultForCurrentMode();
    }

    public void loadDefaultForCurrentMode() {
        String module = AppState.getCurrentModule();
        if ("dashboard".equals(module)) {
            AppState.setAdminMode(true);
            AppState.setCurrentSection("dashboard");
            loadPage("/shared/DashboardHub.fxml", "dashboard");
        } else if ("abonnement".equals(module)) {
            AppState.setCurrentSection("dashboard");
            loadPage(AppState.isAdminMode() ? "/GUI/AdminDashboard.fxml" : "/GUI/ClientDashboard.fxml", "abonnement");
        } else if ("quiz".equals(module)) {
            AppState.setCurrentSection("dashboard");
            loadPage(AppState.isAdminMode() ? "/AdminDashboard.fxml" : "/QuizUtilisateur.fxml", "quiz");
        } else if ("cours".equals(module)) {
            loadPage("/placeholders/CoursPlaceholder.fxml", "cours");
        } else if ("marketplace".equals(module)) {
            loadPage("/placeholders/MarketplacePlaceholder.fxml", "marketplace");
        } else if ("events".equals(module)) {
            loadPage("/placeholders/EventsPlaceholder.fxml", "events");
        } else {
            AppState.setCurrentSection("posts");
            loadPage(AppState.isAdminMode() ? "/post/AfficherPostAdmin.fxml" : "/post/AfficherPostClient.fxml", "forum");
        }
    }

    public void loadPage(String fxmlPath, String module) {
        AppState.setCurrentModule(module);
        try {
            Parent loaded = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            contentHost.setCenter(stripLegacyChrome(loaded));
        } catch (IOException | NullPointerException e) {
            Label error = new Label("Impossible de charger la page: " + e.getMessage());
            error.getStyleClass().add("status-label");
            contentHost.setCenter(error);
        }
        refreshChrome();
    }

    public void refreshChrome() {
        sidebarController.refresh();
        topbarController.refresh();
    }

    private Parent stripLegacyChrome(Parent view) {
        if (view instanceof BorderPane borderPane) {
            borderPane.setLeft(null);
            return borderPane;
        }

        if (view instanceof AnchorPane anchorPane && !anchorPane.getChildren().isEmpty()) {
            Node first = anchorPane.getChildren().get(0);
            if (first instanceof HBox hBox && !hBox.getChildren().isEmpty()) {
                Node maybeSidebar = hBox.getChildren().get(0);
                if (isLegacySidebar(maybeSidebar)) {
                    hBox.getChildren().remove(0);
                }
            }
            if (first instanceof VBox vBox && !vBox.getChildren().isEmpty() && isLegacyTopbar(vBox.getChildren().get(0))) {
                removeModeSwitchOnly(vBox.getChildren().get(0));
            }
            return anchorPane;
        }

        return view;
    }

    private boolean isLegacySidebar(Node node) {
        return node instanceof Pane && (node.getStyleClass().contains("sidebar") || node.getStyleClass().contains("admin-sidebar"));
    }

    private boolean isLegacyTopbar(Node node) {
        return node instanceof Pane && (node.getStyleClass().contains("client-topbar") || node.getStyleClass().contains("admin-topbar"));
    }

    private void removeModeSwitchOnly(Node node) {
        if (node instanceof Pane pane) {
            pane.getChildren().removeIf(child -> child instanceof javafx.scene.control.Button button
                    && button.getText() != null
                    && button.getText().toLowerCase().contains("switch"));
            pane.getChildren().forEach(this::removeModeSwitchOnly);
        }
    }
}
