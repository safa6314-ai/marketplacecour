package Controllers;

import Entities.Abonnement;
import Entities.Souscription;
import Services.AbonnementCRUD;
import Services.SouscriptionCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AdminController implements Initializable {
    private static final int ADMIN_ID = 1;
    private static final int CLIENT_ID = 2;

    @FXML private GridPane grid;
    @FXML private Button btnAbonnement;
    @FXML private Button btnSouscription;
    @FXML private Label modeTitle;
    @FXML private VBox formPanel;

    @FXML private VBox abonnementForm;
    @FXML private TextField aboNomField;
    @FXML private TextField aboPrixField;
    @FXML private TextField aboDureeField;
    @FXML private TextArea aboDescField;
    @FXML private Label aboNomError;
    @FXML private Label aboPrixError;
    @FXML private Label aboDureeError;
    @FXML private Label aboDescError;

    @FXML private VBox souscriptionForm;
    @FXML private TextField subClientField;
    @FXML private DatePicker subDateDebut;
    @FXML private DatePicker subDateFin;
    @FXML private TextField subStatutField;
    @FXML private ComboBox<Abonnement> subAboComboBox;
    @FXML private Label subClientError;
    @FXML private Label subDateDebutError;
    @FXML private Label subDateFinError;
    @FXML private Label subStatutError;
    @FXML private Label subAboIdError;

    private final AbonnementCRUD abonnementCRUD = new AbonnementCRUD();
    private final SouscriptionCRUD souscriptionCRUD = new SouscriptionCRUD();
    private boolean abonnementMode = true;
    private Integer editingAbonnementId = null;
    private Integer editingSouscriptionId = null;
    private int editingAbonnementUserId = ADMIN_ID;
    private int editingSouscriptionUserId = CLIENT_ID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        
        subAboComboBox.setConverter(new javafx.util.StringConverter<Abonnement>() {
            @Override
            public String toString(Abonnement a) {
                return a == null ? "" : a.getNom() + " - " + a.getPrix() + " DT";
            }
            @Override
            public Abonnement fromString(String string) {
                return null;
            }
        });
        
        showAbonnements(null);
    }

    @FXML
    void showAbonnements(ActionEvent event) {
        btnAbonnement.getStyleClass().setAll("admin-switch-btn-active");
        btnSouscription.getStyleClass().setAll("admin-switch-btn");
        modeTitle.setText("Gestion des abonnements");
        abonnementMode = true;
        abonnementForm.setVisible(true);
        abonnementForm.setManaged(true);
        souscriptionForm.setVisible(false);
        souscriptionForm.setManaged(false);
        clearFormFields();
        clearErrors();
        loadAbonnements();
    }

    @FXML
    void showSouscriptions(ActionEvent event) {
        btnAbonnement.getStyleClass().setAll("admin-switch-btn");
        btnSouscription.getStyleClass().setAll("admin-switch-btn-active");
        modeTitle.setText("Gestion des souscriptions");
        abonnementMode = false;
        abonnementForm.setVisible(false);
        abonnementForm.setManaged(false);
        souscriptionForm.setVisible(true);
        souscriptionForm.setManaged(true);
        clearFormFields();
        clearErrors();
        try {
            subAboComboBox.getItems().setAll(abonnementCRUD.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadSouscriptions();
    }

    private void loadAbonnements() {
        grid.getChildren().clear();
        try {
            List<Abonnement> list = abonnementCRUD.afficher();
            int column = 0;
            int row = 1;
            for (Abonnement a : list) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/GUI/AbonnementCard.fxml"));
                VBox cardBox = fxmlLoader.load();
                
                AbonnementCardController cardController = fxmlLoader.getController();
                cardController.setData(a);
                cardController.setOnEdit(this::prepareAbonnementEdit);
                cardController.setOnDelete(this::deleteAbonnement);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                grid.add(cardBox, column++, row);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSouscriptions() {
        grid.getChildren().clear();
        try {
            List<Souscription> list = souscriptionCRUD.afficher();
            int column = 0;
            int row = 1;
            for (Souscription s : list) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/GUI/SouscriptionCard.fxml"));
                VBox cardBox = fxmlLoader.load();

                SouscriptionCardController cardController = fxmlLoader.getController();
                cardController.setData(s);
                cardController.setOnEdit(this::prepareSouscriptionEdit);
                cardController.setOnDelete(this::deleteSouscription);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                grid.add(cardBox, column++, row);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAdd(ActionEvent event) {
        clearFormFields();
        clearErrors();
        formPanel.setVisible(true);
        formPanel.setManaged(true);
    }

    @FXML
    void handleSave(ActionEvent event) {
        try {
            boolean success;
            if (abonnementMode) {
                success = saveAbonnement();
                if (success) {
                    loadAbonnements();
                }
            } else {
                success = saveSouscription();
                if (success) {
                    loadSouscriptions();
                }
            }
            if (success) {
                clearFormFields();
                clearErrors();
                formPanel.setVisible(false);
                formPanel.setManaged(false);
            }
        } catch (SQLException e) {
            showError("Erreur DB", e.getMessage());
        }
    }

    @FXML
    void handleCloseForm(ActionEvent event) {
        clearFormFields();
        clearErrors();
        formPanel.setVisible(false);
        formPanel.setManaged(false);
    }

    private boolean saveAbonnement() throws SQLException {
        clearErrors();
        String nom = requireValue(aboNomField.getText(), aboNomError, "nom obligatoire");
        Double prix = parseDouble(aboPrixField.getText(), aboPrixError, "prix invalide");
        Integer duree = parseInt(aboDureeField.getText(), aboDureeError, "duree invalide");
        String desc = requireValue(aboDescField.getText(), aboDescError, "description obligatoire");
        if (nom == null || prix == null || duree == null || desc == null) return false;
        int userId = (editingAbonnementId == null) ? ADMIN_ID : editingAbonnementUserId;

        Abonnement abonnement = (editingAbonnementId == null)
                ? new Abonnement(userId, nom, prix, duree, desc)
                : new Abonnement(editingAbonnementId, userId, nom, prix, duree, desc);

        if (editingAbonnementId == null) abonnementCRUD.ajouter(abonnement);
        else abonnementCRUD.modifier(abonnement);
        return true;
    }

    private boolean saveSouscription() throws SQLException {
        clearErrors();
        String nomClient = requireValue(subClientField.getText(), subClientError, "nom client obligatoire");
        LocalDate debut = subDateDebut.getValue();
        LocalDate fin = subDateFin.getValue();
        if (debut == null) subDateDebutError.setText("date debut obligatoire");
        if (fin == null) subDateFinError.setText("date fin obligatoire");
        if (debut != null && fin != null && fin.isBefore(debut)) subDateFinError.setText("date fin doit etre >= date debut");
        String statut = requireValue(subStatutField.getText(), subStatutError, "statut obligatoire");
        Abonnement selectedAbo = subAboComboBox.getValue();
        if (selectedAbo == null) {
            subAboIdError.setText("choisir abonnement");
            return false;
        }
        int idAbo = selectedAbo.getIdAbonnement();
        if (nomClient == null || debut == null || fin == null || statut == null || !subDateFinError.getText().isEmpty()) return false;
        int userId = (editingSouscriptionId == null) ? CLIENT_ID : editingSouscriptionUserId;

        Souscription souscription = (editingSouscriptionId == null)
                ? new Souscription(userId, nomClient, Date.valueOf(debut), Date.valueOf(fin), statut, idAbo)
                : new Souscription(editingSouscriptionId, userId, nomClient, Date.valueOf(debut), Date.valueOf(fin), statut, idAbo);
        if (editingSouscriptionId == null) souscriptionCRUD.ajouter(souscription);
        else souscriptionCRUD.modifier(souscription);
        return true;
    }

    private void prepareAbonnementEdit(Abonnement a) {
        if (!confirmAction("Confirmation", "Voulez-vous modifier cet abonnement ?")) {
            return;
        }
        showAbonnements(null);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        editingAbonnementId = a.getIdAbonnement();
        editingAbonnementUserId = a.getIdUser();
        aboNomField.setText(a.getNom());
        aboPrixField.setText(String.valueOf(a.getPrix()));
        aboDureeField.setText(String.valueOf(a.getDureeMois()));
        aboDescField.setText(a.getDescription());
    }

    private void prepareSouscriptionEdit(Souscription s) {
        if (!confirmAction("Confirmation", "Voulez-vous modifier cette souscription ?")) {
            return;
        }
        showSouscriptions(null);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        editingSouscriptionId = s.getIdSouscription();
        editingSouscriptionUserId = s.getIdUser();
        subClientField.setText(s.getNomClient());
        subDateDebut.setValue(s.getDateDebut().toLocalDate());
        subDateFin.setValue(s.getDateFin().toLocalDate());
        subStatutField.setText(s.getStatut());
        for (Abonnement a : subAboComboBox.getItems()) {
            if (a.getIdAbonnement() == s.getIdAbonnement()) {
                subAboComboBox.setValue(a);
                break;
            }
        }
    }

    private void deleteAbonnement(Abonnement a) {
        if (!confirmAction("Confirmation", "Voulez-vous supprimer cet abonnement ?")) {
            return;
        }
        try {
            abonnementCRUD.supprimer(a.getIdAbonnement());
            loadAbonnements();
        } catch (SQLException e) {
            showError("Suppression impossible", e.getMessage());
        }
    }

    private void deleteSouscription(Souscription s) {
        if (!confirmAction("Confirmation", "Voulez-vous supprimer cette souscription ?")) {
            return;
        }
        try {
            souscriptionCRUD.supprimer(s.getIdSouscription());
            loadSouscriptions();
        } catch (SQLException e) {
            showError("Suppression impossible", e.getMessage());
        }
    }

    private void clearFormFields() {
        editingAbonnementId = null;
        editingSouscriptionId = null;
        editingAbonnementUserId = ADMIN_ID;
        editingSouscriptionUserId = CLIENT_ID;
        aboNomField.clear();
        aboPrixField.clear();
        aboDureeField.clear();
        aboDescField.clear();
        subClientField.clear();
        subDateDebut.setValue(null);
        subDateFin.setValue(null);
        subStatutField.clear();
        subAboComboBox.setValue(null);
    }

    private void clearErrors() {
        aboNomError.setText("");
        aboPrixError.setText("");
        aboDureeError.setText("");
        aboDescError.setText("");
        subClientError.setText("");
        subDateDebutError.setText("");
        subDateFinError.setText("");
        subStatutError.setText("");
        subAboIdError.setText("");
    }

    private String requireValue(String value, Label errorLabel, String message) {
        if (value == null || value.trim().isEmpty()) {
            errorLabel.setText(message);
            return null;
        }
        return value.trim();
    }

    private Integer parseInt(String value, Label errorLabel, String message) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            errorLabel.setText(message);
            return null;
        }
    }

    private Double parseDouble(String value, Label errorLabel, String message) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            errorLabel.setText(message);
            return null;
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmAction(String title, String message) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(null);
        confirm.setContentText(message);
        return confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    @FXML
    void switchToClientView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GUI/ClientDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Artevia - Client Dashboard");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Navigation erreur", e.getMessage());
        }
    }
}
