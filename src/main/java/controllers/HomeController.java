package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    private static final double APP_WIDTH = 1100;
    private static final double APP_HEIGHT = 650;

    @FXML
    private void goToSignIn(ActionEvent event) throws IOException {
        changePage(event, "/signin.fxml", "Sign in");
    }

    @FXML
    private void goToLogin(ActionEvent event) throws IOException {
        changePage(event, "/Login.fxml", "Login");
    }

    private void changePage(ActionEvent event, String fxmlPath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();

        if (scene == null) {
            scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        stage.setTitle(title);
        stage.setMinWidth(APP_WIDTH);
        stage.setMinHeight(APP_HEIGHT);
        stage.setMaximized(false);
        stage.show();
    }
}



