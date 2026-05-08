package controllers;

import org.example.entities.Event;
import org.example.services.ServiceEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.ResourceBundle;

public class AfficherEvenement implements Initializable {

    // ── TableView ──
    @FXML private TableView<Event>                  tableEvent;
    @FXML private TableColumn<Event, Integer>       colId;
    @FXML private TableColumn<Event, String>        colTitre;
    @FXML private TableColumn<Event, LocalDateTime> colDateDebut;
    @FXML private TableColumn<Event, LocalDateTime> colDateFin;
    @FXML private TableColumn<Event, String>        colLieu;
    @FXML private TableColumn<Event, Integer>       colCapacite;
    @FXML private TableColumn<Event, String>        colType;
    @FXML private TableColumn<Event, String>        colStatut;

    // ── Filtres et tri ──
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbFiltreType;
    @FXML private ComboBox<String> cbTri;
    @FXML private Label            lblCompteur;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private ObservableList<Event> tousLesEvents = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Lier colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id_event"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configurer les colonnes
        setupTableColumns();

        // Remplir ComboBox filtre type
        cbFiltreType.getItems().addAll("Tous", "exposition", "vente", "conférence", "atelier", "concert");
        cbFiltreType.setValue("Tous");

        // Remplir ComboBox tri
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

    /**
     * Configure le style et le formatage des colonnes du tableau
     */
    private void setupTableColumns() {
        // Centrer les colonnes ID et Capacité
        colId.setCellFactory(col -> new TableCell<Event, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        colCapacite.setCellFactory(col -> new TableCell<Event, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Formater les dates dans la table
        colDateDebut.setCellFactory(column -> new TableCell<Event, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

        colDateFin.setCellFactory(column -> new TableCell<Event, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

        // Style pour le statut avec des badges colorés (version ARTEVIA Purple)
        colStatut.setCellFactory(column -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(javafx.geometry.Pos.CENTER);

                    if ("actif".equals(item)) {
                        setStyle("-fx-background-color: #e9fbf2; -fx-text-fill: #28c76f; " +
                                "-fx-padding: 4 12; -fx-background-radius: 20; " +
                                "-fx-font-size: 11px; -fx-font-weight: 900;");
                    } else if ("planifié".equals(item)) {
                        setStyle("-fx-background-color: #ECBBFA; -fx-text-fill: #5b2b91; " +
                                "-fx-padding: 4 12; -fx-background-radius: 20; " +
                                "-fx-font-size: 11px; -fx-font-weight: 900;");
                    } else if ("terminé".equals(item)) {
                        setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #757575; " +
                                "-fx-padding: 4 12; -fx-background-radius: 20; " +
                                "-fx-font-size: 11px; -fx-font-weight: 900;");
                    } else if ("annulé".equals(item)) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #f44336; " +
                                "-fx-padding: 4 12; -fx-background-radius: 20; " +
                                "-fx-font-size: 11px; -fx-font-weight: 900;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Style pour le type avec badges
        colType.setCellFactory(column -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #f3edf9; -fx-text-fill: #5b2b91; " +
                            "-fx-padding: 4 10; -fx-background-radius: 15; " +
                            "-fx-font-size: 11px; -fx-font-weight: 600;");
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
    }

    // ── Charger tous les événements ──
    @FXML
    public void chargerDonnees() {
        try {
            tousLesEvents = FXCollections.observableArrayList(serviceEvent.getAll());
            tableEvent.setItems(tousLesEvents);
            tfRecherche.clear();
            cbFiltreType.setValue("Tous");
            majCompteur(tousLesEvents.size());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger : " + e.getMessage());
        }
    }

    // ── Recherche par titre (en temps réel) ──
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
        tableEvent.setItems(resultats);
        majCompteur(resultats.size());
    }

    // ── Filtre par type ──
    @FXML
    void filtrerParType() {
        appliquerFiltre();
    }

    private void appliquerFiltre() {
        String type = cbFiltreType.getValue();
        String motCle = tfRecherche.getText().trim().toLowerCase();

        ObservableList<Event> filtres = FXCollections.observableArrayList();
        for (Event e : tousLesEvents) {
            boolean matchType  = type == null || type.equals("Tous") || e.getType().equals(type);
            boolean matchTitre = motCle.isEmpty() ||
                    e.getTitre().toLowerCase().contains(motCle) ||
                    e.getLieu().toLowerCase().contains(motCle);
            if (matchType && matchTitre) filtres.add(e);
        }
        tableEvent.setItems(filtres);
        majCompteur(filtres.size());
    }

    // ── Tri ──
    @FXML
    void trierTable() {
        ObservableList<Event> items = tableEvent.getItems();
        String tri = cbTri.getValue();
        if (tri == null) return;

        switch (tri) {
            case "Date début (croissant)":
                items.sort(Comparator.comparing(Event::getDate_debut));
                break;
            case "Date début (décroissant)":
                items.sort(Comparator.comparing(Event::getDate_debut).reversed());
                break;
            case "Titre (A-Z)":
                items.sort(Comparator.comparing(e -> e.getTitre().toLowerCase()));
                break;
            case "Titre (Z-A)":
                items.sort((a, b) -> b.getTitre().compareToIgnoreCase(a.getTitre()));
                break;
            case "Capacité (croissant)":
                items.sort(Comparator.comparingInt(Event::getCapacite));
                break;
            case "Capacité (décroissant)":
                items.sort(Comparator.comparingInt(Event::getCapacite).reversed());
                break;
        }
        tableEvent.setItems(items);
    }

    // ── Modifier ──
    @FXML
    void ouvrirModifier() {
        Event selected = tableEvent.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un événement à modifier !");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvenement.fxml"));
            Parent root = loader.load();
            AjouterEvenement controller = loader.getController();
            controller.setEventAModifier(selected);
            Stage stage = new Stage();
            stage.setTitle("Modifier : " + selected.getTitre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Appliquer le CSS à la nouvelle fenêtre
            String cssUrl = getClass().getResource("/styles.css").toExternalForm();
            if (cssUrl != null) {
                stage.getScene().getStylesheets().add(cssUrl);
            }

            stage.showAndWait();
            chargerDonnees();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ── Supprimer ──
    @FXML
    void supprimerEvent() {
        Event selected = tableEvent.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un événement à supprimer !");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'événement : \"" + selected.getTitre() + "\" ?");

        // Style du dialog
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceEvent.delete(selected);
                    tousLesEvents.remove(selected);
                    chargerDonnees();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement supprimé !");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    // ── Retour à l'ajout ──
    @FXML
    void retourAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvenement.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un événement");
            stage.setScene(new Scene(root));

            // Appliquer le CSS à la nouvelle fenêtre
            String cssUrl = getClass().getResource("/styles.css").toExternalForm();
            if (cssUrl != null) {
                stage.getScene().getStylesheets().add(cssUrl);
            }

            stage.show();
            ((Stage) tableEvent.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void majCompteur(int nb) {
        lblCompteur.setText("Total : " + nb + " événement(s)");
    }

    private void showAlert(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Appliquer le style CSS à l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        String cssUrl = getClass().getResource("/styles.css").toExternalForm();
        if (cssUrl != null) {
            dialogPane.getStylesheets().add(cssUrl);
        }
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }
}