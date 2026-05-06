package Controllers.post;

import Controllers.commentaire.AfficherCommentaireController;
import Entities.Like;
import Entities.Post;
import Services.LikeCRUD;
import Services.PostCRUD;
import Services.SentimentService;
import Services.TraductionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class AfficherPostController implements Initializable {

    // ── FXML Fields ───────────────────────────────────────────────────────────
    @FXML private TableView<Post>            tablePost;
    @FXML private TableColumn<Post, Integer> colId;
    @FXML private TableColumn<Post, String>  colContenu;
    @FXML private TableColumn<Post, String>  colDate;
    @FXML private TableColumn<Post, Integer> colLikes;
    @FXML private TableColumn<Post, String>  colComments;
    @FXML private TableColumn<Post, String>  colSentiment;
    @FXML private Label                      lblStatus;
    @FXML private Label                      lblPage;
    @FXML private Label                      lblVedetteContenu;
    @FXML private Label                      lblVedetteLikes;
    @FXML private TextField                  tfRecherche;

    // ── Services ──────────────────────────────────────────────────────────────
    private final PostCRUD          postCRUD          = new PostCRUD();
    private final LikeCRUD          likeCRUD          = new LikeCRUD();
    private final SentimentService  sentimentService  = new SentimentService();
    private final TraductionService traductionService = new TraductionService();

    // ── State ─────────────────────────────────────────────────────────────────
    private final ObservableList<Post>  pageCourante   = FXCollections.observableArrayList();
    private final Map<Integer, Integer> likedPosts     = new HashMap<>();
    private final Map<Integer, String>  sentimentCache = new HashMap<>();

    private List<Post> tousLesPosts = new ArrayList<>();
    private List<Post> postsFiltres = new ArrayList<>();

    private static final int POSTS_PAR_PAGE = 5;
    private int pageActuelle = 0;

    // ── initialize ────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        tablePost.setFixedCellSize(40);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDateCreation().toString().substring(0, 19)));
        colComments.setCellValueFactory(c -> new SimpleStringProperty("..."));

        // ── Sentiment ────────────────────────────────────────────────────────
        colSentiment.setCellValueFactory(c -> {
            Post post = c.getValue();
            String cached = sentimentCache.get(post.getId());
            if (cached != null) return new SimpleStringProperty(cached);

            Task<String> task = new Task<>() {
                @Override protected String call() {
                    return sentimentService.analyser(post.getContenu());
                }
            };
            task.setOnSucceeded(ev -> {
                sentimentCache.put(post.getId(), task.getValue());
                tablePost.refresh();
            });
            task.setOnFailed(ev -> sentimentCache.put(post.getId(), "?"));
            new Thread(task, "sentiment-thread-" + post.getId()).start();
            return new SimpleStringProperty("...");
        });

        // ── Contenu + bouton Traduction ───────────────────────────────────────
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setCellFactory(col -> new TableCell<>() {

            private final Label  lblTexte = new Label();
            private final Button btnTrad  = new Button("Traduire");
            private final HBox   box      = new HBox(8, lblTexte, btnTrad);
            private String       originalText = "";

            {
                lblTexte.setMaxWidth(280);
                lblTexte.setWrapText(false);
                lblTexte.setStyle("-fx-text-overrun: ellipsis;");

                btnTrad.setStyle("-fx-font-size: 11; -fx-cursor: hand; -fx-padding: 1 6 1 6; -fx-background-radius: 4;");
                btnTrad.setMaxHeight(24);

                box.setAlignment(Pos.CENTER_LEFT);
                box.setMaxHeight(38);

                btnTrad.setOnAction(e -> {
                    // Si deja traduit -> retour original
                    if (btnTrad.getText().equals("Original")) {
                        lblTexte.setText(originalText);
                        btnTrad.setText("Traduire");
                        return;
                    }

                    Post post = getTableView().getItems().get(getIndex());
                    originalText = post.getContenu();

                    // Etape 1 : choix langue source
                    ChoiceDialog<String> dlgSource = new ChoiceDialog<>("fr", List.of("fr", "en", "ar"));
                    dlgSource.setTitle("Traduire le post");
                    dlgSource.setHeaderText("Langue du post (source) ?");
                    dlgSource.setContentText("Langue source :");

                    dlgSource.showAndWait().ifPresent(sourceLang -> {
                        // Etape 2 : choix langue cible
                        ChoiceDialog<String> dlgCible = new ChoiceDialog<>("en", List.of("en", "fr", "ar"));
                        dlgCible.setTitle("Traduire le post");
                        dlgCible.setHeaderText("Traduire vers quelle langue ?");
                        dlgCible.setContentText("Langue cible :");

                        dlgCible.showAndWait().ifPresent(targetLang -> {
                            lblTexte.setText("Traduction en cours...");
                            btnTrad.setDisable(true);

                            Task<String> task = new Task<>() {
                                @Override protected String call() {
                                    return traductionService.traduire(originalText, sourceLang, targetLang);
                                }
                            };
                            task.setOnSucceeded(ev -> {
                                lblTexte.setText("[Traduit] " + task.getValue());
                                btnTrad.setText("Original");
                                btnTrad.setDisable(false);
                            });
                            task.setOnFailed(ev -> {
                                lblTexte.setText(originalText);
                                btnTrad.setText("Traduire");
                                btnTrad.setDisable(false);
                                lblStatus.setText("[ERREUR] Traduction echouee.");
                            });
                            new Thread(task, "traduction-thread").start();
                        });
                    });
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                lblTexte.setText(item);
                btnTrad.setText("Traduire");
                btnTrad.setDisable(false);
                originalText = item;
                setGraphic(box);
            }
        });

        // ── Like toggle ───────────────────────────────────────────────────────
        colLikes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-cursor: hand; -fx-background-radius: 4; -fx-font-size: 12;");
                btn.setOnAction(e -> {
                    Post post = getTableView().getItems().get(getIndex());
                    toggleLike(post);
                    getTableView().refresh();
                    mettreAJourVedette();
                });
            }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); setText(null); return;
                }
                Post post = getTableView().getItems().get(getIndex());
                boolean liked = likedPosts.containsKey(post.getId());
                int count = 0;
                try { count = likeCRUD.countLikesByPost(post.getId()); } catch (SQLException ignored) {}
                btn.setText(liked ? "Like " + count : "Like " + count);
                btn.getStyleClass().setAll(liked ? "danger-btn" : "icon-btn");
                setGraphic(btn); setText(null);
            }
        });

        tablePost.setItems(pageCourante);

        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            pageActuelle = 0;
            appliquerFiltre(newVal);
        });

        chargerLikesPrecedents();
        charger();
    }

    // ── Chargement ────────────────────────────────────────────────────────────
    private void chargerLikesPrecedents() {
        try {
            for (Like l : likeCRUD.afficher()) likedPosts.put(l.getPostId(), l.getId());
        } catch (SQLException ignored) {}
    }

    private void charger() {
        try {
            tousLesPosts = postCRUD.afficher();
            postsFiltres = new ArrayList<>(tousLesPosts);
            sentimentCache.clear();
            pageActuelle = 0;
            afficherPage();
            mettreAJourVedette();
        } catch (SQLException e) {
            lblStatus.setText("[ERREUR] " + e.getMessage());
        }
    }

    // ── Post en vedette ───────────────────────────────────────────────────────
    private void mettreAJourVedette() {
        Post meilleurPost = null;
        int maxLikes = -1;

        for (Post post : tousLesPosts) {
            try {
                int nb = likeCRUD.countLikesByPost(post.getId());
                if (nb > maxLikes) { maxLikes = nb; meilleurPost = post; }
            } catch (SQLException ignored) {}
        }

        if (meilleurPost != null && maxLikes > 0) {
            String contenu = meilleurPost.getContenu();
            String preview = contenu.length() > 100 ? contenu.substring(0, 100) + "..." : contenu;
            lblVedetteContenu.setText("\"" + preview + "\"");
            lblVedetteLikes.setText(maxLikes + " like(s) - Post #" + meilleurPost.getId());
        } else {
            lblVedetteContenu.setText("Aucun post like pour l'instant - soyez le premier !");
            lblVedetteLikes.setText("");
        }
    }

    // ── Recherche & pagination ────────────────────────────────────────────────
    private void appliquerFiltre(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            postsFiltres = new ArrayList<>(tousLesPosts);
        } else {
            String mc = motCle.toLowerCase().trim();
            postsFiltres = tousLesPosts.stream()
                    .filter(p -> p.getContenu().toLowerCase().contains(mc))
                    .collect(Collectors.toList());
        }
        afficherPage();
    }

    private void afficherPage() {
        int total   = postsFiltres.size();
        int nbPages = (int) Math.ceil((double) total / POSTS_PAR_PAGE);
        if (nbPages == 0) nbPages = 1;

        int debut = pageActuelle * POSTS_PAR_PAGE;
        int fin   = Math.min(debut + POSTS_PAR_PAGE, total);

        pageCourante.setAll(postsFiltres.subList(debut, fin));
        tablePost.refresh();

        lblPage.setText("Page " + (pageActuelle + 1) + " / " + nbPages);
        lblStatus.setText("[INFO] " + total + " post(s) - affichage " +
                (total == 0 ? 0 : debut + 1) + "-" + fin);
    }

    @FXML public void pagePrecedente(ActionEvent e) {
        if (pageActuelle > 0) { pageActuelle--; afficherPage(); }
    }

    @FXML public void pageSuivante(ActionEvent e) {
        int nbPages = (int) Math.ceil((double) postsFiltres.size() / POSTS_PAR_PAGE);
        if (pageActuelle < nbPages - 1) { pageActuelle++; afficherPage(); }
    }

    // ── Like ──────────────────────────────────────────────────────────────────
    private void toggleLike(Post post) {
        try {
            if (!likedPosts.containsKey(post.getId())) {
                Like l = new Like(post.getId(), new Timestamp(System.currentTimeMillis()));
                likeCRUD.ajouter(l);
                likeCRUD.afficher().stream()
                        .filter(x -> x.getPostId() == post.getId())
                        .reduce((a, b) -> b)
                        .ifPresent(x -> likedPosts.put(post.getId(), x.getId()));
                lblStatus.setText("[INFO] Post #" + post.getId() + " like !");
            } else {
                likeCRUD.supprimer(likedPosts.get(post.getId()));
                likedPosts.remove(post.getId());
                lblStatus.setText("[INFO] Like retire.");
            }
        } catch (SQLException e) {
            lblStatus.setText("[ERREUR] " + e.getMessage());
        }
    }

    // ── Actions FXML ──────────────────────────────────────────────────────────
    @FXML public void effacerRecherche(ActionEvent e) {
        tfRecherche.clear();
        pageActuelle = 0;
        appliquerFiltre("");
    }

    @FXML public void ouvrirDialogAjouter(ActionEvent e) {
        Dialog<Post> dialog = buildPostDialog("Ajouter Post", "Nouveau post", null);
        dialog.showAndWait().ifPresent(post -> {
            try { postCRUD.ajouter(post); charger(); lblStatus.setText("[INFO] Post ajoute !"); }
            catch (SQLException ex) { lblStatus.setText("[ERREUR] " + ex.getMessage()); }
        });
    }

    @FXML public void ouvrirDialogModifier(ActionEvent e) {
        Post selected = tablePost.getSelectionModel().getSelectedItem();
        if (selected == null) { lblStatus.setText("[INFO] Selectionnez un post."); return; }
        Dialog<Post> dialog = buildPostDialog("Modifier Post", "Modifier le post", selected);
        dialog.showAndWait().ifPresent(post -> {
            try {
                postCRUD.modifier(post);
                sentimentCache.remove(post.getId());
                charger();
                lblStatus.setText("[INFO] Post modifie !");
            } catch (SQLException ex) { lblStatus.setText("[ERREUR] " + ex.getMessage()); }
        });
    }

    @FXML public void supprimerPost(ActionEvent e) {
        Post selected = tablePost.getSelectionModel().getSelectedItem();
        if (selected == null) { lblStatus.setText("[INFO] Selectionnez un post."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le post");
        confirm.setContentText("Confirmer la suppression de : " +
                selected.getContenu().substring(0, Math.min(selected.getContenu().length(), 40)) + " ?");
        confirm.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        confirm.getDialogPane().getStyleClass().add("dialog-theme");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    postCRUD.supprimer(selected.getId());
                    sentimentCache.remove(selected.getId());
                    charger();
                    lblStatus.setText("[INFO] Post supprime.");
                } catch (SQLException ex) { lblStatus.setText("[ERREUR] " + ex.getMessage()); }
            }
        });
    }

    @FXML public void ouvrirCommentaires(ActionEvent e) {
        Post selected = tablePost.getSelectionModel().getSelectedItem();
        if (selected == null) { lblStatus.setText("[INFO] Selectionnez un post."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/commentaire/AfficherCommentaire.fxml")));
            Parent root = loader.load();
            AfficherCommentaireController ctrl = loader.getController();
            ctrl.setPostContext(selected.getId(), selected.getContenu());
            tablePost.getScene().setRoot(root);
        } catch (IOException ex) { lblStatus.setText("[ERREUR] Navigation impossible."); }
    }

    @FXML public void rafraichir(ActionEvent e) {
        tfRecherche.clear();
        likedPosts.clear();
        sentimentCache.clear();
        chargerLikesPrecedents();
        charger();
    }

    // ── Dialog Ajouter/Modifier ───────────────────────────────────────────────
    private Dialog<Post> buildPostDialog(String title, String header, Post existing) {
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-theme");

        Label lblContenu = new Label("Contenu");
        lblContenu.getStyleClass().add("input-label");
        TextArea tfContenu = new TextArea();
        tfContenu.setPromptText("Exprimez-vous...");
        tfContenu.setPrefHeight(120);
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
        cancelBtn.setText("Annuler");
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
                return new Post(contenu, new Timestamp(System.currentTimeMillis()));
            }
            return null;
        });
        return dialog;
    }
}