package Controllers;

import Utils.NotificationHelper;
import Utils.StripePaymentWindow;
import Utils.StripeService;

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
            
            // 1. Get Stripe Payment URL
            String paymentUrl = StripeService.getPaymentUrl(abonnement);
            
            // 2. Open Stripe Payment Window
            StripePaymentWindow.show(paymentUrl, () -> {
                // Success Callback: Save to DB
                try {
                    Souscription s = new Souscription(
                        2, // Hardcoded for testing
                        clientName, 
                        Date.valueOf(startDate), 
                        Date.valueOf(endDate), 
                        "Active", 
                        abonnement.getIdAbonnement()
                    );
                    crud.ajouter(s);
                    NotificationHelper.success("✅ Paiement réussi", "Votre abonnement '" + abonnement.getNom() + "' est activé !");
                    closeWindow();
                } catch (Exception ex) {
                    NotificationHelper.error("❌ Erreur DB", "Le paiement a réussi mais l'enregistrement a échoué.");
                    ex.printStackTrace();
                }
            }, () -> {
                // Cancel Callback
                NotificationHelper.warning("⚠️ Paiement annulé", "La transaction n'a pas été finalisée.");
            });
            
        } catch (Exception e) {
            NotificationHelper.error("❌ Erreur Stripe", "Impossible de générer la session de paiement.");
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
