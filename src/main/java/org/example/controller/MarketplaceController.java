package org.example.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.example.entities.Achat;
import org.example.entities.Vente;
import org.example.services.ServiceAchat;
import org.example.services.ServiceVente;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

public class MarketplaceController {

    private final ServiceVente serviceVente = new ServiceVente();
    private final ServiceAchat serviceAchat = new ServiceAchat();
    private final ObservableList<Vente> ventesData = FXCollections.observableArrayList();
    private final ObservableList<Achat> achatsData = FXCollections.observableArrayList();

    @FXML private TableView<Vente> ventesTable;
    @FXML private TableView<Achat> achatsTable;
    @FXML private Label ventesStatus;
    @FXML private Label achatsStatus;

    @FXML
    public void initialize() {
        buildVenteColumns();
        buildAchatColumns();
        ventesTable.setItems(ventesData);
        achatsTable.setItems(achatsData);
        refreshVentes();
        refreshAchats();
    }

    @FXML
    private void onAddVente() {
        openVenteDialog(null).ifPresent(vente -> {
            try {
                serviceVente.ajouter(vente);
                refreshVentes();
                ventesStatus.setText("Vente ajoutee.");
            } catch (Exception ex) {
                ventesStatus.setText("Erreur ajout vente: " + ex.getMessage());
            }
        });
    }

