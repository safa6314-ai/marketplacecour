package Controllers.commentaire;

import Entities.Commentaire;
import Services.CommentaireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.ResourceBundle;

public class AfficherCommentaireController implements Initializable {

    @FXML private Label lblPostInfo;
    @FXML private Label lblStatus;
    @FXML private ListView<Commentaire> listCommentaires;
    @FXML private TextArea tfNouveauCommentaire;
    @FXML private HBox adminActions;

    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final ObservableList<Commentaire> data = FXCollections.observableArrayList();

    private int postId;
    private String postContenu;
    private boolean adminMode = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listCommentaires.setItems(data);
        listCommentaires.setCellFactory(view -> new CommentaireCell());
    }

    public void setPostContext(int postId, String postContenu) {
        this.postId = postId;
        this.postContenu = postContenu;
        String preview = postContenu == null ? "" :
                (postContenu.length() > 90 ? postContenu.substring(0, 90) + "..." : postContenu);
        lblPostInfo.setText(preview);
        charger();
    }

    public void setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
        if (adminActions != null) {
            adminActions.setManaged(adminMode);
            adminActions.setVisible(adminMode);
        }
    }

    @FXML
    public void ajouterCommentaire(ActionEvent event) {
        String contenu = tfNouveauCommentaire.getText() == null ? "" : tfNouveauCommentaire.getText().trim();
        if (contenu.isEmpty() || contenu.length() > 500) {
            lblStatus.setText("Le commentaire doit contenir entre 1 et 500 caracteres.");
            return;
        }

        try {
            commentaireCRUD.ajouter(new Commentaire(contenu, new Timestamp(System.currentTimeMillis()), postId));
            tfNouveauCommentaire.clear();
            charger();
            lblStatus.setText("Commentaire ajoute.");
        } catch (SQLException e) {
            lblStatus.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void ouvrirDialogAjouter(ActionEvent event) {
        ajouterCommentaire(event);
    }

    @FXML
    public void ouvrirDialogModifier(ActionEvent event) {
        Commentaire selected = listCommentaires.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("Selectionnez un commentaire a modifier.");
            return;
        }

        modifierCommentaire(selected);
    }

    @FXML
    public void supprimerCommentaire(ActionEvent event) {
        if (!adminMode) return;
        Commentaire selected = listCommentaires.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("Selectionnez un commentaire a supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le commentaire");
        confirm.setContentText("Confirmer la suppression de ce commentaire ?");
        styliserDialog(confirm.getDialogPane());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentaireCRUD.supprimer(selected.getId());
                    charger();
                    lblStatus.setText("Commentaire supprime.");
                } catch (SQLException e) {
                    lblStatus.setText("Erreur : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void rafraichir(ActionEvent event) {
        charger();
    }

    @FXML
    public void allerVersPosts(ActionEvent event) {
        lblStatus.setText("Fermez cette fenetre pour revenir au fil.");
    }

    private void charger() {
        data.clear();
        try {
            data.addAll(commentaireCRUD.afficherParPost(postId));
            lblStatus.setText(data.size() + " commentaire(s).");
        } catch (SQLException e) {
            lblStatus.setText("Erreur : " + e.getMessage());
        }
    }

    private void modifierCommentaire(Commentaire commentaire) {
        Dialog<Commentaire> dialog = buildDialog("Modifier le commentaire", commentaire);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                commentaireCRUD.modifier(updated);
                charger();
                lblStatus.setText("Commentaire modifie.");
            } catch (SQLException e) {
                lblStatus.setText("Erreur : " + e.getMessage());
            }
        });
    }

    private void accepterCommentaire(Commentaire commentaire) {
        modifierStatut(commentaire, "accepte", "Commentaire accepte.");
    }

    private void refuserCommentaire(Commentaire commentaire) {
        modifierStatut(commentaire, "refuse", "Commentaire refuse.");
    }

    private void modifierStatut(Commentaire commentaire, String statut, String message) {
        try {
            commentaireCRUD.modifierStatut(commentaire.getId(), statut);
            commentaire.setStatut(statut);
            lblStatus.setText(message);
            listCommentaires.refresh();
        } catch (SQLException e) {
            lblStatus.setText("Erreur : " + e.getMessage());
        }
    }

    private void confirmerSupprimerCommentaire(Commentaire commentaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le commentaire");
        confirm.setContentText("Confirmer la suppression definitive de ce commentaire ?");
        styliserDialog(confirm.getDialogPane());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentaireCRUD.supprimer(commentaire.getId());
                    charger();
                    lblStatus.setText("Commentaire supprime.");
                } catch (SQLException e) {
                    lblStatus.setText("Erreur : " + e.getMessage());
                }
            }
        });
    }

    private Dialog<Commentaire> buildDialog(String title, Commentaire existing) {
        Dialog<Commentaire> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Edition du commentaire");
        styliserDialog(dialog.getDialogPane());

        TextArea contenu = new TextArea(existing.getContenu());
        contenu.setWrapText(true);
        contenu.setPrefHeight(120);
        VBox form = new VBox(8, new Label("Contenu"), contenu);
        form.setPadding(new Insets(16));
        form.setPrefWidth(440);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Enregistrer");
        ok.getStyleClass().add("primary-btn");
        Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancel.setText("Annuler");
        cancel.getStyleClass().add("secondary-btn");

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            String texte = contenu.getText() == null ? "" : contenu.getText().trim();
            if (texte.isEmpty() || texte.length() > 500) return null;
            existing.setContenu(texte);
            existing.setDateCreation(new Timestamp(System.currentTimeMillis()));
            return existing;
        });
        return dialog;
    }

    private void styliserDialog(DialogPane pane) {
        pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        pane.getStyleClass().add("dialog-theme");
    }

    private class CommentaireCell extends ListCell<Commentaire> {
        private final VBox card = new VBox(8);
        private final Label contenu = new Label();
        private final Label meta = new Label();
        private final Label statut = new Label();
        private final Button accepter = new Button("Accepter");
        private final Button refuser = new Button("Refuser");
        private final Button modifier = new Button("Modifier");
        private final Button supprimer = new Button("Supprimer");
        private final HBox actions = new HBox(8);

        CommentaireCell() {
            card.getStyleClass().add("comment-card");
            card.setPadding(new Insets(12));
            contenu.getStyleClass().add("post-content");
            contenu.setWrapText(true);
            meta.getStyleClass().add("status-label");
            statut.getStyleClass().add("status-badge");
            accepter.getStyleClass().add("primary-btn");
            refuser.getStyleClass().add("secondary-btn");
            modifier.getStyleClass().add("secondary-btn");
            supprimer.getStyleClass().add("danger-btn");
            card.getChildren().addAll(contenu, meta, statut, actions);
        }

        @Override
        protected void updateItem(Commentaire commentaire, boolean empty) {
            super.updateItem(commentaire, empty);
            if (empty || commentaire == null) {
                setGraphic(null);
                return;
            }
            contenu.setText(commentaire.getContenu());
            meta.setText(formatDate(commentaire));
            statut.setText(libelleStatut(commentaire.getStatut()));
            appliquerStyleStatut(statut, commentaire.getStatut());
            actions.getChildren().clear();
            if (adminMode) {
                accepter.setOnAction(event -> accepterCommentaire(commentaire));
                refuser.setOnAction(event -> refuserCommentaire(commentaire));
                supprimer.setOnAction(event -> confirmerSupprimerCommentaire(commentaire));
                actions.getChildren().addAll(accepter, refuser, supprimer);
            } else {
                modifier.setOnAction(event -> modifierCommentaire(commentaire));
                supprimer.setOnAction(event -> confirmerSupprimerCommentaire(commentaire));
                actions.getChildren().addAll(modifier, supprimer);
            }
            setGraphic(card);
        }
    }

    private String libelleStatut(String statut) {
        return switch (statut) {
            case "accepte" -> "Accepte";
            case "refuse" -> "Refuse";
            default -> "En attente";
        };
    }

    private void appliquerStyleStatut(Label label, String statut) {
        label.getStyleClass().removeAll("status-accepted", "status-refused", "status-pending");
        switch (statut) {
            case "accepte" -> label.getStyleClass().add("status-accepted");
            case "refuse" -> label.getStyleClass().add("status-refused");
            default -> label.getStyleClass().add("status-pending");
        }
    }

    private String formatDate(Commentaire commentaire) {
        return commentaire.getDateCreation() == null ? "" : commentaire.getDateCreation().toString().substring(0, 16);
    }
}
