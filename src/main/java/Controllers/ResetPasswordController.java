package controllers;

import Services.ResetTokenCRUD;
import Services.UserCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ResetPasswordController {

    @FXML
    private PasswordField pfNewPassword;

    @FXML
    private PasswordField pfConfirmPassword;

    @FXML
    private Label lblMessage;

    private final UserCRUD UserCRUD = new UserCRUD();
    private final ResetTokenCRUD ResetTokenCRUD = new ResetTokenCRUD();
    private int userId;
    private String token;

    public void setResetData(int userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    @FXML
    private void resetPassword(ActionEvent event) {
        String newPassword = pfNewPassword.getText().trim();
        String confirmPassword = pfConfirmPassword.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showInlineError("Veuillez remplir les deux champs.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showInlineError("Les mots de passe ne sont pas identiques.");
            return;
        }

        if (!isStrongPassword(newPassword)) {
            showInlineError("Mot de passe requis : 8 caracteres, majuscule, chiffre et symbole.");
            return;
        }

        try {
            if (!ResetTokenCRUD.isTokenValid(userId, token)) {
                showInlineError("Code expire. Veuillez recommencer.");
                return;
            }

            UserCRUD.updatePassword(userId, newPassword);
            ResetTokenCRUD.markTokenUsed(userId, token);
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Mot de passe modifie avec succes.");
            closeCurrentWindow(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de donnees", e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        closeCurrentWindow(event);
    }

    private boolean isStrongPassword(String password) {
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^A-Za-z0-9].*");
        return password.length() >= 8 && hasUppercase && hasDigit && hasSymbol;
    }

    private void closeCurrentWindow(ActionEvent event) {
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

