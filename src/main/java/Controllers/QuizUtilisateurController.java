package Controllers;

import Entities.Achievement;
import Entities.Question;
import Entities.Reponse;
import Services.AchievementService;
import Services.QuestionService;
import Services.QuoteService;
import Services.ReponseService;
import Services.TranslationService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static Controllers.ControllerSupport.*;

public class QuizUtilisateurController {

    @FXML
    private TextField tfRecherche;

    @FXML
    private ComboBox<String> cbCategorieFiltre;

    @FXML
    private ComboBox<String> cbNiveauFiltre;

    @FXML
    private ComboBox<String> cbLangue;

    @FXML
    private VBox questionsContainer;

    @FXML
    private VBox reponsesContainer;

    @FXML
    private Button btnValiderReponse;

    @FXML
    private Button btnQuestionSuivante;

    @FXML
    private Label lblFeedbackReponse;

    @FXML
    private Label lblQuestionChoisie;

    @FXML
    private Label lblScore;

    @FXML
    private Label lblResultats;

    @FXML
    private Label lblCitation;

    @FXML
    private Label lblCitationAuteur;

    @FXML
    private ProgressBar progressTimer;

    @FXML
    private Label lblTempsRestant;

    @FXML
    private VBox achievementsContainer;

    @FXML
    private Label lblAchievementNotice;

    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    private final TranslationService translationService = new TranslationService();
    private final QuoteService quoteService = new QuoteService();
    private final AchievementService achievementService = new AchievementService();
    private final AchievementService.QuizStats quizStats = new AchievementService.QuizStats();
    private final ObservableList<Question> questions = FXCollections.observableArrayList();
    private final FilteredList<Question> questionsFiltrees = new FilteredList<>(questions);
    private final Set<Integer> reponsesCorrectesComptees = new HashSet<>();
    private final Set<Integer> questionsRepondues = new HashSet<>();
    private final ToggleGroup groupeReponses = new ToggleGroup();
    private Question selectedQuestion;
    private List<Reponse> reponsesQuestionCourante = List.of();
    private Timeline timer;
    private int dureeQuestion;
    private int tempsRestant;
    private boolean timerExpire;
    private int score;

    @FXML
    void initialize() {
        cbCategorieFiltre.getItems().setAll("Toutes");
        cbCategorieFiltre.getSelectionModel().selectFirst();
        cbNiveauFiltre.getItems().setAll("Tous", "Facile", "Moyen", "Difficile");
        cbNiveauFiltre.getSelectionModel().selectFirst();
        cbLangue.getItems().setAll("Fran\u00e7ais", "English", "\u0627\u0644\u0639\u0631\u0628\u064a\u0629");
        cbLangue.getSelectionModel().selectFirst();

        tfRecherche.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        cbCategorieFiltre.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        cbNiveauFiltre.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        cbLangue.valueProperty().addListener((observable, oldValue, newValue) -> rafraichirTraductions());

        mettreAJourScore();
        mettreAJourTimerVisuel();
        initialiserEtatValidation();
        afficherMessageReponses("Choisissez une question pour afficher ses reponses.");
        chargerCitation();
        afficherAchievements();
        chargerQuestions();
    }

    @FXML
    void allerVersAdmin(ActionEvent event) {
        changerInterface(event, "GUI/AdminDashboard.fxml");
    }

    @FXML
    void recommencer(ActionEvent event) {
        arreterTimer();
        score = 0;
        reponsesCorrectesComptees.clear();
        questionsRepondues.clear();
        selectedQuestion = null;
        reponsesQuestionCourante = List.of();
        timerExpire = false;
        mettreAJourScore();
        mettreAJourTimerVisuel();
        initialiserEtatValidation();
        lblQuestionChoisie.setText("Aucune question sélectionnée");
        afficherMessageReponses("Choisissez une question pour afficher ses reponses.");
        appliquerFiltres();
    }

