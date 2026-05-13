package Controllers;

import Services.QuestionService;
import Services.ReponseService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import static Controllers.ControllerSupport.*;

public class AdminDashboardController {

    @FXML
    private Label lblQuestions;

    @FXML
    private Label lblReponses;

    @FXML
    private Label lblCategories;

    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();

    @FXML
    void initialize() {
        chargerStatistiques();
    }

    @FXML
    void allerVersQuestions(ActionEvent event) {
        changerInterface(event, "Question.fxml");
    }

    @FXML
    void allerVersReponses(ActionEvent event) {
        changerInterface(event, "Reponse.fxml");
    }

    @FXML
    void allerVersUtilisateur(ActionEvent event) {
        changerInterface(event, "QuizUtilisateur.fxml");
    }

    private void chargerStatistiques() {
        try {
            var questions = questionService.afficher();
            lblQuestions.setText(String.valueOf(questions.size()));
            lblReponses.setText(String.valueOf(reponseService.afficher().size()));
            lblCategories.setText(String.valueOf(questions.stream()
                    .map(question -> question.getCategorie() == null ? "" : question.getCategorie().trim())
                    .filter(categorie -> !categorie.isBlank())
                    .distinct()
                    .count()));
        } catch (Exception e) {
            lblQuestions.setText("0");
            lblReponses.setText("0");
            lblCategories.setText("0");
            afficherAlerte(Alert.AlertType.WARNING, "Base de donnees", e.getMessage());
        }
    }

}
