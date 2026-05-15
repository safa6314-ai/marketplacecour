package controllers;

import Entites.User;
import Utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.entities.Event;
import org.example.entities.Participation;
import org.example.services.ServiceEvent;
import org.example.services.ServiceParticipation;
import org.example.services.WeatherService;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VoirEvenements {
    private final ServiceEvent serviceEvent = new ServiceEvent();
    private final ServiceParticipation serviceParticipation = new ServiceParticipation();
    private final WeatherService weatherService = new WeatherService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField tfRecherche;
    @FXML private ComboBox<String> cbFiltre;
    @FXML private Label lblStatus;
    @FXML private Label lblPlaces;
    @FXML private TextArea taDetails;
    @FXML private Button btnReserver;
    @FXML private Button btnAnnulerParticipation;
    @FXML private Button btnSupprimerParticipation;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitre;
    @FXML private TableColumn<Event, String> colLieu;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colPlaces;
    @FXML private TableView<Participation> participationTable;
    @FXML private TableColumn<Participation, String> colParticipant;
    @FXML private TableColumn<Participation, String> colEvent;
    @FXML private TableColumn<Participation, String> colStatut;
    @FXML private TableColumn<Participation, String> colReservationDate;

    @FXML
    public void initialize() {
        cbFiltre.setItems(FXCollections.observableArrayList("Tous", "General", "Art", "Workshop", "Exposition", "Conference", "Networking"));
        cbFiltre.getSelectionModel().selectFirst();

        colTitre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        colLieu.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLieu()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateEvent() == null ? "" : data.getValue().getDateEvent().format(formatter)));
        colPlaces.setCellValueFactory(data -> new SimpleStringProperty(availableSeats(data.getValue())));

        colParticipant.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colEvent.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEventTitle()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colReservationDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt() == null ? "" : data.getValue().getCreatedAt().format(formatter)));

        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, old, event) -> showDetails(event));
        tfRecherche.textProperty().addListener((obs, old, value) -> refreshEvents());
        cbFiltre.valueProperty().addListener((obs, old, value) -> refreshEvents());

        configurePermissions();
        refreshEvents();
        refreshParticipations();
    }

    @FXML
    private void reserver() {
        Event event = eventTable.getSelectionModel().getSelectedItem();
        if (event == null) {
            showError("Selectionnez un evenement.");
            return;
        }
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            showError("Connectez-vous pour reserver.");
            return;
        }

        try {
            serviceParticipation.reserver(event.getId(), user.getId());
            showInfo("Reservation confirmee.");
            refreshEvents();
            refreshParticipations();
            showDetails(event);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void annulerParticipation() {
        Event event = eventTable.getSelectionModel().getSelectedItem();
        User user = SessionManager.getCurrentUser();
        if (event == null || user == null) {
            showError("Selectionnez un evenement.");
            return;
        }

        try {
            serviceParticipation.annuler(event.getId(), user.getId());
            showInfo("Participation annulee.");
            refreshEvents();
            refreshParticipations();
            showDetails(event);
        } catch (SQLException e) {
            showError("Annulation impossible : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerParticipation() {
        Participation participation = participationTable.getSelectionModel().getSelectedItem();
        if (participation == null) {
            showError("Selectionnez une reservation.");
            return;
        }

        try {
            serviceParticipation.supprimer(participation.getId());
            showInfo("Reservation supprimee.");
            refreshEvents();
            refreshParticipations();
        } catch (SQLException e) {
            showError("Suppression impossible : " + e.getMessage());
        }
    }

    @FXML
    private void openMapForSelectedEvent() {
        Event event = eventTable.getSelectionModel().getSelectedItem();
        if (event == null) {
            showError("Veuillez sélectionner un événement.");
            return;
        }

        String address = event.getLieu() == null ? "" : event.getLieu().trim();
        if (address.isEmpty()) {
            showError("Adresse introuvable.");
            return;
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            URI uri = URI.create("https://www.google.com/maps/search/?api=1&query=" + encodedAddress);
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                showError("Ouverture du navigateur indisponible sur cette machine.");
                return;
            }
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            showError("Maps indisponible : " + e.getMessage());
        }
    }

    @FXML
    private void openCalendarPage() {
        try {
            EventNavigation.loadInCenter(eventTable, "/CalendrierEvenements.fxml");
        } catch (Exception e) {
            showError("Calendrier indisponible : " + e.getMessage());
        }
    }

    private void refreshEvents() {
        try {
            eventTable.setItems(FXCollections.observableArrayList(serviceEvent.rechercher(tfRecherche.getText(), cbFiltre.getValue())));
        } catch (SQLException e) {
            lblStatus.setText("Chargement des evenements impossible : " + e.getMessage());
        }
    }

    private void refreshParticipations() {
        try {
            User user = SessionManager.getCurrentUser();
            List<Participation> participations = isManager()
                    ? serviceParticipation.afficher()
                    : serviceParticipation.afficherParUtilisateur(user == null ? 0 : user.getId());
            participationTable.setItems(FXCollections.observableArrayList(participations));
        } catch (SQLException e) {
            lblStatus.setText("Chargement des reservations impossible : " + e.getMessage());
        }
    }

    private void showDetails(Event event) {
        if (event == null) {
            taDetails.clear();
            lblPlaces.setText("Places disponibles : -");
            return;
        }

        try {
            int places = serviceEvent.countAvailableSeats(event.getId());
            lblPlaces.setText("Places disponibles : " + places + " / " + event.getCapacite());
            String weather = weatherService.getWeatherSummary(event.getLieu());
            taDetails.setText("Titre : " + event.getTitre()
                    + "\nLieu : " + event.getLieu()
                    + "\nDate : " + (event.getDateEvent() == null ? "" : event.getDateEvent().format(formatter))
                    + "\nCategorie : " + event.getCategorie()
                    + "\nPrix : " + event.getPrix()
                    + "\nMeteo : " + weather
                    + "\n\n" + (event.getDescription() == null ? "" : event.getDescription()));

            User user = SessionManager.getCurrentUser();
            boolean alreadyReserved = user != null && serviceParticipation.existeParticipation(event.getId(), user.getId());
            btnReserver.setDisable(places <= 0 || alreadyReserved || isReadOnlyRole());
            btnAnnulerParticipation.setDisable(!alreadyReserved);
            lblStatus.setText(alreadyReserved ? "Vous participez deja a cet evenement." : "");
        } catch (SQLException e) {
            lblStatus.setText("Details indisponibles : " + e.getMessage());
        }
    }

    private String availableSeats(Event event) {
        try {
            return String.valueOf(serviceEvent.countAvailableSeats(event.getId()));
        } catch (SQLException e) {
            return "-";
        }
    }

    private void configurePermissions() {
        btnSupprimerParticipation.setVisible(isManager());
        btnSupprimerParticipation.setManaged(isManager());
    }

    private boolean isManager() {
        User user = SessionManager.getCurrentUser();
        if (user == null || user.getRole() == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(user.getRole()) || "ORGANIZER".equalsIgnoreCase(user.getRole());
    }

    private boolean isReadOnlyRole() {
        User user = SessionManager.getCurrentUser();
        if (user == null || user.getRole() == null) {
            return true;
        }
        String role = user.getRole();
        return !("ADMIN".equalsIgnoreCase(role) || "ORGANIZER".equalsIgnoreCase(role)
                || "BUYER".equalsIgnoreCase(role) || "STUDENT".equalsIgnoreCase(role));
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservations");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Reservations");
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Erreur inconnue." : message);
        alert.showAndWait();
    }
}