    @FXML
    private void onEditVente() {
        Vente selected = ventesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ventesStatus.setText("Selectionnez une vente a modifier.");
            return;
        }
        openVenteDialog(selected).ifPresent(vente -> {
            try {
                serviceVente.modifier(vente);
                refreshVentes();
                ventesStatus.setText("Vente modifiee.");
            } catch (Exception ex) {
                ventesStatus.setText("Erreur modification vente: " + ex.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteVente() {
        Vente selected = ventesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ventesStatus.setText("Selectionnez une vente a supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        styleDialog(confirm.getDialogPane());
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la vente");
        confirm.setContentText("Confirmer la suppression de: " + selected.getTitre() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceVente.supprimer(selected.getId());
                refreshVentes();
                ventesStatus.setText("Vente supprimee.");
            } catch (Exception ex) {
                ventesStatus.setText("Erreur suppression vente: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void onRefreshVentes() {
        refreshVentes();
        ventesStatus.setText("Liste des ventes actualisee.");
    }

    @FXML
    private void onClearVentesSelection() {
        ventesTable.getSelectionModel().clearSelection();
        ventesStatus.setText("Selection videe.");
    }

    @FXML
    private void onAddAchat() {
        openAchatDialog(null).ifPresent(achat -> {
            try {
                serviceAchat.ajouter(achat);
                refreshAchats();
                achatsStatus.setText("Achat ajoute.");
            } catch (Exception ex) {
                achatsStatus.setText("Erreur ajout achat: " + ex.getMessage());
            }
        });
    }

    @FXML
    private void onEditAchat() {
        Achat selected = achatsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            achatsStatus.setText("Selectionnez un achat a modifier.");
            return;
        }
        openAchatDialog(selected).ifPresent(achat -> {
            try {
                serviceAchat.modifier(achat);
                refreshAchats();
                achatsStatus.setText("Achat modifie.");
            } catch (Exception ex) {
                achatsStatus.setText("Erreur modification achat: " + ex.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteAchat() {
        Achat selected = achatsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            achatsStatus.setText("Selectionnez un achat a supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        styleDialog(confirm.getDialogPane());
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'achat");
        confirm.setContentText("Confirmer la suppression de: " + selected.getNomOeuvre() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceAchat.supprimer(selected.getId());
                refreshAchats();
                achatsStatus.setText("Achat supprime.");
            } catch (Exception ex) {
                achatsStatus.setText("Erreur suppression achat: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void onRefreshAchats() {
        refreshAchats();
        achatsStatus.setText("Liste des achats actualisee.");
    }

    @FXML
    private void onClearAchatsSelection() {
        achatsTable.getSelectionModel().clearSelection();
        achatsStatus.setText("Selection videe.");
    }

    private void buildVenteColumns() {
        if (ventesTable.getColumns().size() >= 6) {
            @SuppressWarnings("unchecked")
            TableColumn<Vente, Integer> idCol = (TableColumn<Vente, Integer>) ventesTable.getColumns().get(0);
            @SuppressWarnings("unchecked")
            TableColumn<Vente, String> titreCol = (TableColumn<Vente, String>) ventesTable.getColumns().get(1);
            @SuppressWarnings("unchecked")
            TableColumn<Vente, String> descCol = (TableColumn<Vente, String>) ventesTable.getColumns().get(2);
            @SuppressWarnings("unchecked")
            TableColumn<Vente, Double> prixCol = (TableColumn<Vente, Double>) ventesTable.getColumns().get(3);
            @SuppressWarnings("unchecked")
            TableColumn<Vente, String> catCol = (TableColumn<Vente, String>) ventesTable.getColumns().get(4);
            @SuppressWarnings("unchecked")
            TableColumn<Vente, String> artCol = (TableColumn<Vente, String>) ventesTable.getColumns().get(5);

            idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
            titreCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
            descCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
            prixCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrix()));
            catCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategorie()));
            artCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomArtiste()));
            return;
        }

        TableColumn<Vente, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        TableColumn<Vente, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        TableColumn<Vente, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        TableColumn<Vente, Double> prixCol = new TableColumn<>("Prix");
        prixCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrix()));
        TableColumn<Vente, String> catCol = new TableColumn<>("Categorie");
        catCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategorie()));
        TableColumn<Vente, String> artCol = new TableColumn<>("Artiste");
        artCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomArtiste()));
        ventesTable.getColumns().setAll(idCol, titreCol, descCol, prixCol, catCol, artCol);
    }

    private void buildAchatColumns() {
        if (achatsTable.getColumns().size() >= 5) {
            @SuppressWarnings("unchecked")
            TableColumn<Achat, Integer> idCol = (TableColumn<Achat, Integer>) achatsTable.getColumns().get(0);
            @SuppressWarnings("unchecked")
            TableColumn<Achat, String> oeuvreCol = (TableColumn<Achat, String>) achatsTable.getColumns().get(1);
            @SuppressWarnings("unchecked")
            TableColumn<Achat, String> acheteurCol = (TableColumn<Achat, String>) achatsTable.getColumns().get(2);
            @SuppressWarnings("unchecked")
            TableColumn<Achat, Double> prixCol = (TableColumn<Achat, Double>) achatsTable.getColumns().get(3);
            @SuppressWarnings("unchecked")
            TableColumn<Achat, Date> dateCol = (TableColumn<Achat, Date>) achatsTable.getColumns().get(4);

            idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
            oeuvreCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomOeuvre()));
            acheteurCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomAcheteur()));
            prixCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrix()));
            dateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDateAchat()));
            return;
        }

        TableColumn<Achat, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        TableColumn<Achat, String> oeuvreCol = new TableColumn<>("Oeuvre");
        oeuvreCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomOeuvre()));
        TableColumn<Achat, String> acheteurCol = new TableColumn<>("Acheteur");
        acheteurCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomAcheteur()));
        TableColumn<Achat, Double> prixCol = new TableColumn<>("Prix");
        prixCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrix()));
        TableColumn<Achat, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDateAchat()));
        achatsTable.getColumns().setAll(idCol, oeuvreCol, acheteurCol, prixCol, dateCol);
    }

    private void refreshVentes() {
        try {
            ventesData.setAll(serviceVente.afficherAll());
        } catch (Exception e) {
            ventesData.clear();
        }
    }

    private void refreshAchats() {
        try {
            achatsData.setAll(serviceAchat.afficher());
        } catch (Exception e) {
            achatsData.clear();
        }
    }

    private Optional<Vente> openVenteDialog(Vente existing) {
        Dialog<Vente> dialog = new Dialog<>();
        styleDialog(dialog.getDialogPane());
        dialog.setTitle(existing == null ? "Ajouter Vente" : "Modifier Vente");
        dialog.setHeaderText(existing == null ? "Nouvelle vente" : "Edition de la vente");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField titreField = new TextField();
        TextArea descriptionArea = new TextArea();
        TextField prixField = new TextField();
        TextField categorieField = new TextField();
        TextField artisteField = new TextField();
        descriptionArea.setPrefRowCount(4);

        if (existing != null) {
            titreField.setText(existing.getTitre());
            descriptionArea.setText(existing.getDescription());
            prixField.setText(String.valueOf(existing.getPrix()));
            categorieField.setText(existing.getCategorie());
            artisteField.setText(existing.getNomArtiste());
        }

        GridPane form = createFormGrid();
        addRow(form, 0, "Titre", titreField);
        addRow(form, 1, "Description", descriptionArea);
        addRow(form, 2, "Prix", prixField);
        addRow(form, 3, "Categorie", categorieField);
        addRow(form, 4, "Nom Artiste", artisteField);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                double prix = parseDouble(prixField.getText());
                if (existing == null) {
                    return new Vente(titreField.getText(), descriptionArea.getText(), prix, categorieField.getText(), artisteField.getText());
                }
                return new Vente(existing.getId(), titreField.getText(), descriptionArea.getText(), prix, categorieField.getText(), artisteField.getText());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private Optional<Achat> openAchatDialog(Achat existing) {
        Dialog<Achat> dialog = new Dialog<>();
        styleDialog(dialog.getDialogPane());
        dialog.setTitle(existing == null ? "Ajouter Achat" : "Modifier Achat");
        dialog.setHeaderText(existing == null ? "Nouvel achat" : "Edition de l'achat");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField oeuvreField = new TextField();
        TextField acheteurField = new TextField();
        TextField prixField = new TextField();
        DatePicker datePicker = new DatePicker();

        if (existing != null) {
            oeuvreField.setText(existing.getNomOeuvre());
            acheteurField.setText(existing.getNomAcheteur());
            prixField.setText(String.valueOf(existing.getPrix()));
            if (existing.getDateAchat() != null) {
                datePicker.setValue(existing.getDateAchat().toLocalDate());
            }
        }

        GridPane form = createFormGrid();
        addRow(form, 0, "Nom Oeuvre", oeuvreField);
        addRow(form, 1, "Nom Acheteur", acheteurField);
        addRow(form, 2, "Prix", prixField);
        addRow(form, 3, "Date Achat", datePicker);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                double prix = parseDouble(prixField.getText());
                Date date = parseDate(datePicker.getValue());
                if (existing == null) {
                    return new Achat(oeuvreField.getText(), acheteurField.getText(), prix, date);
                }
                return new Achat(existing.getId(), oeuvreField.getText(), acheteurField.getText(), prix, date);
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(8));
        return grid;
    }

    private void addRow(GridPane grid, int row, String label, javafx.scene.Node node) {
        Label l = new Label(label);
        l.getStyleClass().add("input-label");
        grid.add(l, 0, row);
        grid.add(node, 1, row);
        if (node instanceof TextArea) {
            ((TextArea) node).setPrefRowCount(4);
        }
        if (node instanceof TextField) {
            ((TextField) node).setPrefWidth(340);
        }
    }

    private double parseDouble(String value) {
        return Double.parseDouble(value.trim());
    }

    private Date parseDate(LocalDate localDate) {
        if (localDate == null) {
            throw new IllegalArgumentException("Date obligatoire");
        }
        return Date.valueOf(localDate);
    }

    private void styleDialog(DialogPane pane) {
        pane.getStyleClass().add("dialog-theme");
        pane.getStylesheets().add(getClass().getResource("/styles/scene-builder.css").toExternalForm());
    }
}
