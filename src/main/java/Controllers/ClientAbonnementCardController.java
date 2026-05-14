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
        if (onSubscribe != null) {
            onSubscribe.accept(abonnement);
        }
    }

    public void setOnSubscribe(Consumer<Abonnement> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
}
