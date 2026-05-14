package controllers;

import org.example.entities.Event;
import org.example.services.ServiceEvent;
import org.example.services.ServiceParticipation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.ResourceBundle;

public class AfficherEvenement implements Initializable {

    // ── FXML injectés ──
    @FXML private VBox       vboxListeEvents;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbFiltreType;
    @FXML private ComboBox<String> cbTri;
    @FXML private Label            lblCompteur;

    // ── Services ──
    private final ServiceEvent         serviceEvent         = new ServiceEvent();
    private final ServiceParticipation serviceParticipation = new ServiceParticipation();

    // ── État ──
    private ObservableList<Event> tousLesEvents      = FXCollections.observableArrayList();
    private ObservableList<Event> evenementsAffiches = FXCollections.observableArrayList();
    private Event                 evenementSelectionne = null;
    private boolean               isDarkMode           = false;

    // ── Largeurs des colonnes (doivent correspondre aux Label d'en-tête du FXML) ──
    private static final double W_ID       =  55;
    private static final double W_TITRE    = 190;
    private static final double W_DDEBUT  = 130;
    private static final double W_DFIN    = 130;
    private static final double W_LIEU    = 120;
    private static final double W_CAP     =  80;
    private static final double W_TYPE    = 110;
    private static final double W_STATUT  =  95;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbFiltreType.getItems().addAll("Tous", "exposition", "vente", "conférence", "atelier", "concert");
        cbFiltreType.setValue("Tous");

        cbTri.getItems().addAll(
                "Date début (croissant)",
                "Date début (décroissant)",
                "Titre (A-Z)",
                "Titre (Z-A)",
                "Capacité (croissant)",
                "Capacité (décroissant)"
        );
        cbTri.setValue("Date début (croissant)");

        chargerDonnees();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHARGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    public void chargerDonnees() {
        try {
            tousLesEvents = FXCollections.observableArrayList(serviceEvent.getAll());
            tfRecherche.clear();
            cbFiltreType.setValue("Tous");
            evenementsAffiches.setAll(tousLesEvents);
            afficherListe(evenementsAffiches);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger : " + e.getMessage());
        }
    }

