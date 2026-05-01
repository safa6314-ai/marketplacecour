package Controllers;

import Entities.Abonnement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.function.Consumer;

public class ClientAbonnementCardController {

    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label durationLabel;
    @FXML private Label descLabel;

    private Abonnement abonnement;
    private Consumer<Abonnement> onSubscribe;

    public void setData(Abonnement a) {
        this.abonnement = a;
        nameLabel.setText(a.getNom());
        priceLabel.setText(String.format("%.2f", a.getPrix()));
        durationLabel.setText(a.getDureeMois() + " Months Access");
        descLabel.setText(a.getDescription());
    }

    @FXML
    void handleSubscribe(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/GUI/SubscribeForm.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();
            
            SubscribeFormController controller = fxmlLoader.getController();
            controller.setAbonnement(abonnement);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Complete Subscription - " + abonnement.getNom());
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnSubscribe(Consumer<Abonnement> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
}