    @FXML
    void validerReponse(ActionEvent event) {
        RadioButton selectedRadio = (RadioButton) groupeReponses.getSelectedToggle();
        if (selectedQuestion == null || selectedRadio == null) {
            afficherFeedback("Selectionnez une reponse avant de valider.", false);
            return;
        }

        if (timerExpire) {
            afficherFeedback("Temps ecoule : la question ne rapporte plus de point.", false);
            desactiverReponses();
            btnValiderReponse.setDisable(true);
            btnQuestionSuivante.setDisable(false);
            return;
        }

        Reponse reponse = (Reponse) selectedRadio.getUserData();
        boolean dejaValidee = !questionsRepondues.add(selectedQuestion.getId());
        if (!dejaValidee) {
            quizStats.incrementerQuestionsRepondues();
        }

        if (reponse.isCorrect()) {
            if (!dejaValidee && reponsesCorrectesComptees.add(reponse.getId())) {
                int bonusTemps = Math.max(0, tempsRestant / 5);
                score += 10 + bonusTemps;
                quizStats.incrementerBonnesReponses();
                quizStats.setDifficileReussi("difficile".equals(normaliser(selectedQuestion.getNiveau())));
                quizStats.setCategorieRenaissanceReussie(normaliser(selectedQuestion.getCategorie()).contains("renaissance"));
                mettreAJourScore();
                animerScore();
            }
            afficherFeedback("Bonne réponse", true);
        } else {
            if (!dejaValidee) {
                quizStats.incrementerErreurs();
            }
            afficherFeedback("Mauvaise réponse", false);
        }

        arreterTimer();
        verifierAchievements();
        desactiverReponses();
        btnValiderReponse.setDisable(true);
        btnQuestionSuivante.setDisable(false);
    }

    @FXML
    void questionSuivante(ActionEvent event) {
        passerQuestionSuivante();
    }