    @FXML
    void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        Scene scene = vboxListeEvents.getScene();
        if (isDarkMode) {
            scene.getRoot().getStyleClass().add("dark-theme");
        } else {
            scene.getRoot().getStyleClass().remove("dark-theme");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AFFICHAGE DES LIGNES (HBox)
    // ─────────────────────────────────────────────────────────────────────────

    private void afficherListe(ObservableList<Event> liste) {
        evenementSelectionne = null;
        vboxListeEvents.getChildren().clear();

        for (Event ev : liste) {
            HBox row = buildRow(ev);
            vboxListeEvents.getChildren().add(row);
        }
        majCompteur(liste.size());
    }

    private HBox buildRow(Event ev) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 12, 9, 12));
        row.setSpacing(0);
        row.getStyleClass().add("event-row-card");

        row.getChildren().addAll(
                makeCell(String.valueOf(ev.getId_event()), W_ID),
                makeCell(ev.getTitre(),                    W_TITRE),
                makeCell(formatDate(ev.getDate_debut()),   W_DDEBUT),
                makeCell(formatDate(ev.getDate_fin()),     W_DFIN),
                makeCell(ev.getLieu(),                     W_LIEU),
                makeCell(String.valueOf(ev.getCapacite()), W_CAP),
                makeCell(ev.getType(),                     W_TYPE),
                makeCell(ev.getStatut(),                   W_STATUT)
        );

        row.setOnMouseClicked(e -> selectionner(row, ev));
        return row;
    }

    private void selectionner(HBox row, Event ev) {
        // Retirer la sélection visuelle de toutes les lignes
        vboxListeEvents.getChildren().forEach(n ->
                n.getStyleClass().remove("event-row-selected"));
        // Appliquer sur la ligne cliquée
        row.getStyleClass().add("event-row-selected");
        evenementSelectionne = ev;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RECHERCHE
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void rechercherEvent() {
        String motCle = tfRecherche.getText().trim().toLowerCase();
        if (motCle.isEmpty()) {
            appliquerFiltre();
            return;
        }
        ObservableList<Event> resultats = FXCollections.observableArrayList();
        for (Event e : tousLesEvents) {
            if (e.getTitre().toLowerCase().contains(motCle) ||
                    e.getLieu().toLowerCase().contains(motCle)) {
                resultats.add(e);
            }
        }
        evenementsAffiches.setAll(resultats);
        afficherListe(evenementsAffiches);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTRE PAR TYPE
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void filtrerParType() {
        appliquerFiltre();
    }

    private void appliquerFiltre() {
        String type   = cbFiltreType.getValue();
        String motCle = tfRecherche.getText().trim().toLowerCase();

        ObservableList<Event> filtres = FXCollections.observableArrayList();
        for (Event e : tousLesEvents) {
            boolean matchType  = type == null || type.equals("Tous") || e.getType().equals(type);
            boolean matchTitre = motCle.isEmpty()
                    || e.getTitre().toLowerCase().contains(motCle)
                    || e.getLieu().toLowerCase().contains(motCle);
            if (matchType && matchTitre) filtres.add(e);
        }
        evenementsAffiches.setAll(filtres);
        afficherListe(evenementsAffiches);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRI
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void trierTable() {
        String tri = cbTri.getValue();
        if (tri == null || evenementsAffiches.isEmpty()) return;

        switch (tri) {
            case "Date début (croissant)":
                evenementsAffiches.sort(Comparator.comparing(Event::getDate_debut));
                break;
            case "Date début (décroissant)":
                evenementsAffiches.sort(Comparator.comparing(Event::getDate_debut).reversed());
                break;
            case "Titre (A-Z)":
                evenementsAffiches.sort(Comparator.comparing(e -> e.getTitre().toLowerCase()));
                break;
            case "Titre (Z-A)":
                evenementsAffiches.sort((a, b) -> b.getTitre().compareToIgnoreCase(a.getTitre()));
                break;
            case "Capacité (croissant)":
                evenementsAffiches.sort(Comparator.comparingInt(Event::getCapacite));
                break;
            case "Capacité (décroissant)":
                evenementsAffiches.sort(Comparator.comparingInt(Event::getCapacite).reversed());
                break;
        }
        afficherListe(evenementsAffiches);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODIFIER
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void ouvrirModifier() {
        if (evenementSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un événement à modifier !");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvenement.fxml"));
            Parent root = loader.load();
            AjouterEvenement controller = loader.getController();
            controller.setEventAModifier(evenementSelectionne);
            Stage stage = new Stage();
            stage.setTitle("Modifier : " + evenementSelectionne.getTitre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerDonnees();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUPPRIMER
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void supprimerEvent() {
        if (evenementSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un événement à supprimer !");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'événement : \"" + evenementSelectionne.getTitre() + "\" ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceEvent.delete(evenementSelectionne);
                    tousLesEvents.remove(evenementSelectionne);
                    evenementSelectionne = null;
                    chargerDonnees();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement supprimé !");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RETOUR AJOUT
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void retourAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvenement.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un événement");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            ((Stage) scrollPane.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DÉCONNEXION
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void deconnexion() {
        try {
            Stage currentStage = (Stage) scrollPane.getScene().getWindow();
            currentStage.close();
            Home home = new Home();
            Stage newStage = new Stage();
            home.start(newStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITAIRES
    // ─────────────────────────────────────────────────────────────────────────

    /** Crée un Label à largeur fixe pour simuler une cellule de tableau. */
    private Label makeCell(String text, double width) {
        Label lbl = new Label(text != null ? text : "—");
        lbl.setPrefWidth(width);
        lbl.setMinWidth(width);
        lbl.setMaxWidth(width);
        lbl.setWrapText(false);
        lbl.setEllipsisString("…");
        return lbl;
    }

    /** Formate un LocalDateTime en chaîne lisible (ou "—" si null). */
    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void majCompteur(int nb) {
        lblCompteur.setText("Total : " + nb + " événement(s)");
    }

    private void showAlert(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}