package Controllers.commentaire;

import Entities.Commentaire;
import Services.CommentaireCRUD;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.ResourceBundle;

public class AfficherCommentaireController implements Initializable {

    @FXML private TableView<Commentaire>            tableCommentaire;
    @FXML private TableColumn<Commentaire, Integer> colId;
    @FXML private TableColumn<Commentaire, String>  colContenu;
    @FXML private TableColumn<Commentaire, String>  colDate;
    @FXML private TableColumn<Commentaire, Integer> colPostId;
    @FXML private Label                             lblPostInfo;
    @FXML private Label                             lblStatus;

    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final ObservableList<Commentaire> data = FXCollections.observableArrayList();

    private int    postId;
    private String postContenu;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colDate.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDateCreation().toString().substring(0, 19)));
        colPostId.setCellValueFactory(new PropertyValueFactory<>("postId"));
        tableCommentaire.setItems(data);
    }

    /** Appelé depuis AfficherPostController */
    public void setPostContext(int postId, String postContenu) {
        this.postId      = postId;
        this.postContenu = postContenu;
        if (lblPostInfo != null) {
            String preview = postContenu.length() > 70
                    ? postContenu.substring(0, 70) + "…" : postContenu;
            lblPostInfo.setText("Post #" + postId + " — " + preview);
        }
        charger();
    }

    private void charger() {
        data.clear();
        try {
            data.addAll(commentaireCRUD.afficherParPost(postId));
            if (lblStatus != null)
                lblStatus.setText("[INFO] " + data.size() + " commentaire(s) chargé(s).");
        } catch (SQLException e) {
            if (lblStatus != null) lblStatus.setText("[ERREUR] " + e.getMessage());
        }
    }

    // ── Ajouter ───────────────────────────────────────────────────────────────
    @FXML
    public void ouvrirDialogAjouter(ActionEvent e) {
        Dialog<Commentaire> dialog = buildDialog("Ajouter Commentaire", "Nouveau commentaire", null);
        dialog.showAndWait().ifPresent(c -> {
            try {
                commentaireCRUD.ajouter(c);
                charger();
                lblStatus.setText("[INFO] Commentaire ajouté !");
            } catch (SQLException ex) {
                lblStatus.setText("[ERREUR] " + ex.getMessage());
            }
        });
    }

    // ── Modifier ──────────────────────────────────────────────────────────────
    @FXML
    public void ouvrirDialogModifier(ActionEvent e) {
        Commentaire selected = tableCommentaire.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("[INFO] Sélectionnez un commentaire à modifier.");
            return;
        }
        Dialog<Commentaire> dialog = buildDialog("Modifier Commentaire", "Modifier le commentaire", selected);
        dialog.showAndWait().ifPresent(c -> {
            try {
                commentaireCRUD.modifier(c);
                charger();
                lblStatus.setText("[INFO] Commentaire modifié !");
            } catch (SQLException ex) {
                lblStatus.setText("[ERREUR] " + ex.getMessage());
            }
        });
    }

    // ── Supprimer ─────────────────────────────────────────────────────────────
    @FXML
    public void supprimerCommentaire(ActionEvent e) {
        Commentaire selected = tableCommentaire.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("[INFO] Sélectionnez un commentaire à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le commentaire");
        confirm.setContentText("Confirmer la suppression de : " +
                selected.getContenu().substring(0, Math.min(selected.getContenu().length(), 40)) + " ?");
        confirm.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        confirm.getDialogPane().getStyleClass().add("dialog-theme");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentaireCRUD.supprimer(selected.getId());
                    charger();
                    lblStatus.setText("[INFO] Commentaire supprimé.");
                } catch (SQLException ex) {
                    lblStatus.setText("[ERREUR] " + ex.getMessage());
                }
            }
        });
    }

    @FXML
    public void rafraichir(ActionEvent e) { charger(); }

    @FXML
    public void allerVersPosts(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/post/AfficherPost.fxml")));
            tableCommentaire.getScene().setRoot(root);
        } catch (IOException ex) {
            lblStatus.setText("[ERREUR] Navigation impossible.");
        }
    }

    // ── Dialog builder ────────────────────────────────────────────────────────
    private Dialog<Commentaire> buildDialog(String title, String header, Commentaire existing) {
        Dialog<Commentaire> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-theme");

        Label lblContenu = new Label("Contenu");
        lblContenu.getStyleClass().add("input-label");
        TextArea tfContenu = new TextArea();
        tfContenu.setPromptText("Votre commentaire...");
        tfContenu.setPrefHeight(100);
        tfContenu.setWrapText(true);
        if (existing != null) tfContenu.setText(existing.getContenu());

        VBox form = new VBox(8, lblContenu, tfContenu);
        form.setPadding(new Insets(16));
        form.setPrefWidth(460);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Enregistrer");
        okBtn.getStyleClass().add("primary-btn");
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setText("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String contenu = tfContenu.getText().trim();
                if (contenu.isEmpty() || contenu.length() > 500) return null;
                if (existing != null) {
                    existing.setContenu(contenu);
                    existing.setDateCreation(new Timestamp(System.currentTimeMillis()));
                    return existing;
                }
                return new Commentaire(contenu, new Timestamp(System.currentTimeMillis()), postId);
            }
            return null;
        });

        return dialog;
    }
}



