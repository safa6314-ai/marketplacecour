package controllers;

import Utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class WelcomePageController {

    private static final double APP_WIDTH = 1100;
    private static final double APP_HEIGHT = 650;

    @FXML
    private VBox heroContent;

    @FXML
    private VBox modulesPanel;

    @FXML
    public void initialize() {
        playIntro(heroContent, 0);
        playIntro(modulesPanel, 140);
    }

    @FXML
    private void openLogin(ActionEvent event) {
        switchPage(event, "/Login.fxml", "Login");
    }

    @FXML
    private void openSignUp(ActionEvent event) {
        switchPage(event, "/signin.fxml", "Sign Up");
    }

    private void switchPage(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playIntro(Node node, int delayMillis) {
        if (node == null) {
            return;
        }

        node.setOpacity(0);
        node.setTranslateY(24);

        FadeTransition fade = new FadeTransition(Duration.millis(650), node);
        fade.setDelay(Duration.millis(delayMillis));
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(650), node);
        slide.setDelay(Duration.millis(delayMillis));
        slide.setFromY(24);
        slide.setToY(0);

        fade.play();
        slide.play();
    }
}