    private void chargerQuestions() {
        try {
            questions.clear();
            questions.addAll(questionService.afficher().stream()
                    .filter(ControllerSupport::questionValidePourAffichage)
                    .toList());
            chargerCategories();
            appliquerFiltres();
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void chargerCategories() {
        String selected = cbCategorieFiltre.getValue();
        List<String> categories = questions.stream()
                .map(Question::getCategorie)
                .filter(categorie -> categorie != null && !categorie.isBlank())
                .map(ControllerSupport::normaliserEspaces)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        categories.add(0, "Toutes");
        cbCategorieFiltre.getItems().setAll(categories);
        cbCategorieFiltre.getSelectionModel().select(categories.contains(selected) ? selected : "Toutes");
    }

    private void appliquerFiltres() {
        questionsFiltrees.setPredicate(question -> questionValidePourAffichage(question)
                && correspondRecherche(question)
                && correspondCategorie(question)
                && correspondNiveau(question));
        afficherQuestionsFiltrees();
    }

    private void afficherQuestionsFiltrees() {
        questionsContainer.getChildren().clear();

        lblResultats.setText(questionsFiltrees.size() + " question(s)");

        if (questionsFiltrees.isEmpty()) {
            questionsContainer.getChildren().add(creerMessageVide("Aucune question trouvée"));
            return;
        }

        for (Question question : questionsFiltrees) {
            questionsContainer.getChildren().add(creerCarteQuestion(question));
        }
    }

    private Node creerCarteQuestion(Question question) {
        VBox card = new VBox(8);
        card.getStyleClass().add("list-card");
        appliquerAnimationCarte(card);
        if (selectedQuestion != null && selectedQuestion.getId() == question.getId()) {
            card.getStyleClass().add("selected-card");
        }

        Label contenu = new Label(texteTraduit(question.getContenu()));
        contenu.getStyleClass().add("card-title");
        contenu.setWrapText(true);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getChildren().addAll(
                creerBadge(texteTraduit(question.getCategorie()), "badge-blue"),
                creerBadge(question.getNiveau(), "badge-purple")
        );

        card.getChildren().addAll(contenu, meta);
        card.setOnMouseClicked(event -> selectionnerQuestion(question));

        return card;
    }

    private void selectionnerQuestion(Question question) {
        arreterTimer();
        selectedQuestion = question;
        timerExpire = false;
        groupeReponses.selectToggle(null);
        lblFeedbackReponse.setText("");
        lblFeedbackReponse.getStyleClass().removeAll("feedback-success", "feedback-error");
        btnValiderReponse.setDisable(false);
        btnQuestionSuivante.setDisable(true);
        lblQuestionChoisie.setText(texteTraduit(question.getContenu()));
        afficherQuestionsFiltrees();
        chargerReponses(question);
        demarrerTimer(question);
    }

    private void chargerReponses(Question question) {
        reponsesContainer.getChildren().clear();
        try {
            reponsesQuestionCourante = reponseService.afficherParQuestionId(question.getId());
            if (reponsesQuestionCourante.isEmpty()) {
                afficherMessageReponses("Cette question n'a pas encore de reponses.");
                btnValiderReponse.setDisable(true);
                return;
            }

            for (Reponse reponse : reponsesQuestionCourante) {
                reponsesContainer.getChildren().add(creerChoixReponse(reponse));
            }
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private Node creerChoixReponse(Reponse reponse) {
        RadioButton radioButton = new RadioButton(texteTraduit(reponse.getContenu()));
        radioButton.getStyleClass().add("answer-choice");
        radioButton.setWrapText(true);
        radioButton.setToggleGroup(groupeReponses);
        radioButton.setUserData(reponse);
        return radioButton;
    }

    private void afficherMessageReponses(String message) {
        reponsesContainer.getChildren().clear();
        reponsesContainer.getChildren().add(creerMessageVide(message));
    }

    private void demarrerTimer(Question question) {
        dureeQuestion = dureeSelonNiveau(question.getNiveau());
        tempsRestant = dureeQuestion;
        mettreAJourTimerVisuel();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            tempsRestant--;
            mettreAJourTimerVisuel();
            if (tempsRestant <= 0) {
                gererTempsEcoule();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void gererTempsEcoule() {
        arreterTimer();
        timerExpire = true;
        if (selectedQuestion != null && questionsRepondues.add(selectedQuestion.getId())) {
            quizStats.incrementerQuestionsRepondues();
            quizStats.incrementerErreurs();
            verifierAchievements();
        }
        afficherFeedback("Temps écoulé", false);
        desactiverReponses();
        btnValiderReponse.setDisable(true);
        btnQuestionSuivante.setDisable(false);
    }

    private void passerQuestionSuivante() {
        if (selectedQuestion == null || questionsFiltrees.isEmpty()) {
            terminerQuiz();
            return;
        }

        int index = questionsFiltrees.indexOf(selectedQuestion);
        if (index >= 0 && index + 1 < questionsFiltrees.size()) {
            selectionnerQuestion(questionsFiltrees.get(index + 1));
        } else {
            terminerQuiz();
        }
    }

    private void terminerQuiz() {
        arreterTimer();
        selectedQuestion = null;
        lblQuestionChoisie.setText("Quiz terminé");
        afficherMessageReponses("Quiz terminé. Vous pouvez recommencer ou choisir une autre question.");
        initialiserEtatValidation();
        mettreAJourTimerVisuel();
        chargerCitation();
        afficherQuestionsFiltrees();
    }

    private void arreterTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private int dureeSelonNiveau(String niveau) {
        String valeur = normaliser(niveau);
        if (valeur.equals("difficile")) {
            return 10;
        }
        if (valeur.equals("moyen")) {
            return 20;
        }
        return 30;
    }

    private void mettreAJourTimerVisuel() {
        if (selectedQuestion == null || dureeQuestion <= 0) {
            lblTempsRestant.setText("-- s");
            progressTimer.setProgress(0);
            progressTimer.getStyleClass().remove("timer-danger");
            return;
        }

        lblTempsRestant.setText(tempsRestant + " s");
        progressTimer.setProgress(Math.max(0, (double) tempsRestant / dureeQuestion));
        progressTimer.getStyleClass().remove("timer-danger");
        if (tempsRestant <= 5) {
            progressTimer.getStyleClass().add("timer-danger");
        }
    }

    private void desactiverReponses() {
        for (Node node : reponsesContainer.getChildren()) {
            node.setDisable(true);
        }
    }

    private void initialiserEtatValidation() {
        groupeReponses.selectToggle(null);
        btnValiderReponse.setDisable(true);
        btnQuestionSuivante.setDisable(true);
        lblFeedbackReponse.setText("");
        lblFeedbackReponse.getStyleClass().removeAll("feedback-success", "feedback-error");
    }

    private void afficherFeedback(String message, boolean succes) {
        lblFeedbackReponse.setText(message);
        lblFeedbackReponse.getStyleClass().removeAll("feedback-success", "feedback-error");
        lblFeedbackReponse.getStyleClass().add(succes ? "feedback-success" : "feedback-error");

        ScaleTransition transition = new ScaleTransition(Duration.millis(160), lblFeedbackReponse);
        transition.setFromX(0.96);
        transition.setFromY(0.96);
        transition.setToX(1.0);
        transition.setToY(1.0);
        transition.play();
    }

    private void animerScore() {
        ScaleTransition transition = new ScaleTransition(Duration.millis(180), lblScore);
        transition.setFromX(1.0);
        transition.setFromY(1.0);
        transition.setToX(1.15);
        transition.setToY(1.15);
        transition.setAutoReverse(true);
        transition.setCycleCount(2);
        transition.play();
    }

    private void mettreAJourScore() {
        lblScore.setText(String.valueOf(score));
    }

    private void rafraichirTraductions() {
        appliquerFiltres();
        if (selectedQuestion != null) {
            lblQuestionChoisie.setText(texteTraduit(selectedQuestion.getContenu()));
            chargerReponses(selectedQuestion);
        }
    }

    private String texteTraduit(String texte) {
        String cible = langueSelectionnee();
        if (cible.equals("fr") || texte == null || texte.isBlank()) {
            return texte;
        }

        try {
            return translationService.traduire(texte, cible);
        } catch (Exception e) {
            return texte;
        }
    }

    private void chargerCitation() {
        CompletableFuture
                .supplyAsync(quoteService::citationAleatoire)
                .thenAccept(citation -> Platform.runLater(() -> afficherCitation(citation)));
    }

    private void afficherCitation(QuoteService.ArtQuote citation) {
        lblCitation.setText("\"" + citation.getQuote() + "\"");
        lblCitationAuteur.setText("- " + citation.getAuthor());
        FadeTransition fade = new FadeTransition(Duration.millis(420), lblCitation);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void verifierAchievements() {
        List<Achievement> debloques = achievementService.verifierSucces(quizStats);
        if (debloques.isEmpty()) {
            afficherAchievements();
            return;
        }

        Achievement achievement = debloques.get(0);
        lblAchievementNotice.setText("Succès débloqué : " + achievement.getTitre());
        FadeTransition fade = new FadeTransition(Duration.millis(350), lblAchievementNotice);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        afficherAchievements();
    }

    private void afficherAchievements() {
        achievementsContainer.getChildren().clear();
        List<Achievement> achievements = achievementService.afficherDebloques();
        if (achievements.isEmpty()) {
            achievementsContainer.getChildren().add(creerMessageVide("Aucun succès débloqué pour le moment."));
            return;
        }

        for (Achievement achievement : achievements) {
            VBox badge = new VBox(4);
            badge.getStyleClass().add("achievement-card");

            Label titre = new Label(achievement.getTitre());
            titre.getStyleClass().add("achievement-title");
            Label description = new Label(achievement.getDescription());
            description.getStyleClass().add("achievement-text");
            description.setWrapText(true);

            badge.getChildren().addAll(titre, description);
            appliquerAnimationCarte(badge);
            achievementsContainer.getChildren().add(badge);
        }
    }

    private String langueSelectionnee() {
        return translationService.codeLangue(cbLangue.getValue());
    }

    private boolean correspondRecherche(Question question) {
        String recherche = normaliser(tfRecherche.getText());
        return recherche.isBlank()
                || normaliser(question.getContenu()).contains(recherche)
                || normaliser(question.getCategorie()).contains(recherche)
                || normaliser(question.getNiveau()).contains(recherche);
    }

    private boolean correspondCategorie(Question question) {
        String filtre = normaliser(cbCategorieFiltre.getValue());
        return filtre.isBlank() || filtre.equals("toutes") || filtre.equals(normaliser(question.getCategorie()));
    }

    private boolean correspondNiveau(Question question) {
        String filtre = normaliser(cbNiveauFiltre.getValue());
        return filtre.isBlank() || filtre.equals("tous") || filtre.equals(normaliser(question.getNiveau()));
    }

}
