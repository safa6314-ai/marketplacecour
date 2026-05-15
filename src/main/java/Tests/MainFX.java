package Tests;

import Utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    public static final double APP_WIDTH = 1100;
    public static final double APP_HEIGHT = 650;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/WelcomePage.fxml"));
            Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);

            try {
                scene.getStylesheets().add(getClass().getResource("/admin.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("CSS admin.css introuvable ou non chargeable : " + e.getMessage());
            }

            ThemeManager.applySavedTheme(scene);

            primaryStage.setTitle("Artevia");
            primaryStage.setMinWidth(APP_WIDTH);
            primaryStage.setMinHeight(APP_HEIGHT);
            primaryStage.setMaximized(false);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println("Erreur chargement WelcomePage.fxml");
            e.printStackTrace();
        }
    }
}




