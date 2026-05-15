package controllers;

import Services.ResetTokenCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;

public class VerifyCodeController {

    @FXML
    private TextField tfCode;

    @FXML
    private Label lblMessage;

    private final ResetTokenCRUD ResetTokenCRUD = new ResetTokenCRUD();
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void verifyCode(ActionEvent event) {
        String code = tfCode.getText().trim();

        if (!code.matches("\\d{6}")) {
            showInlineError("Veuillez saisir un code de 6 chiffres.");
            return;
        }

        try {
            Integer userId = ResetTokenCRUD.getValidTokenUserId(email, code);

            if (userId == null) {
                showInlineError("Code incorrect ou expire.");
                return;
            }

            openResetPasswordPopup(event, userId, code);
            closeCurrentWindow(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de donnees", e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        closeCurrentWindow(event);
    }

    private void openResetPasswordPopup(ActionEvent event, int userId, String token) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPassword.fxml"));
        Parent root = loader.load();

        ResetPasswordController controller = loader.getController();
        controller.setResetData(userId, token);

        Stage stage = new Stage();
        stage.setTitle("Nouveau mot de passe");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getMainOwner(event));
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private Window getWindow(ActionEvent event) {
        return ((Node) event.getSource()).getScene().getWindow();
    }

    private Window getMainOwner(ActionEvent event) {
        Window currentWindow = getWindow(event);

        if (currentWindow instanceof Stage) {
            Window owner = ((Stage) currentWindow).getOwner();
            return owner == null ? currentWindow : owner;
        }

        return currentWindow;
    }

    private void closeCurrentWindow(ActionEvent event) {
        ((Stage) getWindow(event)).close();
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

