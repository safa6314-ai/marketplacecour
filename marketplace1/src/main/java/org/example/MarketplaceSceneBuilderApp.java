package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MarketplaceSceneBuilderApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/marketplace-view.fxml"));
        VBox root = loader.load();
        Scene scene = new Scene(root, 1120, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/scene-builder.css").toExternalForm());
        stage.setTitle("Marketplace Scene Builder - CRUD");
        stage.setMinWidth(920);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
