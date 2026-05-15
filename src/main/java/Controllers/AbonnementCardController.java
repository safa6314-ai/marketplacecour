package Controllers;

import Entities.Abonnement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.function.Consumer;

public class AbonnementCardController {

    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label durationLabel;
    @FXML private Label descLabel;

    private Abonnement abonnement;
    private Consumer<Abonnement> onEdit;
    private Consumer<Abonnement> onDelete;

    public void setData(Abonnement a) {
        this.abonnement = a;
        nameLabel.setText(a.getNom());
        priceLabel.setText(String.format("%.2f", a.getPrix()));
        durationLabel.setText(a.getDureeMois() + " Months");
        descLabel.setText(a.getDescription());
    }

    @FXML
    void handleEdit(ActionEvent event) {
        if (onEdit != null) {
            onEdit.accept(abonnement);
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (onDelete != null) {
            onDelete.accept(abonnement);
        }
    }

    public void setOnEdit(Consumer<Abonnement> onEdit) {
        this.onEdit = onEdit;
    }

    public void setOnDelete(Consumer<Abonnement> onDelete) {
        this.onDelete = onDelete;
    }
}
