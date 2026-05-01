package Controllers;

import Entities.Abonnement;
import Entities.Souscription;
import Services.SouscriptionCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class SubscribeFormController {

    @FXML private Label planNameLabel;
    @FXML private Label planPriceLabel;
    @FXML private Label planDurationLabel;
    
    @FXML private TextField clientNameField;
    @FXML private TextField startDateField;
    @FXML private TextField endDateField;
    @FXML private Label errorLabel;

    private Abonnement abonnement;
    private LocalDate startDate;
    private LocalDate endDate;

    public void setAbonnement(Abonnement a) {
        this.abonnement = a;
        
        // Setup Plan Summary
        planNameLabel.setText(a.getNom());
        planPriceLabel.setText(String.format("%.2f DT", a.getPrix()));
        planDurationLabel.setText(a.getDureeMois() + " Mois");
        
        // Setup Dates Automatically
        this.startDate = LocalDate.now();
        this.endDate = startDate.plusMonths(a.getDureeMois());
        
        startDateField.setText(startDate.toString());
        endDateField.setText(endDate.toString());
    }

    @FXML
    void handleConfirm(ActionEvent event) {
        String clientName = clientNameField.getText().trim();
        
        if (clientName.isEmpty()) {
            errorLabel.setText("Please enter your name.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        try {
            SouscriptionCRUD crud = new SouscriptionCRUD();
            
            // Hardcoding idUser = 2 for testing based on user's previous request.
            // In a real app, this would come from a Session Manager.
            Souscription s = new Souscription(
                2, 
                clientName, 
                Date.valueOf(startDate), 
                Date.valueOf(endDate), 
                "Active", 
                abonnement.getIdAbonnement()
            );
            
            crud.ajouter(s);
            
            // Close window
            closeWindow();
            
        } catch (Exception e) {
            errorLabel.setText("Database error. Please try again.");
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
        Stage stage = (Stage) clientNameField.getScene().getWindow();
        stage.close();
    }
}
