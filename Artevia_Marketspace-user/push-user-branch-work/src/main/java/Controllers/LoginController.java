package controllers;

import Entites.User;
import Entites.OAuthUser;
import Services.OAuthService;
import Services.SecurityAlertService;
import Services.UserCRUD;
import Utils.SessionManager;
import Utils.CaptchaService;
import Utils.ThemeManager;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.prefs.Preferences;

public class LoginController {

    private static final double APP_WIDTH = 1100;
    private static final double APP_HEIGHT = 650;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_MINUTES = 5;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField tfVisiblePassword;

    @FXML
    private Button btnTogglePassword;

    @FXML
    private Label lblPasswordStrength;

    @FXML
    private CheckBox cbRememberMe;

    @FXML
    private CheckBox cbNotRobot;

    @FXML
    private VBox captchaBox;

    @FXML
    private Label lblCaptchaQuestion;

    @FXML
    private TextField tfCaptchaAnswer;

    @FXML
    private Label lblSecurityMessage;

    private final UserCRUD UserCRUD = new UserCRUD();
    private final OAuthService OAuthService = new OAuthService();
    private final SecurityAlertService SecurityAlertService = new SecurityAlertService();
    private final CaptchaService CaptchaService = new CaptchaService();
    private final Preferences preferences = Preferences.userNodeForPackage(LoginController.class);
    private boolean passwordVisible = false;
    private int failedLoginAttempts = 0;

    @FXML
    public void initialize() {
        boolean rememberMe = preferences.getBoolean("rememberMe", false);

        if (rememberMe) {
            tfEmail.setText(preferences.get("email", ""));
            cbRememberMe.setSelected(true);
        }

        tfVisiblePassword.textProperty().bindBidirectional(pfPassword.textProperty());
        pfPassword.textProperty().addListener((observable, oldValue, newValue) -> updatePasswordStrength(newValue));
        updatePasswordStrength("");
        hideCaptcha();
        clearSecurityMessage();
    }

    @FXML
    private void login(ActionEvent event) {
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText().trim();

        if (!validateAntiBot()) {
            return;
        }

        if (!validateLogin(email, password)) {
            return;
        }

        try {
            if (UserCRUD.isUserLocked(email)) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Connexion temporairement bloquee",
                        buildLockedMessage(email)
                );
                return;
            }

            User User = UserCRUD.login(email, password);

            if (User == null) {
                handleFailedPassword(email);
                return;
            }

            if ("BLOCKED".equalsIgnoreCase(User.getStatus())) {
                showAlert(Alert.AlertType.ERROR, "Compte bloque", "Votre compte est bloque.");
                return;
            }

            if ("PENDING".equalsIgnoreCase(User.getStatus())) {
                showAlert(Alert.AlertType.WARNING, "Compte en attente", "Compte en attente de validation.");
                return;
            }

            if (!"ACTIVE".equalsIgnoreCase(User.getStatus())) {
                showAlert(Alert.AlertType.ERROR, "Acces refuse", "Votre compte n'est pas actif.");
                return;
            }

