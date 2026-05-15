package controllers;

import Entites.User;
import Utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.entities.Event;
import org.example.services.ServiceEvent;
import org.example.services.WeatherService;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AjouterEvenement {
    private final ServiceEvent serviceEvent = new ServiceEvent();
    private final WeatherService weatherService = new WeatherService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Event selectedEvent;

    @FXML private VBox formBox;
    @FXML private Label lblTitle;
    @FXML private Label lblStatus;
    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private TextField tfLieu;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfHeure;
    @FXML private Spinner<Integer> spCapacite;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private TextField tfRecherche;
    @FXML private ComboBox<String> cbFiltre;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAnnuler;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitre;
    @FXML private TableColumn<Event, String> colLieu;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colCapacite;
    @FXML private TableColumn<Event, String> colCategorie;

    @FXML
    public void initialize() {
        spCapacite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000, 20));
        cbCategorie.setItems(FXCollections.observableArrayList("General", "Art", "Workshop", "Exposition", "Conference", "Networking"));
        cbCategorie.getSelectionModel().selectFirst();
        cbFiltre.setItems(FXCollections.observableArrayList("Tous", "General", "Art", "Workshop", "Exposition", "Conference", "Networking"));
        cbFiltre.getSelectionModel().selectFirst();
        tfHeure.setText("10:00");
        tfPrix.setText("0");

        colTitre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        colLieu.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLieu()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getDateEvent())));
        colCapacite.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCapacite())));
        colCategorie.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategorie()));

        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, old, event) -> fillForm(event));
        tfRecherche.textProperty().addListener((obs, old, value) -> refreshEvents());
        cbFiltre.valueProperty().addListener((obs, old, value) -> refreshEvents());

        configurePermissions();
        refreshEvents();
    }

    @FXML
    private void ajouterEvent() {
        try {
            Event event = readForm();
            serviceEvent.ajouter(event);
            showInfo("Evenement ajoute avec succes.");
            setStatus(weatherService.getWeatherSummary(event.getLieu()));
            clearForm();
            refreshEvents();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void modifierEvent() {
        if (selectedEvent == null) {
            showError("Selectionnez un evenement a modifier.");
            return;
        }
        if (!canManage(selectedEvent)) {
            showError("Vous n'avez pas le droit de modifier cet evenement.");
            return;
        }

        try {
            Event event = readForm();
            event.setId(selectedEvent.getId());
            serviceEvent.modifier(event);
            showInfo("Evenement modifie avec succes.");
            setStatus("Evenement mis a jour.");
            clearForm();
            refreshEvents();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void supprimerEvent() {
        if (selectedEvent == null) {
            showError("Selectionnez un evenement a supprimer.");
            return;
        }
        if (!canManage(selectedEvent)) {
            showError("Vous n'avez pas le droit de supprimer cet evenement.");
            return;
        }

        try {
            serviceEvent.supprimer(selectedEvent.getId());
            showInfo("Evenement supprime avec succes.");
            clearForm();
            refreshEvents();
        } catch (SQLException e) {
            showError("Suppression impossible : " + e.getMessage());
        }
    }

    @FXML
    private void annulerSelection() {
        clearForm();
    }

    @FXML
    private void afficherDetails() {
        Event event = eventTable.getSelectionModel().getSelectedItem();
        if (event == null) {
            showError("Selectionnez un evenement.");
            return;
        }
        String weather = weatherService.getWeatherSummary(event.getLieu());
        showInfo(event.getTitre() + "\n\nLieu : " + event.getLieu()
                + "\nDate : " + formatDate(event.getDateEvent())
                + "\nCapacite : " + event.getCapacite()
                + "\nCategorie : " + event.getCategorie()
                + "\nMeteo : " + weather
                + "\n\n" + nullToEmpty(event.getDescription()));
    }

    @FXML
    private void openMapForSelectedEvent() {
        Event event = eventTable.getSelectionModel().getSelectedItem();
        if (event == null) {
            showError("Veuillez sélectionner un événement.");
            return;
        }

        String address = clean(event.getLieu());
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
            eventTable.setItems(FXCollections.observableArrayList(
                    serviceEvent.rechercher(tfRecherche == null ? "" : tfRecherche.getText(), cbFiltre == null ? "Tous" : cbFiltre.getValue())
            ));
        } catch (SQLException e) {
            setStatus("Chargement Events impossible : " + e.getMessage());
        }
    }

    private Event readForm() {
        String titre = clean(tfTitre.getText());
        String lieu = clean(tfLieu.getText());
        LocalDate date = dpDate.getValue();
        LocalTime time = parseTime(tfHeure.getText());
        int capacite = spCapacite.getValue();
        double prix = parsePrice(tfPrix.getText());

        if (titre.isEmpty()) {
            throw new IllegalArgumentException("Le titre evenement est obligatoire.");
        }
        if (lieu.isEmpty()) {
            throw new IllegalArgumentException("Le lieu est obligatoire.");
        }
        if (date == null) {
            throw new IllegalArgumentException("La date est obligatoire.");
        }

        LocalDateTime eventDate = LocalDateTime.of(date, time);
        if (eventDate.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("La date doit etre valide.");
        }

        User user = SessionManager.getCurrentUser();
        int organizerId = user == null ? 0 : user.getId();
        return new Event(titre, nullToEmpty(taDescription.getText()), lieu, eventDate, capacite, prix, cbCategorie.getValue(), organizerId);
    }

    private void fillForm(Event event) {
        selectedEvent = event;
        boolean editable = event == null || canManage(event);
        btnModifier.setDisable(event == null || !editable);
        btnSupprimer.setDisable(event == null || !editable);

        if (event == null) {
            return;
        }

        tfTitre.setText(event.getTitre());
        taDescription.setText(event.getDescription());
        tfLieu.setText(event.getLieu());
        dpDate.setValue(event.getDateEvent() == null ? null : event.getDateEvent().toLocalDate());
        tfHeure.setText(event.getDateEvent() == null ? "10:00" : event.getDateEvent().toLocalTime().toString());
        spCapacite.getValueFactory().setValue(Math.max(1, event.getCapacite()));
        tfPrix.setText(String.format(Locale.US, "%.2f", event.getPrix()));
        cbCategorie.setValue(event.getCategorie() == null ? "General" : event.getCategorie());
    }

    private void clearForm() {
        selectedEvent = null;
        eventTable.getSelectionModel().clearSelection();
        tfTitre.clear();
        taDescription.clear();
        tfLieu.clear();
        dpDate.setValue(null);
        tfHeure.setText("10:00");
        spCapacite.getValueFactory().setValue(20);
        tfPrix.setText("0");
        cbCategorie.getSelectionModel().selectFirst();
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    private void configurePermissions() {
        boolean manager = isAdmin() || isOrganizer();
        formBox.setVisible(manager);
        formBox.setManaged(manager);
        btnAjouter.setVisible(manager);
        btnAjouter.setManaged(manager);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        lblTitle.setText(manager ? "Gestion des evenements" : "Evenements");
    }

    private boolean canManage(Event event) {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return false;
        }
        return isAdmin() || (isOrganizer() && event != null && event.getOrganizerId() == user.getId());
    }

    private boolean isAdmin() {
        User user = SessionManager.getCurrentUser();
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private boolean isOrganizer() {
        User user = SessionManager.getCurrentUser();
        return user != null && "ORGANIZER".equalsIgnoreCase(user.getRole());
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(clean(value).isEmpty() ? "10:00" : clean(value));
        } catch (Exception e) {
            throw new IllegalArgumentException("Heure invalide. Format attendu : HH:mm");
        }
    }

    private double parsePrice(String value) {
        try {
            return clean(value).isEmpty() ? 0 : Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Prix invalide.");
        }
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(formatter);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void setStatus(String message) {
        lblStatus.setText(message == null ? "" : message);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Events");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Events");
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Erreur inconnue." : message);
        alert.showAndWait();
    }
}
