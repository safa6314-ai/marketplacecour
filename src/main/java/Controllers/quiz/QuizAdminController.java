package Controllers.quiz;

import Entities.Question;
import Entities.Reponse;
import Services.QuestionCRUD;
import Services.ReponseCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class QuizAdminController implements Initializable {

    @FXML private Label lblTotalQuestions;
    @FXML private Label lblTotalReponses;
    @FXML private Label lblTotalCorrectes;
    @FXML private Label lblStatus;
    @FXML private TextField searchField;
    @FXML private ListView<Question> listQuestions;
    @FXML private ListView<Reponse> listReponses;
    @FXML private TextArea questionField;
    @FXML private TextField categorieField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private TextField reponseField;
    @FXML private CheckBox correctCheck;

    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private final ReponseCRUD reponseCRUD = new ReponseCRUD();
    private List<Question> allQuestions = new ArrayList<>();
    private Question selectedQuestion;
    private Reponse selectedReponse;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        niveauCombo.setItems(FXCollections.observableArrayList("Facile", "Moyen", "Difficile"));
        niveauCombo.setValue("Facile");
        searchField.textProperty().addListener((obs, old, value) -> filtrerQuestions());
        listQuestions.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
            selectedQuestion = value;
            remplirQuestion(value);
            chargerReponses(value);
        });
        listReponses.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
            selectedReponse = value;
            remplirReponse(value);
        });
        listQuestions.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Question question, boolean empty) {
                super.updateItem(question, empty);
                setText(empty || question == null ? null : question.getContenu() + " - " + question.getCategorie() + " / " + question.getNiveau());
            }
        });
        listReponses.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Reponse reponse, boolean empty) {
                super.updateItem(reponse, empty);
                if (empty || reponse == null) {
                    setText(null);
                    getStyleClass().removeAll("quiz-correct-answer", "quiz-wrong-answer");
                    return;
                }
                setText((reponse.isCorrect() ? "Correct - " : "Faux - ") + reponse.getContenu());
                getStyleClass().removeAll("quiz-correct-answer", "quiz-wrong-answer");
                getStyleClass().add(reponse.isCorrect() ? "quiz-correct-answer" : "quiz-wrong-answer");
            }
        });
        rafraichir();
    }

    @FXML
    void rafraichir() {
        try {
            allQuestions = questionCRUD.afficher();
            List<Reponse> reponses = reponseCRUD.afficher();
            filtrerQuestions();
            lblTotalQuestions.setText(String.valueOf(allQuestions.size()));
            lblTotalReponses.setText(String.valueOf(reponses.size()));
            lblTotalCorrectes.setText(String.valueOf(reponses.stream().filter(Reponse::isCorrect).count()));
            lblStatus.setText("Pret.");
            if (!allQuestions.isEmpty() && selectedQuestion == null) {
                listQuestions.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            afficherErreur("Chargement impossible", e.getMessage());
        }
    }

    @FXML
    void nouvelleQuestion() {
        selectedQuestion = null;
        listQuestions.getSelectionModel().clearSelection();
        listReponses.getItems().clear();
        viderQuestion();
        viderReponse();
        lblStatus.setText("Nouveau formulaire pret.");
    }

    @FXML
    void ajouterQuestion() {
        String contenu = lire(questionField);
        String categorie = lire(categorieField);
        String niveau = niveauCombo.getValue();
        if (contenu.isEmpty() || categorie.isEmpty() || niveau == null) {
            lblStatus.setText("Question, categorie et niveau sont obligatoires.");
            return;
        }
        try {
            questionCRUD.ajouter(new Question(contenu, categorie, niveau));
            viderQuestion();
            selectedQuestion = null;
            rafraichir();
            lblStatus.setText("Question ajoutee.");
        } catch (SQLException e) {
            afficherErreur("Ajout impossible", e.getMessage());
        }
    }

    @FXML
    void modifierQuestion() {
        if (selectedQuestion == null) {
            lblStatus.setText("Selectionnez une question a modifier.");
            return;
        }
        String contenu = lire(questionField);
        String categorie = lire(categorieField);
        String niveau = niveauCombo.getValue();
        if (contenu.isEmpty() || categorie.isEmpty() || niveau == null) {
            lblStatus.setText("Question, categorie et niveau sont obligatoires.");
            return;
        }
        try {
            selectedQuestion.setContenu(contenu);
            selectedQuestion.setCategorie(categorie);
            selectedQuestion.setNiveau(niveau);
            questionCRUD.modifier(selectedQuestion);
            rafraichir();
            lblStatus.setText("Question modifiee.");
        } catch (SQLException e) {
            afficherErreur("Modification impossible", e.getMessage());
        }
    }

    @FXML
    void supprimerQuestion() {
        if (selectedQuestion == null || !confirmer("Supprimer cette question ?")) return;
        try {
            questionCRUD.supprimer(selectedQuestion.getId());
            selectedQuestion = null;
            viderQuestion();
            viderReponse();
            listReponses.getItems().clear();
            rafraichir();
            lblStatus.setText("Question supprimee.");
        } catch (SQLException e) {
            afficherErreur("Suppression impossible", e.getMessage());
        }
    }

    @FXML
    void ajouterReponse() {
        if (selectedQuestion == null) {
            lblStatus.setText("Selectionnez une question avant d'ajouter une reponse.");
            return;
        }
        String contenu = lire(reponseField);
        if (contenu.isEmpty()) {
            lblStatus.setText("La reponse est obligatoire.");
            return;
        }
        try {
            reponseCRUD.ajouter(new Reponse(contenu, correctCheck.isSelected(), selectedQuestion.getId()));
            viderReponse();
            chargerReponses(selectedQuestion);
            rafraichirCompteurs();
            lblStatus.setText("Reponse ajoutee.");
        } catch (SQLException e) {
            afficherErreur("Ajout impossible", e.getMessage());
        }
    }

    @FXML
    void modifierReponse() {
        if (selectedReponse == null) {
            lblStatus.setText("Selectionnez une reponse a modifier.");
            return;
        }
        String contenu = lire(reponseField);
        if (contenu.isEmpty()) {
            lblStatus.setText("La reponse est obligatoire.");
            return;
        }
        try {
            selectedReponse.setContenu(contenu);
            selectedReponse.setCorrect(correctCheck.isSelected());
            reponseCRUD.modifier(selectedReponse);
            viderReponse();
            chargerReponses(selectedQuestion);
            rafraichirCompteurs();
            lblStatus.setText("Reponse modifiee.");
        } catch (SQLException e) {
            afficherErreur("Modification impossible", e.getMessage());
        }
    }

    @FXML
    void supprimerReponse() {
        Reponse selected = listReponses.getSelectionModel().getSelectedItem();
        if (selected == null || !confirmer("Supprimer cette reponse ?")) return;
        try {
            reponseCRUD.supprimer(selected.getId());
            viderReponse();
            chargerReponses(selectedQuestion);
            rafraichirCompteurs();
            lblStatus.setText("Reponse supprimee.");
        } catch (SQLException e) {
            afficherErreur("Suppression impossible", e.getMessage());
        }
    }

    private void chargerReponses(Question question) {
        listReponses.getItems().clear();
        if (question == null) return;
        try {
            listReponses.getItems().setAll(reponseCRUD.afficherParQuestion(question.getId()));
        } catch (SQLException e) {
            afficherErreur("Chargement des reponses impossible", e.getMessage());
        }
    }

    private void rafraichirCompteurs() throws SQLException {
        lblTotalQuestions.setText(String.valueOf(questionCRUD.afficher().size()));
        List<Reponse> reponses = reponseCRUD.afficher();
        lblTotalReponses.setText(String.valueOf(reponses.size()));
        lblTotalCorrectes.setText(String.valueOf(reponses.stream().filter(Reponse::isCorrect).count()));
        lblStatus.setText("Operation effectuee.");
    }

    private void filtrerQuestions() {
        String query = lire(searchField).toLowerCase();
        if (query.isEmpty()) {
            listQuestions.getItems().setAll(allQuestions);
            return;
        }
        listQuestions.getItems().setAll(allQuestions.stream()
                .filter(q -> q.getContenu().toLowerCase().contains(query)
                        || q.getCategorie().toLowerCase().contains(query)
                        || q.getNiveau().toLowerCase().contains(query))
                .toList());
    }

    private void remplirQuestion(Question question) {
        if (question == null) {
            viderQuestion();
            return;
        }
        questionField.setText(question.getContenu());
        categorieField.setText(question.getCategorie());
        niveauCombo.setValue(question.getNiveau());
    }

    private void viderQuestion() {
        questionField.clear();
        categorieField.clear();
        niveauCombo.setValue("Facile");
    }

    private void remplirReponse(Reponse reponse) {
        if (reponse == null) {
            viderReponse();
            return;
        }
        reponseField.setText(reponse.getContenu());
        correctCheck.setSelected(reponse.isCorrect());
    }

    private void viderReponse() {
        selectedReponse = null;
        listReponses.getSelectionModel().clearSelection();
        reponseField.clear();
        correctCheck.setSelected(false);
    }

    private String lire(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String lire(TextArea field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private boolean confirmer(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void afficherErreur(String titre, String message) {
        lblStatus.setText(message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