            resetSecurityState();
            UserCRUD.resetFailedAttempts(email);
            saveRememberMe(email);
            SessionManager.setCurrentUser(User);
            openAdminDashboard(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de donnees", e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
        }
    }

    private void handleFailedPassword(String email) {
        registerFailedAttempt();

        try {
            int attempts = UserCRUD.incrementFailedAttempts(email);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Alerte de securite",
                        "Pour des raisons de securite, une photo sera capturee apres plusieurs tentatives echouees."
                );

                SecurityAlertService.triggerFailedLoginAlert(email);
                UserCRUD.lockUserUntil(email, LocalDateTime.now().plusMinutes(LOCK_MINUTES));

                showAlert(
                        Alert.AlertType.ERROR,
                        "Connexion bloquee",
                        "Trop de tentatives echouees. Le login est bloque pendant 5 minutes."
                );
                return;
            }
        } catch (SQLException e) {
            System.err.println("[SECURITY] Tentative echouee non enregistree : " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[SECURITY] Alerte login ignoree : " + e.getMessage());
        }

        showAlert(Alert.AlertType.ERROR, "Connexion echouee", "Email ou mot de passe incorrect.");
    }

    private String buildLockedMessage(String email) {
        try {
            LocalDateTime lockUntil = UserCRUD.getLockUntil(email);

            if (lockUntil == null) {
                return "Votre compte est temporairement bloque. Reessayez dans quelques minutes.";
            }

            long minutes = Math.max(1, Duration.between(LocalDateTime.now(), lockUntil).toMinutes() + 1);
            return "Votre compte est temporairement bloque. Reessayez dans " + minutes + " minute(s).";
        } catch (SQLException e) {
            return "Votre compte est temporairement bloque. Reessayez dans quelques minutes.";
        }
    }

    @FXML
    private void loginWithGoogle(ActionEvent event) {
        runSocialLogin(event, "Google", () -> OAuthService.loginWithGoogle());
    }

    @FXML
    private void loginWithFacebook(ActionEvent event) {
        runSocialLogin(event, "Facebook", () -> OAuthService.loginWithFacebook());
    }

    private void runSocialLogin(ActionEvent event, String provider, OAuthLoginAction action) {
        Node source = (Node) event.getSource();

        if (source instanceof Button) {
            source.setDisable(true);
        }

        Task<OAuthUser> task = new Task<>() {
            @Override
            protected OAuthUser call() throws Exception {
                System.out.println("[OAUTH DEBUG] Demarrage login " + provider + " en arriere-plan.");
                return action.login();
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            if (source instanceof Button) {
                source.setDisable(false);
            }

            try {
                loginWithOAuthUser(event, task.getValue(), provider);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, provider + " Login", cleanErrorMessage(e));
                e.printStackTrace();
            }
        });

        task.setOnFailed(workerStateEvent -> {
            if (source instanceof Button) {
                source.setDisable(false);
            }

            Throwable error = task.getException();
            showAlert(Alert.AlertType.ERROR, provider + " Login", cleanErrorMessage(error));

            if (error != null) {
                error.printStackTrace();
            }
        });

        Thread thread = new Thread(task, provider + "-oauth-login");
        thread.setDaemon(true);
        thread.start();
    }

    private String cleanErrorMessage(Throwable error) {
        if (error == null) {
            return "Erreur inconnue.";
        }

        String message = error.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return error.getClass().getSimpleName();
        }

        if (message.contains("redirect_uri_mismatch")) {
            return "Redirect URI incorrect. Ajoutez exactement : http://localhost:8765/oauth/google dans Google Cloud Console.";
        }

        return message;
    }

    private interface OAuthLoginAction {
        OAuthUser login() throws Exception;
    }

    private void loginWithOAuthUser(ActionEvent event, OAuthUser OAuthUser, String provider) throws SQLException, IOException {
        if (OAuthUser == null || OAuthUser.getEmail() == null || OAuthUser.getEmail().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Connexion sociale", "Impossible de recuperer l'email depuis " + provider + ".");
            return;
        }

        try {
            User User = UserCRUD.getByEmail(OAuthUser.getEmail());

            if (User == null) {
                String message = "Google".equalsIgnoreCase(provider)
                        ? "Aucun compte liÃ© Ã  cet email Google."
                        : "Aucun compte liÃ© Ã  cet email " + provider + ".";
                showAlert(Alert.AlertType.ERROR, "Compte introuvable", message);
                return;
            }

            if ("BLOCKED".equalsIgnoreCase(User.getStatus())) {
                showAlert(Alert.AlertType.ERROR, "Compte bloque", "Votre compte est bloque.");
                return;
            }

            if ("PENDING".equalsIgnoreCase(User.getStatus())) {
                showAlert(Alert.AlertType.WARNING, "Compte en attente", "Compte en attente de validation.");
                return;
            }

            if (!"ACTIVE".equalsIgnoreCase(User.getStatus())) {
                showAlert(Alert.AlertType.ERROR, "Acces refuse", "Votre compte n'est pas actif.");
                return;
            }

            resetSecurityState();
            SessionManager.setCurrentUser(User);
            openAdminDashboard(event);
        } catch (SQLException | IOException e) {
            throw e;
        }
    }

    private boolean validateAntiBot() {
        clearSecurityMessage();

        if (!cbNotRobot.isSelected()) {
            showSecurityMessage("Veuillez confirmer que vous n'etes pas un robot.");
            return false;
        }

        if (failedLoginAttempts >= 3 && !CaptchaService.validateCaptcha(tfCaptchaAnswer.getText())) {
            showSecurityMessage("Captcha incorrect. Veuillez reessayer.");
            refreshCaptcha();
            return false;
        }

        return true;
    }

    private void registerFailedAttempt() {
        failedLoginAttempts++;

        if (failedLoginAttempts >= 3) {
            showCaptcha();
            showSecurityMessage("Trop de tentatives echouees. Completez le captcha.");
        }
    }

    private void resetSecurityState() {
        failedLoginAttempts = 0;
        tfCaptchaAnswer.clear();
        hideCaptcha();
        clearSecurityMessage();
    }

    private void showCaptcha() {
        captchaBox.setVisible(true);
        captchaBox.setManaged(true);
        refreshCaptcha();
    }

    private void hideCaptcha() {
        captchaBox.setVisible(false);
        captchaBox.setManaged(false);
    }

    private void refreshCaptcha() {
        lblCaptchaQuestion.setText(CaptchaService.generateCaptcha());
        tfCaptchaAnswer.clear();
    }

    private void showSecurityMessage(String message) {
        lblSecurityMessage.setText(message);
    }

    private void clearSecurityMessage() {
        lblSecurityMessage.setText("");
    }

    private boolean validateLogin(String email, String password) {
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Email obligatoire", "Veuillez saisir votre email.");
            return false;
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe obligatoire", "Veuillez saisir votre mot de passe.");
            return false;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", "Veuillez saisir un email valide.");
            return false;
        }

        return true;
    }

    private void saveRememberMe(String email) {
        if (cbRememberMe.isSelected()) {
            preferences.putBoolean("rememberMe", true);
            preferences.put("email", email);
        } else {
            preferences.putBoolean("rememberMe", false);
            preferences.remove("email");
        }
    }

    private void openAdminDashboard(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AdminDashboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();

        if (scene == null) {
            scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        ThemeManager.applySavedTheme(scene);
        stage.setTitle("Admin Dashboard");
        stage.setMinWidth(APP_WIDTH);
        stage.setMinHeight(APP_HEIGHT);
        stage.setMaximized(false);
        stage.show();
    }

    @FXML
    private void goToSignIn(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/signin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            ThemeManager.applySavedTheme(scene);
            stage.setTitle("Sign in");
            stage.setMinWidth(APP_WIDTH);
            stage.setMinHeight(APP_HEIGHT);
            stage.setMaximized(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", "Impossible d'ouvrir la page Sign in.");
        }
    }

    @FXML
    private void openForgotPasswordPopup(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ForgotPassword.fxml"));

            Stage popup = new Stage();
            popup.setTitle("Forgot Password");
            popup.initModality(Modality.WINDOW_MODAL);
            popup.initOwner(((Node) event.getSource()).getScene().getWindow());
            popup.setResizable(false);
            popup.setScene(new Scene(root));
            popup.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", "Impossible d'ouvrir Forgot Password.");
        }
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

    private void updatePasswordStrength(String password) {
        clearPasswordState();

        if (password == null || password.isEmpty()) {
            lblPasswordStrength.setText("");
            return;
        }

        if (password.length() < 6) {
            applyPasswordState("password-weak", "Mot de passe faible");
            return;
        }

        if (isStrongPassword(password)) {
            applyPasswordState("password-strong", "Mot de passe fort");
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
        lblPasswordStrength.getStyleClass().add(styleClass + "-text");
        lblPasswordStrength.setText(message);
    }

    private void clearPasswordState() {
        pfPassword.getStyleClass().removeAll("password-weak", "password-medium", "password-strong");
        tfVisiblePassword.getStyleClass().removeAll("password-weak", "password-medium", "password-strong");
        lblPasswordStrength.getStyleClass().removeAll(
                "password-weak-text",
                "password-medium-text",
                "password-strong-text"
        );
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}



