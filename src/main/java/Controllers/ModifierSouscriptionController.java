package Controllers;

import Entities.Souscription;
import Services.SouscriptionCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Date;

public class ModifierSouscriptionController {

    @FXML private Label clientNameLabel;
    @FXML private TextField statutField;
    @FXML private DatePicker dateFinPicker;
    @FXML private Label errorLabel;

    private Souscription souscription;

    public void setSouscription(Souscription s) {
        this.souscription = s;
        clientNameLabel.setText("Pour: " + s.getNomClient());
        statutField.setText(s.getStatut());
        if (s.getDateFin() != null) {
            dateFinPicker.setValue(s.getDateFin().toLocalDate());
        }
    }

    @FXML
    void handleSaveEdit(ActionEvent event) {
        String statut = statutField.getText().trim();
        
        if (statut.isEmpty()) {
            errorLabel.setText("Le statut est requis.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        if (dateFinPicker.getValue() == null) {
            errorLabel.setText("La date de fin est requise.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        try {
            souscription.setStatut(statut);
            souscription.setDateFin(Date.valueOf(dateFinPicker.getValue()));
            
            SouscriptionCRUD crud = new SouscriptionCRUD();
            crud.modifier(souscription);
            
            closeWindow();
        } catch (Exception e) {
            errorLabel.setText("Erreur base de donnees.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            e.printStackTrace();
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) statutField.getScene().getWindow();
        stage.close();
    }
}
