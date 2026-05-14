package controllers;

import Entites.User;
import Utils.SessionManager;
import Utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AdminDashboardController {

    private static final double APP_WIDTH = 1100;
    private static final double APP_HEIGHT = 650;

    @FXML
    private StackPane centerContent;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnUsers;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnThemeToggle;

    @FXML
    private Label lblAdminName;

    @FXML
    private ImageView ivAdminPhoto;

    @FXML
    private HBox adminProfileBox;

    @FXML
    public void initialize() {
        User admin = SessionManager.getCurrentUser();

        if (admin == null) {
            Platform.runLater(this::redirectToLogin);
            return;
        }

        showConnectedAdmin(admin);
        Platform.runLater(() -> {
            ThemeManager.applySavedTheme(centerContent.getScene());
            updateThemeToggle();
        });
        loadPage("/AdminUsers.fxml", btnUsers);
    }

    @FXML
    private void showDashboard() {
        loadPage("/DashboardContent.fxml", btnDashboard);
    }

    @FXML
    private void showUsers() {
        loadPage("/AdminUsers.fxml", btnUsers);
    }

    @FXML
    private void showProfile() {
        loadPage("/AdminProfile.fxml", btnProfile);
    }

    @FXML
    private void showProfileFromTopbar() {
        loadPage("/AdminProfile.fxml", btnProfile);
    }

    @FXML
    private void logout(ActionEvent event) {
        SessionManager.clearSession();
        switchRoot(event, "/Login.fxml", "Login");
    }

    @FXML
    private void toggleTheme() {
        String theme = ThemeManager.toggleTheme(centerContent.getScene());
        for (Node child : centerContent.getChildren()) {
            if (child instanceof Parent) {
                ThemeManager.applyTheme((Parent) child, theme);
            }
        }
        updateThemeToggle();
    }

    private void loadPage(String fxmlPath, Button activeButton) {
        if (SessionManager.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        try {
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                throw new IOException("FXML introuvable : " + fxmlPath);
            }

            Parent page = FXMLLoader.load(url);
            ThemeManager.applySavedTheme(page);
            centerContent.getChildren().setAll(page);
            setActiveButton(activeButton);
            playFade(page);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        btnDashboard.getStyleClass().remove("sidebar-nav-button-active");
        btnUsers.getStyleClass().remove("sidebar-nav-button-active");
        btnProfile.getStyleClass().remove("sidebar-nav-button-active");

        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-nav-button-active")) {
            activeButton.getStyleClass().add("sidebar-nav-button-active");
        }
    }

    private void playFade(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(180), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void showConnectedAdmin(User admin) {
        lblAdminName.setText(admin.getUsername() == null ? "Admin" : admin.getUsername());

        ivAdminPhoto.setImage(getImageOrDefault(admin.getProfile_image()));
        ivAdminPhoto.setClip(new Circle(20, 20, 20));
    }

    private Image getImageOrDefault(String imagePath) {
        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File file = new File(imagePath);

                if (file.exists()) {
                    return new Image(file.toURI().toString());
                }
            }
        } catch (Exception e) {
            System.out.println("Image profil introuvable : " + e.getMessage());
        }

        return new Image(getClass().getResource("/images/default-user.png").toExternalForm());
    }

    private void redirectToLogin() {
        try {
            URL url = getClass().getResource("/Login.fxml");

            if (url == null) {
                throw new IOException("FXML introuvable : /Login.fxml");
            }

            Parent root = FXMLLoader.load(url);
            Scene scene = centerContent.getScene();

            if (scene != null) {
                scene.setRoot(root);
                ThemeManager.applySavedTheme(scene);
            }
        } catch (Exception e) {
            System.out.println("Erreur redirection Login.fxml");
            e.printStackTrace();
        }
    }

    private void updateThemeToggle() {
        if (btnThemeToggle == null) {
            return;
        }

        if (ThemeManager.isDarkMode()) {
            btnThemeToggle.setText("Light");
            btnThemeToggle.setGraphic(new FontIcon("fas-sun"));
        } else {
            btnThemeToggle.setText("Dark");
            btnThemeToggle.setGraphic(new FontIcon("fas-moon"));
        }
    }

    private void switchRoot(ActionEvent event, String fxmlPath, String title) {
        try {
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                throw new IOException("FXML introuvable : " + fxmlPath);
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            ThemeManager.applySavedTheme(scene);
            stage.setTitle(title);
            stage.setMinWidth(APP_WIDTH);
            stage.setMinHeight(APP_HEIGHT);
            stage.setMaximized(false);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}




