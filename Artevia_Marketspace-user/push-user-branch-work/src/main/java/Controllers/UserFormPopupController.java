package controllers;

import Entites.User;
import Services.UserCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class UserFormPopupController {

    @FXML
    private Label lblTitle;

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField tfFirstName;

    @FXML
    private TextField tfLastName;

    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private ComboBox<String> cbStatus;

    private final UserCRUD UserCRUD = new UserCRUD();
    private Stage stage;
    private User userToEdit;
    private boolean saved;

    @FXML
    public void initialize() {
        cbRole.getItems().setAll("ADMIN", "ARTIST", "BUYER", "TRAINER", "ORGANIZER");
        cbStatus.getItems().setAll("ACTIVE", "BLOCKED");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setUser(User User) {
        this.userToEdit = User;

        if (User == null) {
            lblTitle.setText("Ajouter utilisateur");
            cbStatus.setValue("ACTIVE");
            return;
        }

        lblTitle.setText("Modifier utilisateur");
        tfUsername.setText(User.getUsername());
        tfEmail.setText(User.getEmail());
        tfFirstName.setText(User.getFirst_name());
        tfLastName.setText(User.getLast_name());
        cbRole.setValue(User.getRole());
        cbStatus.setValue(User.getStatus());
        pfPassword.clear();
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void save() {
        if (!validateForm()) {
            return;
        }

        try {
            if (userToEdit == null) {
                User User = new User(
                        tfUsername.getText().trim(),
                        tfEmail.getText().trim(),
                        pfPassword.getText().trim(),
                        tfFirstName.getText().trim(),
                        tfLastName.getText().trim(),
                        cbRole.getValue(),
                        cbStatus.getValue()
                );

                UserCRUD.ajouter(User);
            } else {
                userToEdit.setUsername(tfUsername.getText().trim());
                userToEdit.setEmail(tfEmail.getText().trim());
                userToEdit.setPassword(pfPassword.getText().trim());
                userToEdit.setFirst_name(tfFirstName.getText().trim());
                userToEdit.setLast_name(tfLastName.getText().trim());
                userToEdit.setRole(cbRole.getValue());
                userToEdit.setStatus(cbStatus.getValue());

                UserCRUD.modifier(userToEdit);
            }

            saved = true;
            close();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur sauvegarde", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private boolean validateForm() {
        if (tfUsername.getText().trim().isEmpty()
                || tfEmail.getText().trim().isEmpty()
                || tfFirstName.getText().trim().isEmpty()
                || tfLastName.getText().trim().isEmpty()
                || cbRole.getValue() == null
                || cbStatus.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs obligatoires", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (userToEdit == null && pfPassword.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Password obligatoire", "Veuillez saisir un password.");
            return false;
        }

        if (!isValidEmail(tfEmail.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", "Veuillez saisir un email valide.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void close() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


