package Controllers.quiz;

import Entities.Question;
import Entities.Reponse;
import Services.QuestionCRUD;
import Services.ReponseCRUD;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class QuizClientController implements Initializable {

    @FXML private Label questionLabel;
    @FXML private Label metaLabel;
    @FXML private Label scoreLabel;
    @FXML private Label progressLabel;
    @FXML private Label statusLabel;
    @FXML private VBox reponsesBox;

    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private final ReponseCRUD reponseCRUD = new ReponseCRUD();
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private List<Question> questions = new ArrayList<>();
    private List<Reponse> currentReponses = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private boolean answered = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargerQuiz();
    }

    @FXML
    void verifier() {
        if (answered) {
            statusLabel.setText("Passez a la question suivante.");
            return;
        }
        RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
        if (selected == null) {
            statusLabel.setText("Choisissez une reponse.");
            return;
        }
        Reponse reponse = (Reponse) selected.getUserData();
        answered = true;
        if (reponse.isCorrect()) {
            score++;
            statusLabel.setText("Bonne reponse.");
            statusLabel.getStyleClass().setAll("status-accepted");
        } else {
            statusLabel.setText("Reponse incorrecte.");
            statusLabel.getStyleClass().setAll("status-refused");
        }
        afficherCorrection();
        afficherScore();
    }

    @FXML
    void precedente() {
        if (questions.isEmpty()) return;
        currentIndex = currentIndex == 0 ? questions.size() - 1 : currentIndex - 1;
        afficherQuestion();
    }

    @FXML
    void suivante() {
        if (questions.isEmpty()) return;
        if (currentIndex == questions.size() - 1 && answered) {
            afficherFin();
            return;
        }
        currentIndex = Math.min(currentIndex + 1, questions.size() - 1);
        afficherQuestion();
    }

    @FXML
    void recommencer() {
        currentIndex = 0;
        score = 0;
        afficherQuestion();
    }

    private void chargerQuiz() {
        try {
            questions = questionCRUD.afficher();
            afficherQuestion();
        } catch (SQLException e) {
            statusLabel.setText("Chargement impossible: " + e.getMessage());
        }
    }

    private void afficherQuestion() {
        reponsesBox.getChildren().clear();
        toggleGroup.getToggles().clear();
        answered = false;
        statusLabel.getStyleClass().setAll("status-pending");
        if (questions.isEmpty()) {
            questionLabel.setText("Aucune question disponible.");
            metaLabel.setText("Ajoutez des questions depuis Quiz Admin.");
            scoreLabel.setText("Score 0 / 0");
            progressLabel.setText("0 / 0");
            statusLabel.setText("En attente");
            return;
        }
        Question question = questions.get(currentIndex);
        questionLabel.setText(question.getContenu());
        metaLabel.setText(question.getCategorie() + " - " + question.getNiveau());
        progressLabel.setText("Question " + (currentIndex + 1) + " / " + questions.size());
        try {
            currentReponses = reponseCRUD.afficherParQuestion(question.getId());
            for (Reponse reponse : currentReponses) {
                RadioButton radio = new RadioButton(reponse.getContenu());
                radio.setToggleGroup(toggleGroup);
                radio.setUserData(reponse);
                radio.getStyleClass().add("quiz-option");
                reponsesBox.getChildren().add(radio);
            }
            statusLabel.setText(currentReponses.isEmpty() ? "Aucune reponse pour cette question." : "Choisissez une reponse.");
        } catch (SQLException e) {
            statusLabel.setText("Reponses indisponibles: " + e.getMessage());
        }
        afficherScore();
    }

    private void afficherScore() {
        scoreLabel.setText("Score " + score + " / " + questions.size());
    }

    private void afficherCorrection() {
        for (javafx.scene.Node node : reponsesBox.getChildren()) {
            if (!(node instanceof RadioButton radio)) continue;
            Reponse reponse = (Reponse) radio.getUserData();
            radio.setDisable(true);
            radio.getStyleClass().removeAll("quiz-option-correct", "quiz-option-wrong");
            if (reponse.isCorrect()) {
                radio.getStyleClass().add("quiz-option-correct");
            } else if (radio.isSelected()) {
                radio.getStyleClass().add("quiz-option-wrong");
            }
        }
    }

    private void afficherFin() {
        reponsesBox.getChildren().clear();
        questionLabel.setText("Quiz termine");
        metaLabel.setText("Resultat final");
        progressLabel.setText(questions.size() + " / " + questions.size());
        statusLabel.getStyleClass().setAll(score >= Math.ceil(questions.size() / 2.0) ? "status-accepted" : "status-refused");
        statusLabel.setText("Votre score final est " + score + " / " + questions.size() + ".");
        afficherScore();
    }
}
