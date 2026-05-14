package Controllers;

import Entities.Question;
import Entities.Reponse;
import Services.QuestionService;
import Services.ReponseService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static Controllers.ControllerSupport.*;

public class ReponseController {

    @FXML
    private TextField tfContenu;

    @FXML
    private CheckBox cbCorrect;

    @FXML
    private ComboBox<Question> cbQuestion;

    @FXML
    private TextField tfRecherche;

    @FXML
    private VBox reponsesContainer;

    @FXML
    private Label lblResultats;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    private final List<Question> questions = new ArrayList<>();
    private final List<Reponse> reponses = new ArrayList<>();
    private Reponse selectedReponse;

    @FXML
    void initialize() {
        configurerComboQuestions();
        tfRecherche.textProperty().addListener((observable, oldValue, newValue) -> afficherReponsesFiltrees());
        cbQuestion.valueProperty().addListener((observable, oldValue, newValue) -> afficherReponsesFiltrees());
        setSelection(null);
        chargerQuestions();
        chargerReponses();
    }

    @FXML
    void ajouterReponse(ActionEvent event) {
        Question question = cbQuestion.getValue();
        if (!validerSaisie()) {
            return;
        }

        try {
            reponseService.ajouterReponseQcm(new Reponse(normaliserEspaces(tfContenu.getText()), cbCorrect.isSelected(), question.getId()));
            viderChamps();
            chargerReponses();
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Reponse ajoutee avec succes.");
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void modifierReponse(ActionEvent event) {
        Question question = cbQuestion.getValue();
        if (selectedReponse == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Selection", "Veuillez selectionner une reponse.");
            return;
        }

        if (!validerSaisie()) {
            return;
        }

        try {
            selectedReponse.setContenu(normaliserEspaces(tfContenu.getText()));
            selectedReponse.setCorrect(cbCorrect.isSelected());
            selectedReponse.setQuestion_id(question.getId());
            reponseService.modifierReponseQcm(selectedReponse);
            viderChamps();
            chargerReponses();
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Reponse modifiee avec succes.");
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void supprimerReponse(ActionEvent event) {
        if (selectedReponse == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Selection", "Veuillez selectionner une reponse.");
            return;
        }

        try {
            reponseService.supprimer(selectedReponse.getId());
            viderChamps();
            chargerReponses();
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Reponse supprimee avec succes.");
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
        chargerReponses();
    }

    @FXML
    void allerVersQuestions(ActionEvent event) {
        changerInterface(event, "Question.fxml");
    }

    @FXML
    void allerVersDashboard(ActionEvent event) {
        changerInterface(event, "GUI/AdminDashboard.fxml");
    }

    @FXML
    void allerVersUtilisateur(ActionEvent event) {
        changerInterface(event, "QuizUtilisateur.fxml");
    }

    private void chargerQuestions() {
        try {
            questions.clear();
            questions.addAll(questionService.afficher().stream()
                    .filter(ControllerSupport::questionValidePourAffichage)
                    .toList());
            cbQuestion.getItems().setAll(questions);
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void chargerReponses() {
        try {
            reponses.clear();
            reponses.addAll(reponseService.afficher());
            afficherReponsesFiltrees();
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void afficherReponsesFiltrees() {
        reponsesContainer.getChildren().clear();
        Question questionFiltre = cbQuestion.getValue();
        List<Reponse> resultats = reponses.stream()
                .filter(reponse -> questionFiltre == null || reponse.getQuestion_id() == questionFiltre.getId())
                .filter(this::correspondRecherche)
                .toList();

        lblResultats.setText(resultats.size() + " resultat(s)");

        if (resultats.isEmpty()) {
            reponsesContainer.getChildren().add(creerMessageVide("Aucune réponse trouvée."));
            return;
        }

        for (Reponse reponse : resultats) {
            reponsesContainer.getChildren().add(creerCarteReponse(reponse));
        }
    }

    private Node creerCarteReponse(Reponse reponse) {
        VBox card = new VBox(8);
        card.getStyleClass().add("list-card");
        appliquerAnimationCarte(card);
        if (selectedReponse != null && selectedReponse.getId() == reponse.getId()) {
            card.getStyleClass().add("selected-card");
        }

        Label contenu = new Label(reponse.getContenu());
        contenu.getStyleClass().add("card-title");
        contenu.setWrapText(true);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getChildren().add(creerBadge(reponse.isCorrect() ? "Correcte" : "Incorrecte",
                reponse.isCorrect() ? "badge-green" : "badge-pink"));
        meta.getChildren().add(creerBadge("Question " + reponse.getQuestion_id(), "badge-muted"));

        card.getChildren().addAll(contenu, meta);
        card.setOnMouseClicked(event -> {
            setSelection(reponse);
            afficherReponsesFiltrees();
        });

        return card;
    }

    private void setSelection(Reponse reponse) {
        selectedReponse = reponse;
        btnModifier.setDisable(reponse == null);
        btnSupprimer.setDisable(reponse == null);

        if (reponse == null) {
            return;
        }

        tfContenu.setText(reponse.getContenu());
        cbCorrect.setSelected(reponse.isCorrect());
        selectionnerQuestion(reponse.getQuestion_id());
    }

    private boolean validerSaisie() {
        Question question = cbQuestion.getValue();
        String contenu = normaliserEspaces(tfContenu.getText());

        if (question == null) {
            afficherAlerte(Alert.AlertType.ERROR, "Validation", "Veuillez selectionner une question valide.");
            return false;
        }

        if (!texteSignificatif(contenu) || contenu.length() < 2) {
            afficherAlerte(Alert.AlertType.ERROR, "Validation",
                    "La reponse est obligatoire, doit contenir au moins 2 caracteres et ne peut pas etre uniquement numerique ou symbolique.");
            return false;
        }

        try {
            int idAExclure = selectedReponse == null ? 0 : selectedReponse.getId();
            if (reponseService.reponseExistePourQuestion(question.getId(), contenu, idAExclure)) {
                afficherAlerte(Alert.AlertType.ERROR, "Validation", "Cette reponse existe deja pour la question selectionnee.");
                return false;
            }
        } catch (SQLException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
            return false;
        }

        return true;
    }

    private void selectionnerQuestion(int questionId) {
        for (Question question : questions) {
            if (question.getId() == questionId) {
                cbQuestion.getSelectionModel().select(question);
                return;
            }
        }
    }

    private boolean correspondRecherche(Reponse reponse) {
        String recherche = normaliser(tfRecherche.getText());
        return recherche.isBlank() || normaliser(reponse.getContenu()).contains(recherche);
    }

    private void viderChamps() {
        selectedReponse = null;
        tfContenu.clear();
        cbCorrect.setSelected(false);
        cbQuestion.getSelectionModel().clearSelection();
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    private void configurerComboQuestions() {
        cbQuestion.setConverter(new StringConverter<>() {
            @Override
            public String toString(Question question) {
                if (question == null) {
                    return "";
                }
                return "#" + question.getId() + " - " + question.getContenu();
            }

            @Override
            public Question fromString(String string) {
                return null;
            }
        });

        cbQuestion.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Question question, boolean empty) {
                super.updateItem(question, empty);
                setText(empty || question == null ? null : "#" + question.getId() + " - " + question.getContenu());
            }
        });
    }

}
