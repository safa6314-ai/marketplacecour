package controllers;

import Entites.User;
import Controllers.AdminController;
import Utils.SessionManager;
import Utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.controller.ArteviaMarketplaceController;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class AdminDashboardController {

    private static final double APP_WIDTH = 1100;
    private static final double APP_HEIGHT = 650;

    @FXML private StackPane centerPane;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnDashboardStats;
    @FXML private Button btnProfile;
    @FXML private Button btnMarketplace;
    @FXML private Button btnMarketplaceVente;
    @FXML private Button btnMarketplaceAchat;
    @FXML private Button btnCourses;
    @FXML private Button btnCoursesCours;
    @FXML private Button btnCoursesEtudiants;
    @FXML private Button btnCoursesRapport;
    @FXML private Button btnForum;
    @FXML private Button btnForumPoste;
    @FXML private Button btnForumLike;
    @FXML private Button btnForumCommentaire;
    @FXML private Button btnAbonnementMenu;
    @FXML private Button btnAbonnement;
    @FXML private Button btnAbonnementStatistique;
    @FXML private Button btnSouscription;
    @FXML private Button btnQuizMenu;
    @FXML private Button btnQuiz;
    @FXML private Button btnQuestions;
    @FXML private Button btnResultats;
    @FXML private Button btnEventsMenu;
    @FXML private Button btnEvenements;
    @FXML private Button btnReservations;
    @FXML private Button btnCalendrier;
    @FXML private Button btnSettings;
    @FXML private Button btnThemeToggle;

    @FXML private VBox dashboardSubmenu;
    @FXML private VBox marketplaceSubmenu;
    @FXML private VBox coursesSubmenu;
    @FXML private VBox forumSubmenu;
    @FXML private VBox abonnementSubmenu;
    @FXML private VBox quizSubmenu;
    @FXML private VBox eventsSubmenu;

    @FXML private FontIcon dashboardArrowIcon;
    @FXML private FontIcon marketplaceArrowIcon;
    @FXML private FontIcon coursesArrowIcon;
    @FXML private FontIcon forumArrowIcon;
    @FXML private FontIcon abonnementArrowIcon;
    @FXML private FontIcon quizArrowIcon;
    @FXML private FontIcon eventsArrowIcon;

    @FXML private Label lblAdminName;
    @FXML private ImageView ivAdminPhoto;
    @FXML private HBox adminProfileBox;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();

        if (user == null) {
            Platform.runLater(this::redirectToLogin);
            return;
        }

        showConnectedUser(user);
        SessionManager.addSessionListener(() -> Platform.runLater(this::refreshConnectedUser));
        configureSidebarByRole(user.getRole());
        Platform.runLater(() -> {
            ThemeManager.applySavedTheme(centerPane.getScene());
            updateThemeToggle();
        });
        openDefaultPage(user.getRole());
    }

    @FXML public void toggleDashboardMenu() { toggleMenu(dashboardSubmenu, dashboardArrowIcon, btnDashboard); }
    @FXML public void toggleMarketplaceMenu() { toggleMenu(marketplaceSubmenu, marketplaceArrowIcon, btnMarketplace); }
    @FXML public void toggleCoursesMenu() { toggleMenu(coursesSubmenu, coursesArrowIcon, btnCourses); }
    @FXML public void toggleForumMenu() { toggleMenu(forumSubmenu, forumArrowIcon, btnForum); }
    @FXML public void toggleAbonnementMenu() { toggleMenu(abonnementSubmenu, abonnementArrowIcon, btnAbonnementMenu); }
    @FXML public void toggleQuizMenu() { toggleMenu(quizSubmenu, quizArrowIcon, btnQuizMenu); }
    @FXML public void toggleEventsMenu() { toggleMenu(eventsSubmenu, eventsArrowIcon, btnEventsMenu); }

    @FXML public void showUsers() { openAdminOnly("/AdminUsers.fxml", btnUsers, "Gestion des membres"); }
    @FXML public void showDashboard() { openAdminOnly("/DashboardContent.fxml", btnDashboardStats, "Statistique"); }
    @FXML public void showProfile() { loadPage("/AdminProfile.fxml", btnProfile); }
    @FXML public void showProfileFromTopbar() { showProfile(); }

    @FXML public void openMarketplaceVente() { setMenuOpen(marketplaceSubmenu, marketplaceArrowIcon, true); loadMarketplacePage("/MarketplaceVente.fxml", true, btnMarketplaceVente); }
    @FXML public void openMarketplaceAchat() { setMenuOpen(marketplaceSubmenu, marketplaceArrowIcon, true); loadMarketplacePage("/MarketplaceAchat.fxml", false, btnMarketplaceAchat); }

    @FXML public void openCoursesCours() { loadPlaceholder("Cours", "Module Courses - Cours", btnCoursesCours); }
    @FXML public void openCoursesEtudiants() { loadPlaceholder("Etudiants", "Module Courses - Etudiants", btnCoursesEtudiants); }
    @FXML public void openCoursesRapport() { loadPlaceholder("Rapport", "Module Courses - Rapport", btnCoursesRapport); }

    @FXML
    public void openForumPoste() {
        setMenuOpen(forumSubmenu, forumArrowIcon, true);
        loadPage(isAdmin() ? "/post/AfficherPostAdmin.fxml" : "/post/AfficherPostClient.fxml", btnForumPoste);
    }

    @FXML public void openForumLike() { setMenuOpen(forumSubmenu, forumArrowIcon, true); loadPage("/post/AfficherLike.fxml", btnForumLike); }
    @FXML public void openForumCommentaire() { setMenuOpen(forumSubmenu, forumArrowIcon, true); loadPage("/commentaire/AfficherCommentaire.fxml", btnForumCommentaire); }

    @FXML public void openAbonnement() { loadAbonnementPage("abonnement", btnAbonnement); }
    @FXML public void openAbonnementStatistique() { loadAbonnementPage("statistique", btnAbonnementStatistique); }
    @FXML public void openSouscription() { loadAbonnementPage("souscription", btnSouscription); }

    @FXML public void openQuiz() { loadPlaceholder("Quiz", "Module Quiz", btnQuiz); }
    @FXML public void openQuestions() { loadPlaceholder("Questions", "Module Quiz - Questions", btnQuestions); }
    @FXML public void openResultats() { loadPlaceholder("Resultats", "Module Quiz - Resultats", btnResultats); }

    @FXML public void openEvenements() { loadPlaceholder("Evenements", "Module Events - Evenements", btnEvenements); }
    @FXML public void openReservations() { loadPlaceholder("Reservations", "Module Events - Reservations", btnReservations); }
    @FXML public void openCalendrier() { loadPlaceholder("Calendrier", "Module Events - Calendrier", btnCalendrier); }

    @FXML public void openSettings() { loadPlaceholder("Settings", "Parametres du compte et de l'application", btnSettings); }

    @FXML
    public void logout(ActionEvent event) {
        SessionManager.clearSession();
        switchRoot(event, "/WelcomePage.fxml", "Artevia");
    }

    public void loadPage(String fxmlPath) {
        loadPage(fxmlPath, null);
    }

    private void loadPage(String fxmlPath, Button activeButton) {
        if (SessionManager.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IOException("FXML introuvable : " + fxmlPath);
            }

            Parent page = FXMLLoader.load(url);
            ThemeManager.applySavedTheme(page);
            centerPane.getChildren().setAll(page);
            setActiveButton(activeButton);
            playFade(page);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMarketplacePage(String fxmlPath, boolean venteMode, Button activeButton) {
        if (SessionManager.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IOException("FXML introuvable : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();
            ArteviaMarketplaceController controller = loader.getController();

            if (controller != null) {
                if (venteMode) {
                    controller.showVenteFromMainSidebar();
                } else {
                    controller.showAchatFromMainSidebar();
                }
            }

            ThemeManager.applySavedTheme(page);
            centerPane.getChildren().setAll(page);
            setActiveButton(activeButton);
            playFade(page);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Marketplace", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAbonnementPage(String mode, Button activeButton) {
        if (!isAdmin()) {
            loadPage("/GUI/ClientDashboard.fxml", activeButton);
            return;
        }

        try {
            URL url = getClass().getResource("/GUI/AdminDashboard.fxml");
            if (url == null) {
                throw new IOException("FXML introuvable : /GUI/AdminDashboard.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();
            AdminController controller = loader.getController();
            if (controller != null) {
                switch (mode) {
                    case "abonnement" -> controller.showAbonnements(null);
                    case "souscription" -> controller.showSouscriptions(null);
                    default -> controller.showStatistiques(null);
                }
            }

            ThemeManager.applySavedTheme(page);
            centerPane.getChildren().setAll(page);
            setActiveButton(activeButton);
            playFade(page);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Abonnement", e.getMessage());
            e.printStackTrace();
        }
    }

    private void openAdminOnly(String fxmlPath, Button activeButton, String title) {
        if (!isAdmin()) {
            loadPlaceholder(title, "Acces reserve aux administrateurs.", activeButton);
            return;
        }
        loadPage(fxmlPath, activeButton);
    }

    private void configureSidebarByRole(String role) {
        boolean admin = "ADMIN".equalsIgnoreCase(role == null ? "" : role.trim());
        btnUsers.setVisible(admin);
        btnUsers.setManaged(admin);
        btnDashboardStats.setVisible(admin);
        btnDashboardStats.setManaged(admin);
        btnAbonnementStatistique.setVisible(admin);
        btnAbonnementStatistique.setManaged(admin);
    }

    private void openDefaultPage(String role) {
        setMenuOpen(dashboardSubmenu, dashboardArrowIcon, true);
        if ("ADMIN".equalsIgnoreCase(role == null ? "" : role.trim())) {
            showUsers();
            return;
        }
        showProfile();
    }

    private void toggleMenu(VBox submenu, FontIcon arrow, Button parentButton) {
        setMenuOpen(submenu, arrow, !submenu.isVisible());
        setActiveButton(parentButton);
    }

    private void setMenuOpen(VBox submenu, FontIcon arrow, boolean open) {
        submenu.setVisible(open);
        submenu.setManaged(open);

        RotateTransition rotate = new RotateTransition(Duration.millis(160), arrow);
        rotate.setToAngle(open ? 90 : 0);
        rotate.play();
    }

    private void setActiveButton(Button activeButton) {
        for (Button button : getAllSidebarButtons()) {
            button.getStyleClass().removeAll("sidebar-nav-button-active", "sidebar-submenu-button-active");
        }

        if (activeButton == null) {
            return;
        }

        if (getSubmenuButtons().contains(activeButton)) {
            activeButton.getStyleClass().add("sidebar-submenu-button-active");
            Button parent = parentFor(activeButton);
            if (parent != null) {
                parent.getStyleClass().add("sidebar-nav-button-active");
            }
        } else {
            activeButton.getStyleClass().add("sidebar-nav-button-active");
        }
    }

    private Button parentFor(Button child) {
        if (Arrays.asList(btnUsers, btnDashboardStats, btnProfile).contains(child)) return btnDashboard;
        if (Arrays.asList(btnMarketplaceVente, btnMarketplaceAchat).contains(child)) return btnMarketplace;
        if (Arrays.asList(btnCoursesCours, btnCoursesEtudiants, btnCoursesRapport).contains(child)) return btnCourses;
        if (Arrays.asList(btnForumPoste, btnForumLike, btnForumCommentaire).contains(child)) return btnForum;
        if (Arrays.asList(btnAbonnement, btnAbonnementStatistique, btnSouscription).contains(child)) return btnAbonnementMenu;
        if (Arrays.asList(btnQuiz, btnQuestions, btnResultats).contains(child)) return btnQuizMenu;
        if (Arrays.asList(btnEvenements, btnReservations, btnCalendrier).contains(child)) return btnEventsMenu;
        return null;
    }

    private List<Button> getAllSidebarButtons() {
        return Arrays.asList(
                btnDashboard, btnUsers, btnDashboardStats, btnProfile,
                btnMarketplace, btnMarketplaceVente, btnMarketplaceAchat,
                btnCourses, btnCoursesCours, btnCoursesEtudiants, btnCoursesRapport,
                btnForum, btnForumPoste, btnForumLike, btnForumCommentaire,
                btnAbonnementMenu, btnAbonnement, btnAbonnementStatistique, btnSouscription,
                btnQuizMenu, btnQuiz, btnQuestions, btnResultats,
                btnEventsMenu, btnEvenements, btnReservations, btnCalendrier,
                btnSettings
        );
    }

    private List<Button> getSubmenuButtons() {
        return Arrays.asList(
                btnUsers, btnDashboardStats, btnProfile,
                btnMarketplaceVente, btnMarketplaceAchat,
                btnCoursesCours, btnCoursesEtudiants, btnCoursesRapport,
                btnForumPoste, btnForumLike, btnForumCommentaire,
                btnAbonnement, btnAbonnementStatistique, btnSouscription,
                btnQuiz, btnQuestions, btnResultats,
                btnEvenements, btnReservations, btnCalendrier
        );
    }

    private void loadPlaceholder(String title, String subtitle, Button activeButton) {
        VBox box = new VBox(10);
        box.getStyleClass().add("module-placeholder");
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(box, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("module-placeholder-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("module-placeholder-subtitle");
        subtitleLabel.setWrapText(true);

        box.getChildren().addAll(titleLabel, subtitleLabel);
        centerPane.getChildren().setAll(box);
        setActiveButton(activeButton);
        playFade(box);
    }

    @FXML
    private void toggleTheme() {
        String theme = ThemeManager.toggleTheme(centerPane.getScene());
        for (Node child : centerPane.getChildren()) {
            if (child instanceof Parent) {
                ThemeManager.applyTheme((Parent) child, theme);
            }
        }
        updateThemeToggle();
    }

    private void playFade(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(180), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private boolean isAdmin() {
        User user = SessionManager.getCurrentUser();
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private void showConnectedUser(User user) {
        lblAdminName.setText(user.getUsername() == null ? "User" : user.getUsername());
        ivAdminPhoto.setImage(getImageOrDefault(user.getProfile_image()));
        ivAdminPhoto.setClip(new Circle(20, 20, 20));
    }

    private void refreshConnectedUser() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            showConnectedUser(user);
        }
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

    private void redirectToLogin() {
        try {
            URL url = getClass().getResource("/WelcomePage.fxml");
            if (url == null) {
                throw new IOException("FXML introuvable : /WelcomePage.fxml");
            }

            Parent root = FXMLLoader.load(url);
            Scene scene = centerPane.getScene();
            if (scene != null) {
                scene.setRoot(root);
                ThemeManager.applySavedTheme(scene);
            }
        } catch (Exception e) {
            System.out.println("Erreur redirection WelcomePage.fxml");
            e.printStackTrace();
        }
    }

    private void updateThemeToggle() {
        if (btnThemeToggle == null) {
            return;
        }

        if (ThemeManager.isDarkMode()) {
            btnThemeToggle.setText("Light");
            btnThemeToggle.setGraphic(new FontIcon("fas-sun"));
        } else {
            btnThemeToggle.setText("Dark");
            btnThemeToggle.setGraphic(new FontIcon("fas-moon"));
        }
    }

    private void switchRoot(ActionEvent event, String fxmlPath, String title) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IOException("FXML introuvable : " + fxmlPath);
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            ThemeManager.applySavedTheme(scene);
            stage.setTitle(title);
            stage.setMinWidth(APP_WIDTH);
            stage.setMinHeight(APP_HEIGHT);
            stage.setMaximized(false);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur navigation", e.getMessage());
            e.printStackTrace();
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
