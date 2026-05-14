package controllers;

import Entites.User;
import Services.SmsVerificationService;
import Services.UserCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class VerifyPhoneController {

    @FXML
    private Label lblPhone;

    @FXML
    private TextField tfCode;

    @FXML
    private Label lblMessage;

    private final SmsVerificationService SmsVerificationService = new SmsVerificationService();
    private final UserCRUD UserCRUD = new UserCRUD();
    private User pendingUser;
    private Runnable onSuccess;

    public void setPendingUser(User pendingUser, Runnable onSuccess) {
        this.pendingUser = pendingUser;
        this.onSuccess = onSuccess;
        lblPhone.setText(pendingUser == null ? "" : pendingUser.getPhone());
    }

    @FXML
    private void verifyCode(ActionEvent event) {
        String code = tfCode.getText().trim();

        if (!code.matches("\\d{4,8}")) {
            showInlineError("Veuillez saisir le code recu par SMS.");
            return;
        }

        try {
            if (pendingUser == null) {
                showInlineError("Utilisateur introuvable. Veuillez recommencer.");
                return;
            }

            boolean verified = SmsVerificationService.checkVerificationCode(pendingUser.getPhone(), code);

            if (!verified) {
                showInlineError("Code incorrect ou expire.");
                return;
            }

            pendingUser.setPhone_verified(true);
            UserCRUD.ajouter(pendingUser);

            if (onSuccess != null) {
                onSuccess.run();
            }

            showAlert(Alert.AlertType.INFORMATION, "Succes", "Compte cree et telephone verifie avec succes.");
            close(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur ajout", e.getMessage());
        } catch (Exception e) {
            showInlineError(e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur SMS", e.getMessage());
        }
    }

    @FXML
    private void resendCode() {
        try {
            if (pendingUser == null) {
                showInlineError("Utilisateur introuvable. Veuillez recommencer.");
                return;
            }

            boolean sent = SmsVerificationService.sendVerificationCode(pendingUser.getPhone());
            showInlineError(sent ? "Nouveau code envoye." : "Impossible d'envoyer le code.");
        } catch (Exception e) {
            showInlineError(e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        close(event);
    }

    private void close(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showInlineError(String message) {
        lblMessage.setText(message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


