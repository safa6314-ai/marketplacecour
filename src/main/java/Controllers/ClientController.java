package Controllers;

import Utils.NotificationHelper;

import Entities.Abonnement;
import Entities.Souscription;
import Services.AbonnementCRUD;
import Services.SouscriptionCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
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
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/GUI/SubscribeForm.fxml"));
            Parent root = fxmlLoader.load();

            SubscribeFormController controller = fxmlLoader.getController();
            controller.setAbonnement(abonnement);

            Stage stage = new Stage();
            stage.setTitle("Souscription - " + abonnement.getNom());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur ouverture formulaire", e.getMessage());
        }
    }

    private void deleteSouscriptionClient(Souscription s) {
        try {
            souscriptionCRUD.supprimerPourUser(s.getIdSouscription(), CURRENT_USER_ID);
            NotificationHelper.warning("🗑 Désabonné", "Votre souscription a été supprimée.");
            loadMesSouscriptions();
        } catch (SQLException e) {
            NotificationHelper.error("❌ Erreur", e.getMessage());
            showError("Erreur suppression", e.getMessage());
        }
    }

    @FXML
    void switchToAdminView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GUI/AdminDashboard.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Artevia - Admin Dashboard");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Navigation erreur", e.getMessage());
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
