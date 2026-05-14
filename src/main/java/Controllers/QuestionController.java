package Controllers;

import Entities.Question;
import Services.QuestionService;
import Services.SpeechService;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static Controllers.ControllerSupport.*;

public class QuestionController {

    @FXML
    private TextField tfContenu;

    @FXML
    private TextField tfCategorie;

    @FXML
    private TextField tfNiveau;

    @FXML
    private TextField tfRecherche;

    @FXML
    private ComboBox<String> cbCategorieFiltre;

    @FXML
    private ComboBox<String> cbNiveauFiltre;

    @FXML
    private VBox questionsContainer;

    @FXML
    private Label lblResultats;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    private final QuestionService questionService = new QuestionService();
    private final SpeechService speechService = new SpeechService();
    private final ObservableList<Question> questions = FXCollections.observableArrayList();
    private final FilteredList<Question> questionsFiltrees = new FilteredList<>(questions);
    private Question selectedQuestion;

    @FXML
    void initialize() {
        cbCategorieFiltre.getItems().setAll("Toutes");
        cbCategorieFiltre.getSelectionModel().selectFirst();
        cbNiveauFiltre.getItems().setAll("Tous", "Facile", "Moyen", "Difficile");
        cbNiveauFiltre.getSelectionModel().selectFirst();

        tfRecherche.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        cbCategorieFiltre.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        cbNiveauFiltre.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());

        setSelection(null);
        chargerQuestions();
    }

    @FXML
    void ajouterQuestion(ActionEvent event) {
        if (!validerSaisie()) {
            return;
        }

        try {
            questionService.ajouter(new Question(
                    normaliserEspaces(tfContenu.getText()),
                    normaliserEspaces(tfCategorie.getText()),
                    normaliserNiveau(tfNiveau.getText())
            ));
            viderChamps();
            chargerQuestions();
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Question ajoutee avec succes.");
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void modifierQuestion(ActionEvent event) {
        if (selectedQuestion == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Selection", "Veuillez selectionner une question.");
            return;
        }

        if (!validerSaisie()) {
            return;
        }

        try {
            selectedQuestion.setContenu(normaliserEspaces(tfContenu.getText()));
            selectedQuestion.setCategorie(normaliserEspaces(tfCategorie.getText()));
            selectedQuestion.setNiveau(normaliserNiveau(tfNiveau.getText()));
            questionService.modifier(selectedQuestion);
            viderChamps();
            chargerQuestions();
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Question modifiee avec succes.");
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void supprimerQuestion(ActionEvent event) {
        if (selectedQuestion == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Selection", "Veuillez selectionner une question.");
            return;
        }

        try {
            questionService.supprimer(selectedQuestion.getId());
            viderChamps();
            chargerQuestions();
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Question supprimee avec ses reponses.");
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void actualiser(ActionEvent event) {
        viderChamps();
        chargerQuestions();
    }

    @FXML
    void allerVersReponses(ActionEvent event) {
        changerInterface(event, "Reponse.fxml");
    }

    @FXML
    void allerVersDashboard(ActionEvent event) {
        changerInterface(event, "GUI/AdminDashboard.fxml");
    }

    @FXML
    void allerVersUtilisateur(ActionEvent event) {
        changerInterface(event, "QuizUtilisateur.fxml");
    }

    @FXML
    void trierQuestionsParDifficulte(ActionEvent event) {
        try {
            questions.clear();
            questions.addAll(questionService.trierParDifficulte());
            appliquerFiltres();
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void lireQuestion(ActionEvent event) {
        String texte = tfContenu.getText();

        if (texte == null || texte.isBlank()) {
            afficherAlerte(Alert.AlertType.WARNING, "Lecture vocale", "Veuillez selectionner ou saisir une question.");
            return;
        }

        Thread speechThread = new Thread(() -> {
            try {
                speechService.speak(texte);
            } catch (Exception e) {
                Platform.runLater(() ->
                        afficherAlerte(Alert.AlertType.ERROR, "Lecture vocale", e.getMessage()));
            }
        }, "question-speech-thread");
        speechThread.setDaemon(true);
        speechThread.start();
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

        lblResultats.setText(questionsFiltrees.size() + " resultat(s)");

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

        Label contenu = new Label(question.getContenu());
        contenu.getStyleClass().add("card-title");
        contenu.setWrapText(true);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label categorie = creerBadge(question.getCategorie(), "badge-blue");
        Label niveau = creerBadge(question.getNiveau(), "badge-purple");
        Label id = creerBadge("ID " + question.getId(), "badge-muted");
        meta.getChildren().addAll(categorie, niveau, id);

        Button reponses = new Button("Reponses");
        reponses.getStyleClass().add("soft-btn");
        reponses.setOnAction(event -> changerInterface(event, "Reponse.fxml"));

        HBox footer = new HBox(reponses);
        footer.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(contenu, meta, footer);
        card.setOnMouseClicked(event -> {
            setSelection(question);
            afficherQuestionsFiltrees();
        });

        return card;
    }

    private boolean validerSaisie() {
        String question = normaliserEspaces(tfContenu.getText());
        String categorie = normaliserEspaces(tfCategorie.getText());
        String niveau = normaliserEspaces(tfNiveau.getText());

        if (!texteQuestionValide(question, 10)) {
            afficherAlerte(Alert.AlertType.ERROR, "Validation",
                    "La question est obligatoire, doit contenir au moins 10 caracteres, des lettres et un texte significatif.");
            return false;
        }

        if (!texteSignificatif(categorie) || !categorie.matches("[\\p{L}\\s'\\u2019-]{2,}")) {
            afficherAlerte(Alert.AlertType.ERROR, "Validation",
                    "La categorie est obligatoire et doit contenir un texte significatif, sans chiffres ni symboles seuls.");
            return false;
        }

        if (!niveauValide(niveau)) {
            afficherAlerte(Alert.AlertType.ERROR, "Validation",
                    "Le niveau doit etre exactement : Facile, Moyen ou Difficile.");
            return false;
        }

        return true;
    }

    private void setSelection(Question question) {
        selectedQuestion = question;
        btnModifier.setDisable(question == null);
        btnSupprimer.setDisable(question == null);

        if (question == null) {
            return;
        }

        tfContenu.setText(question.getContenu());
        tfCategorie.setText(question.getCategorie());
        tfNiveau.setText(question.getNiveau());
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

    private void viderChamps() {
        selectedQuestion = null;
        tfContenu.clear();
        tfCategorie.clear();
        tfNiveau.clear();
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

}
