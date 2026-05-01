package Controllers;

import Entities.Souscription;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.function.Consumer;

public class SouscriptionCardController {

    @FXML private Label clientLabel;
    @FXML private Label statusLabel;
    @FXML private Label planLabel;
    @FXML private Label dateLabel;

    private Souscription souscription;
    private Consumer<Souscription> onEdit;
    private Consumer<Souscription> onDelete;

    public void setData(Souscription s) {
        this.souscription = s;
        clientLabel.setText(s.getNomClient());
        statusLabel.setText(s.getStatut().toUpperCase());
        planLabel.setText("Abonnement #" + s.getIdAbonnement()); // Ideally we'd fetch the name
        dateLabel.setText(s.getDateDebut() + " to " + s.getDateFin());
        
        // Color status based on value
        if (s.getStatut().equalsIgnoreCase("active")) {
            statusLabel.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 15;");
        } else {
            statusLabel.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 15;");
        }
    }

    @FXML
    void handleRenew(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/GUI/ModifierSouscriptionForm.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();
            
            ModifierSouscriptionController controller = fxmlLoader.getController();
            controller.setSouscription(souscription);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Modifier - " + souscription.getNomClient());
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Note: In a real app, you might want to refresh the card here 
            // to show the new status/date.
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        if (onDelete != null) {
            onDelete.accept(souscription);
        }
    }

    public void setOnEdit(Consumer<Souscription> onEdit) {
        this.onEdit = onEdit;
    }

    public void setOnDelete(Consumer<Souscription> onDelete) {
        this.onDelete = onDelete;
    }
}
