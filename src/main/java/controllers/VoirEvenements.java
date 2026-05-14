package controllers;

import org.example.entities.Event;
import org.example.entities.Participation;
import org.example.services.ServiceEvent;
import org.example.services.ServiceParticipation;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.concurrent.Task;
import javafx.application.Platform;
import org.example.services.GeocodingService;
import org.example.services.WeatherService;
import org.example.utils.ToastUtil;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class VoirEvenements implements Initializable {

    // ── FXML — liste ──────────────────────────────────────────────────────────
    @FXML private VBox        vboxListeEvents;
    @FXML private ScrollPane  scrollPane;
    @FXML private Label       lblCompteur;
    @FXML private TextField   tfSearch;
    @FXML private ComboBox<String> cbTypeFilter;
    @FXML private Button      btnPrev;
    @FXML private Button      btnNext;
    @FXML private Label       lblPageInfo;

    // ── FXML — overlay + panneau ──────────────────────────────────────────────
    @FXML private AnchorPane  overlayPane;
    @FXML private AnchorPane  detailPane;

    // ── FXML — contenu du panneau ─────────────────────────────────────────────
    @FXML private Label       detailChipType;
    @FXML private Label       detailTitre;
    @FXML private Label       detailDateDebut;
    @FXML private Label       detailDateFin;
    @FXML private Label       detailLieu;
    @FXML private Label       detailStatut;
    @FXML private Label       detailPlacesLabel;
    @FXML private AnchorPane  barreRemplissage;
    @FXML private Label       detailStatutInscription;
    @FXML private Button      btnSinscrire;
    @FXML private Button      btnAnnuler;

    // ── FXML — Météo & Carte ──
    @FXML private VBox              weatherContainer;
    @FXML private Label             lblWeatherEmoji;
    @FXML private Label             lblWeatherDesc;
    @FXML private Label             lblWeatherTemp;
    @FXML private Label             lblWeatherPrecip;
    @FXML private Label             lblWeatherStatus;
    @FXML private WebView           mapWebView;
    @FXML private Label             lblMapError;
    @FXML private ProgressIndicator mapProgress;

    // ── Services ──────────────────────────────────────────────────────────────
    private final ServiceEvent         serviceEvent         = new ServiceEvent();
    private final ServiceParticipation serviceParticipation = new ServiceParticipation();

    // ── État ──────────────────────────────────────────────────────────────────
    private ObservableList<Event> tousLesEvents       = FXCollections.observableArrayList();
    private ObservableList<Event> filteredEvents      = FXCollections.observableArrayList();
    private Event                 evenementSelectionne = null;
    private boolean               panneauOuvert        = false;
    private boolean               isDarkMode           = false;

    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;

    // ── Largeurs colonnes ─────────────────────────────────────────────────────
    private static final double W_TITRE  = 220;
    private static final double W_DDEBUT = 150;
    private static final double W_DFIN   = 150;
    private static final double W_LIEU   = 150;
    private static final double W_PLACES = 130;
    private static final double W_TYPE   = 150;

    // ── Largeur du panneau latéral (doit correspondre au prefWidth du FXML) ──
    private static final double PANEL_WIDTH = 370;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Panneau hors écran à droite dès le départ
        detailPane.setTranslateX(PANEL_WIDTH);

        // Initialiser Filtre Type
        cbTypeFilter.getItems().addAll("Tous", "exposition", "vente", "conférence", "atelier", "concert");
        cbTypeFilter.setValue("Tous");

        chargerDonnees();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHARGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    private void chargerDonnees() {
        try {
            tousLesEvents = FXCollections.observableArrayList(serviceEvent.getAll());
            applyFilters();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les événements : " + e.getMessage());
        }
    }

    @FXML
    private void applyFilters() {
        String search = tfSearch.getText().toLowerCase().trim();
        String type = cbTypeFilter.getValue();

        filteredEvents = tousLesEvents.filtered(ev -> {
            boolean matchesSearch = ev.getTitre().toLowerCase().contains(search) ||
                    ev.getLieu().toLowerCase().contains(search);
            boolean matchesType = type == null || type.equals("Tous") || ev.getType().equalsIgnoreCase(type);

            return matchesSearch && matchesType;
        });

        currentPage = 1;
        afficherPage();
    }

    private void afficherPage() {
        int totalEvents = filteredEvents.size();
        int totalPages = (int) Math.ceil((double) totalEvents / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalEvents);

        ObservableList<Event> pageItems = FXCollections.observableArrayList();
        if (start < totalEvents) {
            pageItems.addAll(filteredEvents.subList(start, end));
        }

        afficherListe(pageItems);

        // UI Pagination
        lblPageInfo.setText(String.format("Page %d sur %d", currentPage, totalPages));
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
        
        lblCompteur.setText("Total : " + totalEvents + " événement(s)");
    }

    @FXML
    void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            afficherPage();
        }
    }

    @FXML
    void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredEvents.size() / PAGE_SIZE);
        if (currentPage < totalPages) {
            currentPage++;
            afficherPage();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AFFICHAGE DES LIGNES
    // ─────────────────────────────────────────────────────────────────────────

    private void afficherListe(ObservableList<Event> liste) {
        vboxListeEvents.getChildren().clear();

        for (int i = 0; i < liste.size(); i++) {
            Event ev = liste.get(i);
            HBox row = buildRow(ev, i);
            vboxListeEvents.getChildren().add(row);
        }
    }

    private HBox buildRow(Event ev, int index) {
        int placesRestantes = calculerPlacesRestantes(ev);

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 12, 9, 12));
        row.setSpacing(0);
        row.getStyleClass().add("event-row-card");
        if (index % 2 == 1) row.getStyleClass().add("event-row-alt");

        // Cellule places colorée
        String placesStr = placesRestantes == 0 ? "Complet"
                : placesRestantes + " place(s)";
        Label lblPlaces = makeCell(placesStr, W_PLACES);
        if (placesRestantes == 0) {
            lblPlaces.setStyle("-fx-text-fill:#e53935; -fx-font-weight:bold;");
        } else if (placesRestantes <= 5) {
            lblPlaces.setStyle("-fx-text-fill:#f57c00; -fx-font-weight:bold;");
        } else {
            lblPlaces.setStyle("-fx-text-fill:#2e7d32;");
        }

        row.getChildren().addAll(
                makeCell(ev.getTitre(),                  W_TITRE),
                makeCell(formatDate(ev.getDate_debut()), W_DDEBUT),
                makeCell(formatDate(ev.getDate_fin()),   W_DFIN),
                makeCell(ev.getLieu(),                   W_LIEU),
                lblPlaces,
                makeCell(ev.getType(),                   W_TYPE)
        );

        // Clic → ouvrir panneau latéral
        row.setOnMouseClicked(e -> {
            deselectAll();
            row.getStyleClass().add("event-row-selected");
            evenementSelectionne = ev;
            ouvrirPanneau(ev);
        });

        return row;
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

    @FXML
    void ouvrirCalendrier() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/CalendrierEvenements.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Mon Calendrier");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PANNEAU LATÉRAL — OUVERTURE
    // ─────────────────────────────────────────────────────────────────────────

    private void ouvrirPanneau(Event ev) {
        remplirPanneau(ev);

        // Rendre visible avant d'animer
        detailPane.setVisible(true);
        overlayPane.setVisible(true);

        // Animation slide depuis la droite
        TranslateTransition slide = new TranslateTransition(Duration.millis(280), detailPane);
        slide.setFromX(PANEL_WIDTH);
        slide.setToX(0);
        slide.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        // Fondu de l'overlay
        FadeTransition fade = new FadeTransition(Duration.millis(280), overlayPane);
        fade.setFromValue(0);
        fade.setToValue(1);

        new ParallelTransition(slide, fade).play();
        panneauOuvert = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PANNEAU LATÉRAL — FERMETURE
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void fermerPanneau() {
        if (!panneauOuvert) return;

        TranslateTransition slide = new TranslateTransition(Duration.millis(240), detailPane);
        slide.setFromX(0);
        slide.setToX(PANEL_WIDTH);
        slide.setInterpolator(javafx.animation.Interpolator.EASE_IN);

        FadeTransition fade = new FadeTransition(Duration.millis(240), overlayPane);
        fade.setFromValue(1);
        fade.setToValue(0);

        ParallelTransition pt = new ParallelTransition(slide, fade);
        pt.setOnFinished(e -> {
            detailPane.setVisible(false);
            overlayPane.setVisible(false);
        });
        pt.play();

        deselectAll();
        evenementSelectionne = null;
        panneauOuvert = false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PANNEAU LATÉRAL — REMPLISSAGE DES DONNÉES
    // ─────────────────────────────────────────────────────────────────────────

    private void remplirPanneau(Event ev) {
        int placesRestantes  = calculerPlacesRestantes(ev);
        boolean dejaInscrit  = verifierInscription(ev);

        // ── Chip type ──
        detailChipType.setText("  " + ev.getType().toUpperCase() + "  ");

        // ── Titre ──
        detailTitre.setText(ev.getTitre());

        // ── Dates ──
        detailDateDebut.setText(formatDate(ev.getDate_debut()));
        detailDateFin.setText(formatDate(ev.getDate_fin()));

        // ── Lieu ──
        detailLieu.setText(ev.getLieu());

        // ── Statut ──
        detailStatut.setText(ev.getStatut() != null ? ev.getStatut() : "—");

        // ── Places restantes + jauge ──
        if (placesRestantes == 0) {
            detailPlacesLabel.setText("Complet");
            detailPlacesLabel.setStyle("-fx-text-fill:#e53935; -fx-font-weight:900;");
        } else if (placesRestantes <= 5) {
            detailPlacesLabel.setText(placesRestantes + " place(s) — Presque complet !");
            detailPlacesLabel.setStyle("-fx-text-fill:#f57c00; -fx-font-weight:900;");
        } else {
            detailPlacesLabel.setText(placesRestantes + " / " + ev.getCapacite());
            detailPlacesLabel.setStyle("-fx-text-fill:#2e7d32; -fx-font-weight:900;");
        }

        // Jauge : largeur proportionnelle au taux de remplissage
        double tauxRemplissage = ev.getCapacite() > 0
                ? (double)(ev.getCapacite() - placesRestantes) / ev.getCapacite()
                : 1.0;
        double largeurBarre = Math.max(4, tauxRemplissage * 326); // 326 = largeur conteneur
        barreRemplissage.setPrefWidth(largeurBarre);

        // Couleur de la jauge
        String couleurBarre;
        if (tauxRemplissage >= 1.0)       couleurBarre = "#e53935"; // rouge — complet
        else if (tauxRemplissage >= 0.75) couleurBarre = "#f57c00"; // orange — presque plein
        else if (tauxRemplissage >= 0.50) couleurBarre = "#f9a825"; // jaune
        else                              couleurBarre = "#2e7d32"; // vert — beaucoup de places
        barreRemplissage.setStyle("-fx-background-color:" + couleurBarre + "; -fx-background-radius:999;");

        // ── Badge statut inscription ──
        if (dejaInscrit) {
            detailStatutInscription.setText("✅  Vous êtes déjà inscrit à cet événement !");
            detailStatutInscription.setStyle(
                    "-fx-background-color:#e8f5e9; -fx-text-fill:#2e7d32;" +
                            "-fx-font-weight:900; -fx-background-radius:8; -fx-padding:12 16 12 16;");
            
            btnSinscrire.setVisible(false);
            btnSinscrire.setManaged(false);
            
            btnAnnuler.setVisible(true);
            btnAnnuler.setManaged(true);
        } else if (placesRestantes == 0) {
            detailStatutInscription.setText("❌  Cet événement est complet.");
            detailStatutInscription.setStyle(
                    "-fx-background-color:#ffebee; -fx-text-fill:#c62828;" +
                            "-fx-font-weight:900; -fx-background-radius:8; -fx-padding:12 16 12 16;");
            btnSinscrire.setDisable(true);
            btnSinscrire.setText("Événement complet");
            btnSinscrire.setStyle(
                    "-fx-background-color:#ef9a9a; -fx-text-fill:#b71c1c;" +
                            "-fx-background-radius:7; -fx-font-weight:900;" +
                            "-fx-pref-height:46; -fx-cursor:default;");
        } else {
            detailStatutInscription.setText("🎟️  Des places sont disponibles. Inscrivez-vous !");
            detailStatutInscription.setStyle(
                    "-fx-background-color:#f3edf9; -fx-text-fill:#5b2b91;" +
                            "-fx-font-weight:900; -fx-background-radius:8; -fx-padding:12 16 12 16;");
            btnSinscrire.setDisable(false);
            btnSinscrire.setText("S'inscrire à cet événement");
            btnSinscrire.setStyle(""); // laisser la classe CSS primary-action-button agir
            btnSinscrire.getStyleClass().setAll("primary-action-button");
            btnSinscrire.setVisible(true);
            btnSinscrire.setManaged(true);
            
            btnAnnuler.setVisible(false);
            btnAnnuler.setManaged(false);
        }

        // ── Chargement Météo & Carte ──
        chargerMeteoEtCarte(ev);
    }

    private void chargerMeteoEtCarte(Event ev) {
        // Reset UI
        weatherContainer.setVisible(false);
        weatherContainer.setManaged(false);
        lblWeatherStatus.setText("Chargement de la météo...");
        mapWebView.setVisible(false);
        lblMapError.setVisible(false);
        mapProgress.setVisible(true);

        Task<double[]> geoTask = new Task<>() {
            @Override protected double[] call() {
                return GeocodingService.getCoordinates(ev.getLieu());
            }
        };

        geoTask.setOnSucceeded(e -> {
            double[] coords = geoTask.getValue();
            if (coords != null) {
                afficherCarte(coords[0], coords[1]);
                fetchWeather(coords[0], coords[1], ev.getDate_debut().toLocalDate());
            } else {
                mapProgress.setVisible(false);
                lblMapError.setVisible(true);
                lblWeatherStatus.setText("📍 Lieu introuvable pour la météo.");
            }
        });

        new Thread(geoTask).start();
    }

    private void fetchWeather(double lat, double lon, LocalDate date) {
        long daysDiff = ChronoUnit.DAYS.between(LocalDate.now(), date);
        
        if (daysDiff < 0) {
            lblWeatherStatus.setText("☁️ Événement passé");
            return;
        }
        if (daysDiff > 16) {
            lblWeatherStatus.setText("☁️ Météo disponible 16 jours avant l'événement");
            return;
        }

        Task<WeatherService.WeatherData> weatherTask = new Task<>() {
            @Override protected WeatherService.WeatherData call() {
                return WeatherService.getWeatherForecast(lat, lon, date);
            }
        };

        weatherTask.setOnSucceeded(e -> {
            WeatherService.WeatherData data = weatherTask.getValue();
            if (data != null) {
                lblWeatherEmoji.setText(data.emoji);
                lblWeatherDesc.setText(data.condition);
                lblWeatherTemp.setText(String.format("Min: %.1f°C | Max: %.1f°C", data.tempMin, data.tempMax));
                lblWeatherPrecip.setText("💧 Précipitations: " + data.precipProb + "%");
                
                weatherContainer.setVisible(true);
                weatherContainer.setManaged(true);
                lblWeatherStatus.setText("");
            } else {
                lblWeatherStatus.setText("❌ Erreur météo");
            }
        });

        new Thread(weatherTask).start();
    }

    private void afficherCarte(double lat, double lon) {
        String html = "<!DOCTYPE html><html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>#map { height: 100vh; margin:0; border-radius:10px; }</style></head><body>" +
                "<div id='map'></div><script>" +
                "var map = L.map('map').setView([" + lat + ", " + lon + "], 13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
                "var icon = L.divIcon({className: 'custom-div-icon', html: \"<div style='background-color:#5b2b91; width:12px; height:12px; border-radius:50%; border:2px solid white; box-shadow:0 0 5px rgba(0,0,0,0.5);'></div>\", iconSize: [12, 12], iconAnchor: [6, 6]});" +
                "L.marker([" + lat + ", " + lon + "], {icon: icon}).addTo(map);" +
                "</script></body></html>";
        
        Platform.runLater(() -> {
            mapWebView.getEngine().loadContent(html);
            mapWebView.setVisible(true);
            mapProgress.setVisible(false);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // S'INSCRIRE (depuis le panneau)
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    void sinscrire() {
        if (evenementSelectionne == null) return;

        try {
            int inscrits = serviceParticipation.countInscrits(evenementSelectionne.getId_event());
            if (inscrits >= evenementSelectionne.getCapacite()) {
                showAlert(Alert.AlertType.WARNING, "Complet", "Cet événement est déjà complet.");
                return;
            }

            Participation p = new Participation(evenementSelectionne.getId_event(), Home.ID_CLIENT);
            serviceParticipation.add(p);
            ToastUtil.show((Stage)btnSinscrire.getScene().getWindow(), "🎉 Inscription réussie !");
            
            chargerDonnees(); // Rafraîchir la liste d'abord
            remplirPanneau(evenementSelectionne); // Puis rafraîchir le panneau
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.WARNING, "Attention", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    @FXML
    void annulerInscription() {
        if (evenementSelectionne == null) return;

        try {
            serviceParticipation.cancelSubscription(evenementSelectionne.getId_event(), Home.ID_CLIENT);
            ToastUtil.show((Stage)btnAnnuler.getScene().getWindow(), "❌ Inscription annulée");
            
            chargerDonnees(); // Rafraîchir la liste d'abord
            remplirPanneau(evenementSelectionne); // Puis rafraîchir le panneau
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler : " + e.getMessage());
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
    // UTILITAIRES PRIVÉS
    // ─────────────────────────────────────────────────────────────────────────

    /** Retire la classe de sélection de toutes les lignes. */
    private void deselectAll() {
        vboxListeEvents.getChildren().forEach(n ->
                n.getStyleClass().remove("event-row-selected"));
    }

    /** Calcule les places restantes pour un événement (0 si erreur SQL). */
    private int calculerPlacesRestantes(Event ev) {
        try {
            int inscrits = serviceParticipation.countInscrits(ev.getId_event());
            return Math.max(0, ev.getCapacite() - inscrits);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    /** Vérifie si le client courant est déjà inscrit à l'événement. */
    private boolean verifierInscription(Event ev) {
        try {
            return serviceParticipation.estDejaInscrit(ev.getId_event(), Home.ID_CLIENT);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Crée un Label à largeur fixe (cellule de ligne). */
    private Label makeCell(String text, double width) {
        Label lbl = new Label(text != null ? text : "—");
        lbl.setPrefWidth(width);
        lbl.setMinWidth(width);
        lbl.setMaxWidth(width);
        lbl.setWrapText(false);
        lbl.setEllipsisString("…");
        return lbl;
    }

    /** Formate un LocalDateTime en "dd/MM/yyyy HH:mm". */
    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void showAlert(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}