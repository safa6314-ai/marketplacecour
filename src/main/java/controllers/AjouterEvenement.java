package controllers;

import org.example.entities.Event;
import org.example.services.ServiceEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AjouterEvenement {

    // ── Champs du formulaire ──
    @FXML private TextField       tfTitre;
    @FXML private DatePicker      dpDateDebut;
    @FXML private DatePicker      dpDateFin;
    @FXML private TextField       tfLieu;
    @FXML private Spinner<Integer> spCapacite;
    @FXML private ComboBox<String> cbType;
    @FXML private CheckBox        cbStatut;

    // ── Labels d'erreur ──
    @FXML private Label lblErrTitre;
    @FXML private Label lblErrDateDebut;
    @FXML private Label lblErrDateFin;
    @FXML private Label lblErrLieu;
    @FXML private Label lblErrType;
    @FXML private Label lblErrGlobal;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private Event eventAModifier = null;

    @FXML
    void initialize() {
        // Remplir le ComboBox
        cbType.getItems().addAll("exposition", "vente", "conférence", "atelier", "concert");

        // Configurer le Spinner
        spCapacite.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 50));

        // ── Validation en temps réel ──
        tfTitre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().length() < 3 && !newVal.isEmpty()) {
                setErreur(lblErrTitre, "Min 3 caractères");
                setChampRouge(tfTitre);
            } else if (newVal.trim().isEmpty()) {
                setErreur(lblErrTitre, "Obligatoire !");
                setChampRouge(tfTitre);
            } else {
                clearErreur(lblErrTitre);
                setChampVert(tfTitre);
            }
        });

        tfLieu.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                setErreur(lblErrLieu, "Obligatoire !");
                setChampRouge(tfLieu);
            } else {
                clearErreur(lblErrLieu);
                setChampVert(tfLieu);
            }
        });

        dpDateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                setErreur(lblErrDateDebut, "Obligatoire !");
            } else if (newVal.isBefore(LocalDate.now())) {
                setErreur(lblErrDateDebut, "Date passée !");
            } else {
                clearErreur(lblErrDateDebut);
                verifierDates();
            }
        });

        dpDateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                setErreur(lblErrDateFin, "Obligatoire !");
            } else {
                clearErreur(lblErrDateFin);
                verifierDates();
            }
        });

        cbType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                setErreur(lblErrType, "Obligatoire !");
            } else {
                clearErreur(lblErrType);
            }
        });
    }

    private void verifierDates() {
        if (dpDateDebut.getValue() != null && dpDateFin.getValue() != null) {
            if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
                setErreur(lblErrDateFin, "Doit être après la date début !");
            } else {
                clearErreur(lblErrDateFin);
            }
        }
    }

    // ── Pré-remplir pour modification ──
    public void setEventAModifier(Event event) {
        this.eventAModifier = event;
        tfTitre.setText(event.getTitre());
        dpDateDebut.setValue(event.getDate_debut().toLocalDate());
        dpDateFin.setValue(event.getDate_fin().toLocalDate());
        tfLieu.setText(event.getLieu());
        spCapacite.getValueFactory().setValue(event.getCapacite());
        cbType.setValue(event.getType());
        cbStatut.setSelected("planifié".equals(event.getStatut()));
    }

    // ── Ajouter / Modifier ──
    @FXML
    void ajouterEvent() {
        lblErrGlobal.setText("");

        // Validation complète avant soumission
        boolean valide = true;

        if (tfTitre.getText().trim().length() < 3) {
            setErreur(lblErrTitre, tfTitre.getText().isEmpty() ? "Obligatoire !" : "Min 3 caractères");
            setChampRouge(tfTitre);
            valide = false;
        }
        if (dpDateDebut.getValue() == null) {
            setErreur(lblErrDateDebut, "Obligatoire !");
            valide = false;
        }
        if (dpDateFin.getValue() == null) {
            setErreur(lblErrDateFin, "Obligatoire !");
            valide = false;
        }
        if (tfLieu.getText().trim().isEmpty()) {
            setErreur(lblErrLieu, "Obligatoire !");
            setChampRouge(tfLieu);
            valide = false;
        }
        if (cbType.getValue() == null) {
            setErreur(lblErrType, "Obligatoire !");
            valide = false;
        }

        if (dpDateDebut.getValue() != null && dpDateFin.getValue() != null) {
            if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
                setErreur(lblErrDateFin, "Doit être après la date début !");
                valide = false;
            }
        }

        if (!valide) {
            lblErrGlobal.setText("Veuillez corriger les erreurs ci-dessus !");
            return;
        }

        LocalDateTime dateDebut = LocalDateTime.of(dpDateDebut.getValue(), LocalTime.of(0, 0));
        LocalDateTime dateFin   = LocalDateTime.of(dpDateFin.getValue(), LocalTime.of(23, 59));
        String statut = cbStatut.isSelected() ? "planifié" : "actif";

        try {
            if (eventAModifier == null) {
                // ADD
                Event e = new Event(
                        tfTitre.getText().trim(), dateDebut, dateFin,
                        tfLieu.getText().trim(), spCapacite.getValue(),
                        cbType.getValue(), statut
                );
                serviceEvent.add(e);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Événement ajouté avec succès !\nID = " + e.getId_event());
                clearFields();
            } else {
                // UPDATE
                eventAModifier.setTitre(tfTitre.getText().trim());
                eventAModifier.setDate_debut(dateDebut);
                eventAModifier.setDate_fin(dateFin);
                eventAModifier.setLieu(tfLieu.getText().trim());
                eventAModifier.setCapacite(spCapacite.getValue());
                eventAModifier.setType(cbType.getValue());
                eventAModifier.setStatut(statut);
                serviceEvent.update(eventAModifier);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès !");
                ((Stage) tfTitre.getScene().getWindow()).close();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    // ── Voir les événements ──
    @FXML
    void voirEvenements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEvenement.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Événements");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            ((Stage) tfTitre.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void deconnexion() {
        try {
            Stage currentStage = (Stage) tfTitre.getScene().getWindow();
            currentStage.close();
            
            Home home = new Home();
            Stage newStage = new Stage();
            home.start(newStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Vider les champs ──
    @FXML
    void viderChamps() {
        clearFields();
    }

    private void clearFields() {
        tfTitre.clear();
        tfLieu.clear();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        cbType.setValue(null);
        cbStatut.setSelected(false);
        spCapacite.getValueFactory().setValue(50);
        clearErreur(lblErrTitre);
        clearErreur(lblErrLieu);
        clearErreur(lblErrDateDebut);
        clearErreur(lblErrDateFin);
        clearErreur(lblErrType);
        lblErrGlobal.setText("");
        tfTitre.setStyle("");
        tfLieu.setStyle("");
    }

    // ── Helpers validation ──
    private void setErreur(Label lbl, String msg) {
        lbl.setText("⚠ " + msg);
    }

    private void clearErreur(Label lbl) {
        lbl.setText("");
    }

    private void setChampRouge(TextField tf) {
        tf.setStyle("-fx-border-color: red; -fx-border-width: 1.5px;");
    }

    private void setChampVert(TextField tf) {
        tf.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 1.5px;");
    }

    private void showAlert(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}