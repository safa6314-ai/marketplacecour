package Controllers;

import Entities.Abonnement;
import Entities.Souscription;
import Services.AbonnementCRUD;
import Services.SouscriptionCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML private GridPane grid;
    @FXML private Button btnPlans;
    @FXML private Button btnMesSouscriptions;
    @FXML private Label pageTitle;

    private static final int CURRENT_USER_ID = 2;
    private final AbonnementCRUD abonnementCRUD = new AbonnementCRUD();
    private final SouscriptionCRUD souscriptionCRUD = new SouscriptionCRUD();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showPlans(null);
    }

    @FXML
    void showPlans(ActionEvent event) {
        pageTitle.setText("Plans disponibles");
        btnPlans.getStyleClass().setAll("client-switch-btn-active");
        btnMesSouscriptions.getStyleClass().setAll("client-switch-btn");
        loadAbonnements();
    }

    @FXML
    void showMesSouscriptions(ActionEvent event) {
        pageTitle.setText("Mes souscriptions");
        btnPlans.getStyleClass().setAll("client-switch-btn");
        btnMesSouscriptions.getStyleClass().setAll("client-switch-btn-active");
        loadMesSouscriptions();
    }

    private void loadAbonnements() {
        grid.getChildren().clear();
        try {
            List<Abonnement> list = abonnementCRUD.afficher();
            int column = 0;
            int row = 1;
            for (Abonnement a : list) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/GUI/ClientAbonnementCard.fxml"));
                VBox cardBox = fxmlLoader.load();
                
                ClientAbonnementCardController cardController = fxmlLoader.getController();
                cardController.setData(a);
                cardController.setOnSubscribe(this::subscribeToAbonnement);
                
                if (column == 3) {
                    column = 0;
                    row++;
                }

                grid.add(cardBox, column++, row);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMesSouscriptions() {
        grid.getChildren().clear();
        try {
            List<Souscription> list = souscriptionCRUD.afficherParUser(CURRENT_USER_ID);
            int column = 0;
            int row = 1;
            for (Souscription s : list) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/GUI/SouscriptionCard.fxml"));
                VBox cardBox = fxmlLoader.load();

                SouscriptionCardController cardController = fxmlLoader.getController();
                cardController.setData(s);
                cardController.setOnDelete(this::deleteSouscriptionClient);

                if (column == 3) {
                    column = 0;
                    row++;
                }
                grid.add(cardBox, column++, row);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToAbonnement(Abonnement abonnement) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Souscrire a " + abonnement.getNom());
        dialog.setHeaderText("Complete les informations de ta souscription");

        ButtonType saveType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(8);
        pane.setPadding(new Insets(10));

        TextField clientNameField = new TextField();
        clientNameField.setPromptText("Ton nom");
        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusMonths(abonnement.getDureeMois()));
        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("active", "pending");
        statutBox.setValue("active");
        Label planInfo = new Label(abonnement.getNom() + " | " + abonnement.getPrix() + " DT | " + abonnement.getDureeMois() + " mois");
        Label clientNameError = new Label();
        Label startDateError = new Label();
        Label endDateError = new Label();
        Label statutError = new Label();
        Label dbError = new Label();
        clientNameError.setStyle("-fx-text-fill: #d93025; -fx-font-size: 11px;");
        startDateError.setStyle("-fx-text-fill: #d93025; -fx-font-size: 11px;");
        endDateError.setStyle("-fx-text-fill: #d93025; -fx-font-size: 11px;");
        statutError.setStyle("-fx-text-fill: #d93025; -fx-font-size: 11px;");
        dbError.setStyle("-fx-text-fill: #d93025; -fx-font-size: 11px;");

        pane.add(new Label("Plan"), 0, 0);
        pane.add(planInfo, 1, 0);
        pane.add(new Label("Nom client"), 0, 1);
        pane.add(clientNameField, 1, 1);
        pane.add(clientNameError, 1, 2);
        pane.add(new Label("Date debut"), 0, 3);
        pane.add(startPicker, 1, 3);
        pane.add(startDateError, 1, 4);
        pane.add(new Label("Date fin"), 0, 5);
        pane.add(endPicker, 1, 5);
        pane.add(endDateError, 1, 6);
        pane.add(new Label("Statut"), 0, 7);
        pane.add(statutBox, 1, 7);
        pane.add(statutError, 1, 8);
        pane.add(dbError, 1, 9);

        GridPane.setHgrow(clientNameField, Priority.ALWAYS);
        GridPane.setHgrow(startPicker, Priority.ALWAYS);
        GridPane.setHgrow(endPicker, Priority.ALWAYS);
        GridPane.setHgrow(statutBox, Priority.ALWAYS);

        dialog.getDialogPane().setContent(pane);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, evt -> {
            clientNameError.setText("");
            startDateError.setText("");
            endDateError.setText("");
            statutError.setText("");
            dbError.setText("");
            String clientName = clientNameField.getText() == null ? "" : clientNameField.getText().trim();
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            String statut = statutBox.getValue();

            if (clientName.isEmpty()) {
                clientNameError.setText("Nom client obligatoire.");
                evt.consume();
            }
            if (start == null || end == null) {
                if (start == null) startDateError.setText("Date debut obligatoire.");
                if (end == null) endDateError.setText("Date fin obligatoire.");
                evt.consume();
            }
            if (start != null && end != null && end.isBefore(start)) {
                endDateError.setText("Date fin doit etre >= date debut.");
                evt.consume();
            }
            if (statut == null || statut.trim().isEmpty()) {
                statutError.setText("Statut obligatoire.");
                evt.consume();
            }

            if (!clientNameError.getText().isEmpty() || !startDateError.getText().isEmpty()
                    || !endDateError.getText().isEmpty() || !statutError.getText().isEmpty()) {
                return;
            }

            try {
                Souscription s = new Souscription(
                        CURRENT_USER_ID,
                        clientName,
                        Date.valueOf(start),
                        Date.valueOf(end),
                        statut,
                        abonnement.getIdAbonnement()
                );
                souscriptionCRUD.ajouter(s);
                showMesSouscriptions(null);
            } catch (SQLException e) {
                dbError.setText("Erreur DB: " + e.getMessage());
                evt.consume();
            }
        });

        dialog.showAndWait();
    }

    private void deleteSouscriptionClient(Souscription s) {
        try {
            souscriptionCRUD.supprimerPourUser(s.getIdSouscription(), CURRENT_USER_ID);
            loadMesSouscriptions();
        } catch (SQLException e) {
            showError("Erreur suppression", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
