package Tests;

import Utils.DataSeeder;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public class MainFX extends Application {

    private BorderPane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DataSeeder.seed();

        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        ComboBox<String> roleSelector = new ComboBox<>(FXCollections.observableArrayList("Forum", "Marketplace"));
        roleSelector.setValue("Forum");
        roleSelector.getStyleClass().add("role-selector");

        Label label = new Label("Espace");
        label.getStyleClass().add("input-label");

        HBox selectorBar = new HBox(10, label, roleSelector);
        selectorBar.setPadding(new Insets(10, 18, 10, 18));
        selectorBar.getStyleClass().add("role-bar");
        root.setTop(selectorBar);

        roleSelector.valueProperty().addListener((obs, oldValue, newValue) -> chargerEspace(newValue));
        chargerEspace("Forum");

        Scene scene = new Scene(root, 1180, 820);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/CSS/style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("ForumInArtevia");
        primaryStage.show();
    }

    private void chargerEspace(String role) {
        if ("Marketplace".equals(role)) {
            chargerMarketplace();
            return;
        }
        String fxml = "/post/AfficherPostAdmin.fxml";
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            root.setCenter(view);
        } catch (IOException e) {
            Label error = new Label("Impossible de charger l'espace " + role + " : " + e.getMessage());
            error.getStyleClass().add("status-label");
            root.setCenter(error);
        }
    }

    private void chargerMarketplace() {
        try {
            URL fxmlUrl = getClass().getResource("/org/example/artevia-marketplace-view.fxml");
            FXMLLoader loader;
            if (fxmlUrl != null) {
                loader = new FXMLLoader(fxmlUrl);
            } else {
                File marketplaceClasses = new File("marketplace1/target/classes");
                File marketplaceResources = new File("marketplace1/src/main/resources");
                File marketplaceFxml = new File(marketplaceResources, "org/example/artevia-marketplace-view.fxml");
                if (!marketplaceFxml.exists()) {
                    throw new IOException("FXML Marketplace introuvable: " + marketplaceFxml.getAbsolutePath());
                }
                URLClassLoader marketplaceLoader = new URLClassLoader(
                        new URL[]{marketplaceClasses.toURI().toURL(), marketplaceResources.toURI().toURL()},
                        getClass().getClassLoader()
                );
                loader = new FXMLLoader(marketplaceFxml.toURI().toURL());
                loader.setClassLoader(marketplaceLoader);
            }
            Parent marketplaceView = loader.load();
            root.setCenter(marketplaceView);
        } catch (Exception e) {
            Label error = new Label("Impossible de charger Marketplace : " + e.getMessage() +
                    "\nCompilez d'abord marketplace1 avec: powershell -ExecutionPolicy Bypass -File marketplace1\\run-javafx.ps1");
            error.getStyleClass().add("status-label");
            root.setCenter(error);
        }
    }
}
