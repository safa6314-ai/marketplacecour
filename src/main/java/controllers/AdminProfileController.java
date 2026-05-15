package controllers;

import Entites.User;
import Services.UserCRUD;
import Utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;

public class AdminProfileController {

    @FXML
    private ImageView ivProfileImage;

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfFirstName;

    @FXML
    private TextField tfLastName;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private Label lblRole;

    @FXML
    private Label lblStatus;

    private final UserCRUD UserCRUD = new UserCRUD();
    private User currentUser;
    private String selectedImagePath;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Session introuvable", "Veuillez vous reconnecter.");
            Platform.runLater(this::redirectToHome);
            return;
        }

        selectedImagePath = currentUser.getProfile_image();
        fillForm(currentUser);
        loadProfileImage(selectedImagePath);
    }

    @FXML
    private void chooseProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Window window = ivProfileImage.getScene() == null ? null : ivProfileImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            loadProfileImage(selectedImagePath);
        }
    }

    @FXML
    private void removeProfileImage() {
        selectedImagePath = null;
        loadProfileImage(null);
    }

    @FXML
    private void saveProfile() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Session introuvable", "Veuillez vous reconnecter.");
            redirectToHome();
            return;
        }

        if (!validateForm()) {
            return;
        }

        currentUser.setUsername(tfUsername.getText().trim());
        currentUser.setEmail(tfEmail.getText().trim());
        currentUser.setFirst_name(tfFirstName.getText().trim());
        currentUser.setLast_name(tfLastName.getText().trim());
        currentUser.setPassword(pfPassword.getText().trim());
        currentUser.setProfile_image(selectedImagePath);

        try {
            UserCRUD.modifier(currentUser);
            User refreshedUser = UserCRUD.getById(currentUser.getId());

            if (refreshedUser != null) {
                SessionManager.setCurrentUser(refreshedUser);
                currentUser = refreshedUser;
                selectedImagePath = refreshedUser.getProfile_image();
                fillForm(currentUser);
                loadProfileImage(selectedImagePath);
            }

            pfPassword.clear();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Profil modifie avec succes.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur modification", e.getMessage());
            e.printStackTrace();
        }
    }

    private void fillForm(User User) {
        tfUsername.setText(User.getUsername());
        tfEmail.setText(User.getEmail());
        tfFirstName.setText(User.getFirst_name());
        tfLastName.setText(User.getLast_name());
        lblRole.setText(User.getRole());
        lblStatus.setText(User.getStatus());
    }

    private void loadProfileImage(String imagePath) {
        Image image = getImageOrDefault(imagePath);
        ivProfileImage.setImage(image);
        ivProfileImage.setClip(new Circle(75, 75, 75));
    }

    private Image getImageOrDefault(String imagePath) {
        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File file = new File(imagePath);

                if (file.exists()) {
                    try (FileInputStream inputStream = new FileInputStream(file)) {
                        return new Image(inputStream);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Image profil introuvable : " + e.getMessage());
        }

        return new Image(getClass().getResource("/images/default-user.png").toExternalForm());
    }

    private boolean validateForm() {
        if (tfUsername.getText().trim().isEmpty()
                || tfEmail.getText().trim().isEmpty()
                || tfFirstName.getText().trim().isEmpty()
                || tfLastName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs obligatoires", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (!isValidEmail(tfEmail.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", "Veuillez saisir un email valide.");
            return false;
        }

        return true;
    }

    private void redirectToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Scene scene = tfUsername.getScene();

            if (scene != null) {
                scene.setRoot(root);
            }
        } catch (Exception e) {
            System.out.println("Erreur redirection Home.fxml depuis profil");
            e.printStackTrace();
        }
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




