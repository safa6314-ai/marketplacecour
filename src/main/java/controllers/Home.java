package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class Home extends Application {

    // ── IDs fixes pour les tests ──
    public static final int ID_ADMIN  = 1;
    public static final int ID_CLIENT = 2;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // ── Page de sélection Admin / Client ──
        Label titre = new Label("Bienvenue sur Artevia");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #1e1f2f;");

        Label sousTitre = new Label("Entrez votre ID pour vous connecter");
        sousTitre.setStyle("-fx-font-size: 14px; -fx-text-fill: #8a8fa3;");

        javafx.scene.control.TextField tfId = new javafx.scene.control.TextField();
        tfId.setPromptText("ID (ex: 1 pour Admin, 2 pour Client)");
        tfId.setPrefWidth(250);
        tfId.setPrefHeight(40);
        tfId.setStyle("-fx-font-size: 14px; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #d1d5db;");

        Button btnLogin = new Button("Se connecter");
        btnLogin.setPrefWidth(250);
        btnLogin.setPrefHeight(45);
        btnLogin.setStyle("-fx-background-color: linear-gradient(to right, #6f2dbd, #8e43e7);" +
                "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;" +
                "-fx-background-radius: 8; -fx-cursor: hand;");

        // ── Actions ──
        btnLogin.setOnAction(e -> {
            String idStr = tfId.getText().trim();
            if (idStr.equals(String.valueOf(ID_ADMIN))) {
                ouvrirFenetre("/AjouterEvenement.fxml", "Admin — Gestion des Événements", primaryStage);
            } else if (idStr.equals(String.valueOf(ID_CLIENT))) {
                ouvrirFenetre("/VoirEvenements.fxml", "Client — Événements disponibles", primaryStage);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de connexion");
                alert.setHeaderText(null);
                alert.setContentText("ID invalide. Veuillez entrer 1 (Admin) ou 2 (Client).");
                alert.showAndWait();
            }
        });

        VBox root = new VBox(20, titre, sousTitre, tfId, btnLogin);
        root.setStyle("-fx-alignment: center; -fx-padding: 60;" +
                "-fx-background-color: #f6f8fc;");

        Scene scene = new Scene(root, 520, 280);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Artevia — Accueil");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void ouvrirFenetre(String fxml, String titre, Stage ancienStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(scene);
            stage.show();
            ancienStage.close();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur : " + e.getMessage());
            alert.showAndWait();
        }
    }
}