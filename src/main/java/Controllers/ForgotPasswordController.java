package controllers;

import Entites.User;
import Services.EmailService;
import Services.ResetTokenCRUD;
import Services.UserCRUD;
import jakarta.mail.MessagingException;
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
import java.time.LocalDateTime;

public class ForgotPasswordController {

    @FXML
    private TextField tfEmail;

    @FXML
    private Label lblMessage;

    private final UserCRUD UserCRUD = new UserCRUD();
    private final ResetTokenCRUD ResetTokenCRUD = new ResetTokenCRUD();
    private final EmailService EmailService = new EmailService();

    @FXML
    private void sendCode(ActionEvent event) {
        String email = tfEmail.getText().trim();

        if (email.isEmpty()) {
            showInlineError("Veuillez saisir votre email.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showInlineError("Veuillez saisir un email valide.");
            return;
        }

        try {
            User User = UserCRUD.getByEmail(email);

            if (User == null) {
                showInlineError("Aucun compte trouve avec cet email.");
                return;
            }

            String token = EmailService.generateOtp();
            ResetTokenCRUD.createToken(User.getId(), token, LocalDateTime.now().plusMinutes(10));
            EmailService.sendPasswordResetCode(email, token);

            openVerifyCodePopup(event, email);
            closeCurrentWindow(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de donnees", e.getMessage());
        } catch (MessagingException e) {
            showInlineError(e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur email", e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        closeCurrentWindow(event);
    }

    private void openVerifyCodePopup(ActionEvent event, String email) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/VerifyCode.fxml"));
        Parent root = loader.load();

        VerifyCodeController controller = loader.getController();
        controller.setEmail(email);

        Stage stage = new Stage();
        stage.setTitle("Verification du code");
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


