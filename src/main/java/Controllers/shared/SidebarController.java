package Controllers.shared;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.List;

public class SidebarController {
    @FXML private Label modeSubtitle;
    @FXML private Button dashboardButton;
    @FXML private Button forumButton;
    @FXML private Button postsButton;
    @FXML private Button commentsButton;
    @FXML private Button likesButton;
    @FXML private Button abonnementButton;
    @FXML private Button abonnementPlansButton;
    @FXML private Button souscriptionsButton;
    @FXML private Button coursButton;
    @FXML private Button marketplaceButton;
    @FXML private Button eventsButton;
    @FXML private Button quizButton;
    @FXML private Button quizQuestionsButton;
    @FXML private Button quizReponsesButton;
    @FXML private Button switchModeButton;

    private AppShellController shell;

    void setShell(AppShellController shell) {
        this.shell = shell;
    }

    @FXML
    void showDashboard() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("dashboard");
        shell.loadPage("/shared/DashboardHub.fxml", "dashboard");
    }

    @FXML
    void navigateForumAdmin() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("posts");
        shell.loadPage("/post/AfficherPostAdmin.fxml", "forum");
    }

    @FXML
    void navigateForumClient() {
        AppState.setAdminMode(false);
        AppState.setCurrentSection("posts");
        shell.loadPage("/post/AfficherPostClient.fxml", "forum");
    }

    @FXML
    void navigateComments() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("comments");
        shell.loadPage("/post/AfficherPostAdmin.fxml", "forum");
    }

    @FXML
    void navigateLikes() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("likes");
        shell.loadPage("/post/AfficherPostAdmin.fxml", "forum");
    }

    @FXML
    void navigateAbonnements() {
        AppState.setCurrentSection("dashboard");
        shell.loadPage(AppState.isAdminMode() ? "/GUI/AdminDashboard.fxml" : "/GUI/ClientDashboard.fxml", "abonnement");
    }

    @FXML
    void navigateAbonnementPlans() {
        AppState.setCurrentSection("abonnements");
        shell.loadPage(AppState.isAdminMode() ? "/GUI/AdminDashboard.fxml" : "/GUI/ClientDashboard.fxml", "abonnement");
    }

    @FXML
    void navigateSouscriptions() {
        AppState.setCurrentSection("souscriptions");
        shell.loadPage(AppState.isAdminMode() ? "/GUI/AdminDashboard.fxml" : "/GUI/ClientDashboard.fxml", "abonnement");
    }

    @FXML
    void navigateCours() {
        shell.loadPage("/placeholders/CoursPlaceholder.fxml", "cours");
    }

    @FXML
    void navigateMarketplace() {
        shell.loadPage("/placeholders/MarketplacePlaceholder.fxml", "marketplace");
    }

    @FXML
    void navigateEvents() {
        shell.loadPage("/placeholders/EventsPlaceholder.fxml", "events");
    }

    @FXML
    void navigateQuiz() {
        AppState.setCurrentSection("dashboard");
        shell.loadPage(AppState.isAdminMode() ? "/AdminDashboard.fxml" : "/QuizUtilisateur.fxml", "quiz");
    }

    @FXML
    void navigateQuizQuestions() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("questions");
        shell.loadPage("/Question.fxml", "quiz");
    }

    @FXML
    void navigateQuizReponses() {
        AppState.setAdminMode(true);
        AppState.setCurrentSection("reponses");
        shell.loadPage("/Reponse.fxml", "quiz");
    }

    @FXML
    void switchMode() {
        NavigationService.switchMode();
    }

    void refresh() {
        modeSubtitle.setText(AppState.isAdminMode() ? "Administration" : "Client");
        switchModeButton.setText(AppState.isAdminMode() ? "↗ Switch To Client View" : "↗ Switch To Admin View");

        List<Button> buttons = List.of(
                dashboardButton, forumButton, postsButton, commentsButton, likesButton,
                abonnementButton, abonnementPlansButton, souscriptionsButton,
                coursButton, marketplaceButton, eventsButton, quizButton,
                quizQuestionsButton, quizReponsesButton
        );
        buttons.forEach(button -> button.getStyleClass().remove("unified-nav-active"));

        String module = AppState.getCurrentModule();
        if ("forum".equals(module)) {
            forumButton.getStyleClass().add("unified-nav-active");
            if ("comments".equals(AppState.getCurrentSection())) {
                commentsButton.getStyleClass().add("unified-nav-active");
            } else if ("likes".equals(AppState.getCurrentSection())) {
                likesButton.getStyleClass().add("unified-nav-active");
            } else {
                postsButton.getStyleClass().add("unified-nav-active");
            }
        } else if ("abonnement".equals(module)) {
            abonnementButton.getStyleClass().add("unified-nav-active");
            if ("souscriptions".equals(AppState.getCurrentSection())) {
                souscriptionsButton.getStyleClass().add("unified-nav-active");
            } else if ("abonnements".equals(AppState.getCurrentSection())) {
                abonnementPlansButton.getStyleClass().add("unified-nav-active");
            }
        } else if ("cours".equals(module)) {
            coursButton.getStyleClass().add("unified-nav-active");
        } else if ("marketplace".equals(module)) {
            marketplaceButton.getStyleClass().add("unified-nav-active");
        } else if ("events".equals(module)) {
            eventsButton.getStyleClass().add("unified-nav-active");
        } else if ("quiz".equals(module)) {
            quizButton.getStyleClass().add("unified-nav-active");
            if ("questions".equals(AppState.getCurrentSection())) {
                quizQuestionsButton.getStyleClass().add("unified-nav-active");
            } else if ("reponses".equals(AppState.getCurrentSection())) {
                quizReponsesButton.getStyleClass().add("unified-nav-active");
            }
        } else {
            dashboardButton.getStyleClass().add("unified-nav-active");
        }
    }
}
