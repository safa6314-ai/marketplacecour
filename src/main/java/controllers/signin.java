package controllers;

import Entites.User;
import Services.SmsVerificationService;
import Services.UserCRUD;
import Utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;

public class signin {

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfPhone;

    @FXML
    private Label lblPhoneMessage;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField tfVisiblePassword;

    @FXML
    private Button btnTogglePassword;

    @FXML
    private Label lblPasswordMessage;

    @FXML
    private TextField tfFirstName;

    @FXML
    private TextField tfLastName;

    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private ComboBox<String> cbStatus;

    private final SmsVerificationService SmsVerificationService = new SmsVerificationService();
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("ADMIN", "ARTIST", "BUYER", "TRAINER", "ORGANIZER");
        cbStatus.getItems().addAll("ACTIVE", "BLOCKED");

        tfVisiblePassword.textProperty().bindBidirectional(pfPassword.textProperty());
        pfPassword.textProperty().addListener((observable, oldValue, newValue) -> updatePasswordStrength(newValue));
        tfPhone.textProperty().addListener((observable, oldValue, newValue) -> validatePhoneLive(newValue));
        updatePasswordStrength("");
        validatePhoneLive("");
    }

    @FXML
    private void registerUser(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText().trim();
        String phone = normalizePhone(tfPhone.getText());
        String firstName = tfFirstName.getText().trim();
        String lastName = tfLastName.getText().trim();
        String role = cbRole.getValue();
        String status = cbStatus.getValue();

        if (!validateForm(username, email, password, phone, firstName, lastName, role, status)) {
            return;
        }

        try {
            UserCRUD UserCRUD = new UserCRUD();

            if (UserCRUD.emailExists(email)) {
                showAlert(Alert.AlertType.WARNING, "Email existe", "Cet email existe deja.");
                return;
            }

            if (UserCRUD.phoneExists(phone)) {
                showAlert(Alert.AlertType.WARNING, "Telephone existe", "Ce numero de telephone existe deja.");
                return;
            }

            User User = new User(username, email, password, firstName, lastName, role, status, phone, false);
            SmsVerificationService.sendVerificationCode(phone);

            openVerifyPhonePopup(event, User);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur ajout", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SMS", e.getMessage());
        }
    }

    @FXML
    private void goToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root, 1100, 650);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            ThemeManager.applySavedTheme(scene);
            stage.setTitle("Login");
            stage.setMinWidth(1100);
            stage.setMinHeight(650);
            stage.setMaximized(false);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root, 1100, 650);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            ThemeManager.applySavedTheme(scene);
            stage.setTitle("Login");
            stage.setMinWidth(1100);
            stage.setMinHeight(650);
            stage.setMaximized(false);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAfficherUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficheruser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Afficher Users");
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        tfUsername.clear();
        tfEmail.clear();
        tfPhone.clear();
        pfPassword.clear();
        tfFirstName.clear();
        tfLastName.clear();
        cbRole.setValue(null);
        cbStatus.setValue(null);
        hidePassword();
        updatePasswordStrength("");
        validatePhoneLive("");
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        pfPassword.setVisible(!passwordVisible);
        pfPassword.setManaged(!passwordVisible);
        tfVisiblePassword.setVisible(passwordVisible);
        tfVisiblePassword.setManaged(passwordVisible);
        btnTogglePassword.setText(passwordVisible ? "Masquer" : "Voir");
    }

    private void hidePassword() {
        passwordVisible = false;
        pfPassword.setVisible(true);
        pfPassword.setManaged(true);
        tfVisiblePassword.setVisible(false);
        tfVisiblePassword.setManaged(false);
        btnTogglePassword.setText("Voir");
    }

    private void updatePasswordStrength(String password) {
        clearPasswordState();

        if (password == null || password.isEmpty()) {
            lblPasswordMessage.setText("");
            return;
        }

        if (password.length() < 6) {
            applyPasswordState("password-weak", "Mot de passe trop faible");
            return;
        }

        if (isStrongPassword(password)) {
            applyPasswordState("password-strong", "Mot de passe sÃ©curisÃ©");
            return;
        }

        applyPasswordState("password-medium", "Mot de passe moyen");
    }

    private boolean isStrongPassword(String password) {
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^A-Za-z0-9].*");

        return password.length() >= 8 && hasUppercase && hasDigit && hasSymbol;
    }

    private void applyPasswordState(String styleClass, String message) {
        pfPassword.getStyleClass().add(styleClass);
        tfVisiblePassword.getStyleClass().add(styleClass);
        lblPasswordMessage.getStyleClass().add(styleClass + "-text");
        lblPasswordMessage.setText(message);
    }

    private void clearPasswordState() {
        pfPassword.getStyleClass().removeAll("password-weak", "password-medium", "password-strong");
        tfVisiblePassword.getStyleClass().removeAll("password-weak", "password-medium", "password-strong");
        lblPasswordMessage.getStyleClass().removeAll(
                "password-weak-text",
                "password-medium-text",
                "password-strong-text"
        );
    }

    private void validatePhoneLive(String phoneValue) {
        clearPhoneState();

        String phone = normalizePhone(phoneValue);

        if (phone.isEmpty()) {
            lblPhoneMessage.setText("");
            return;
        }

        if (isValidTunisianPhone(phone)) {
            tfPhone.getStyleClass().add("phone-valid");
            lblPhoneMessage.getStyleClass().add("phone-valid-text");
            lblPhoneMessage.setText("Numero tunisien valide");
        } else {
            tfPhone.getStyleClass().add("phone-invalid");
            lblPhoneMessage.getStyleClass().add("phone-invalid-text");
            lblPhoneMessage.setText("Format requis : +216XXXXXXXX");
        }
    }

    private void clearPhoneState() {
        tfPhone.getStyleClass().removeAll("phone-valid", "phone-invalid");
        lblPhoneMessage.getStyleClass().removeAll("phone-valid-text", "phone-invalid-text");
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("\\s+", "").trim();
    }

    private boolean isValidTunisianPhone(String phone) {
        return phone != null && phone.matches("^\\+216\\d{8}$");
    }

    private void openVerifyPhonePopup(ActionEvent event, User pendingUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/VerifyPhone.fxml"));
        Parent root = loader.load();

        VerifyPhoneController controller = loader.getController();
        controller.setPendingUser(pendingUser, this::clearForm);

        Stage stage = new Stage();
        stage.setTitle("Verification telephone");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getWindow(event));
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private Window getWindow(ActionEvent event) {
        return ((Node) event.getSource()).getScene().getWindow();
    }

    private boolean validateForm(String username, String email, String password, String phone, String firstName,
                                 String lastName, String role, String status) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()
                || phone.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                || role == null || status == null) {
            showAlert(Alert.AlertType.WARNING, "Champs obligatoires", "Veuillez remplir tous les champs.");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", "Veuillez saisir un email valide.");
            return false;
        }

        if (!isValidTunisianPhone(phone)) {
            showAlert(Alert.AlertType.WARNING, "Telephone invalide", "Le numero doit respecter le format +216XXXXXXXX.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}




