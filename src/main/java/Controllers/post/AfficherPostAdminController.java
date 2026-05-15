package Controllers.post;

import Controllers.commentaire.AfficherCommentaireController;
import Entities.Like;
import Entities.Post;
import Services.CommentaireCRUD;
import Services.LikeCRUD;
import Services.ModerationService;
import Services.PostCRUD;
import Services.SentimentService;
import Utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class AfficherPostAdminController implements Initializable {

    @FXML private ListView<Post> listPosts;
    @FXML private TextField tfRecherche;
    @FXML private Label lblPage;
    @FXML private Label lblStatus;
    @FXML private Label lblVedetteContenu;
    @FXML private Label lblVedetteLikes;
    @FXML private Label lblDashboardPosts;
    @FXML private Label lblDashboardLikes;
    @FXML private Label lblDashboardCommentaires;
    @FXML private Label lblMostActivePost;
    @FXML private Label lblDominantSentiment;
    @FXML private ComboBox<String> cbCategorieFiltre;

    private final PostCRUD postCRUD = new PostCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final LikeCRUD likeCRUD = new LikeCRUD();
    private final SentimentService sentimentService = new SentimentService();
    private final ModerationService moderationService = new ModerationService();

    private final ObservableList<Post> pageCourante = FXCollections.observableArrayList();
    private final Map<Integer, String> sentimentCache = new HashMap<>();

    private List<Post> tousLesPosts = new ArrayList<>();
    private List<Post> postsFiltres = new ArrayList<>();

    private static final int POSTS_PAR_PAGE = 5;
    private int pageActuelle = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!isAdmin()) {
            Platform.runLater(() -> lblStatus.setText("Acces reserve aux administrateurs."));
            return;
        }
        listPosts.setItems(pageCourante);
        listPosts.setCellFactory(view -> new PostAdminCell());
        cbCategorieFiltre.setItems(FXCollections.observableArrayList(categoriesFiltre()));
        cbCategorieFiltre.getSelectionModel().select("Toutes");
        tfRecherche.textProperty().addListener((obs, oldValue, newValue) -> {
            pageActuelle = 0;
            appliquerFiltres();
        });
        cbCategorieFiltre.valueProperty().addListener((obs, oldValue, newValue) -> {
            pageActuelle = 0;
            appliquerFiltres();
        });
        charger();
    }

    @FXML
    public void ouvrirDialogAjouter(ActionEvent event) {
        Dialog<Post> dialog = buildPostDialog("Ajouter un post", "Nouveau post", null);
        dialog.showAndWait().ifPresent(post -> {
            try {
                postCRUD.ajouter(post);
                lblStatus.setText("Post ajoute avec succes.");
                charger();
            } catch (SQLException e) {
                afficherErreur(e);
            }
        });
    }

    @FXML
    public void ouvrirDialogModifier(ActionEvent event) {
        Post selected = listPosts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("Selectionnez un post a modifier.");
            return;
        }

        Dialog<Post> dialog = buildPostDialog("Modifier le post", "Edition du post", selected);
        dialog.showAndWait().ifPresent(post -> {
            try {
                postCRUD.modifier(post);
                sentimentCache.remove(post.getId());
                lblStatus.setText("Post modifie.");
                charger();
            } catch (SQLException e) {
                afficherErreur(e);
            }
        });
    }

    @FXML
    public void supprimerPost(ActionEvent event) {
        Post selected = listPosts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("Selectionnez un post a supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le post");
        confirm.setContentText("Confirmer la suppression de ce post ?");
        styliserDialog(confirm.getDialogPane());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    postCRUD.supprimer(selected.getId());
                    sentimentCache.remove(selected.getId());
                    lblStatus.setText("Post supprime.");
                    charger();
                } catch (SQLException e) {
                    afficherErreur(e);
                }
            }
        });
    }

    @FXML
    public void ouvrirCommentaires(ActionEvent event) {
        Post selected = listPosts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("Selectionnez un post pour afficher ses commentaires.");
            return;
        }
        ouvrirCommentairesDialog(selected, true);
    }

    @FXML
    public void afficherPosts(ActionEvent event) {
        charger();
        listPosts.getSelectionModel().clearSelection();
        lblStatus.setText("Liste des posts actualisee.");
    }

    @FXML
    public void ouvrirCommentairesSidebar(ActionEvent event) {
        Post selected = listPosts.getSelectionModel().getSelectedItem();
        if (selected == null && !pageCourante.isEmpty()) {
            selected = pageCourante.get(0);
            listPosts.getSelectionModel().select(selected);
        }
        if (selected == null) {
            lblStatus.setText("Aucun post disponible pour afficher les commentaires.");
            return;
        }
        ouvrirCommentairesDialog(selected, true);
    }

    @FXML
    public void ouvrirLikes(ActionEvent event) {
        try {
            List<Post> posts = postCRUD.afficher();
            int totalLikes = likeCRUD.afficher().size();
            String details = posts.stream()
                    .sorted(Comparator.comparingInt(this::compterLikes).reversed())
                    .map(post -> compterLikes(post) + " like(s) - " + preview(post.getContenu(), 80))
                    .collect(Collectors.joining("\n"));

            Label title = new Label("Likes des publications");
            title.getStyleClass().add("section-title");

            Label total = new Label(totalLikes + " like(s) au total");
            total.getStyleClass().add("status-label");

            TextArea content = new TextArea(details.isBlank() ? "Aucun post trouve." : details);
            content.setEditable(false);
            content.setWrapText(true);
            content.setPrefWidth(520);
            content.setPrefHeight(260);

            VBox box = new VBox(10, title, total, content);
            box.setPadding(new Insets(12));

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Likes");
            styliserDialog(dialog.getDialogPane());
            dialog.getDialogPane().setContent(box);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            ((Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE)).setText("Fermer");
            dialog.showAndWait();
            lblStatus.setText("Resume des likes affiche.");
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    @FXML
    public void rafraichir(ActionEvent event) {
        sentimentCache.clear();
        charger();
    }

    @FXML
    public void autoModerer(ActionEvent event) {
        lblStatus.setText("Moderation automatique en cours...");
        Task<int[]> task = new Task<>() {
            @Override
            protected int[] call() throws Exception {
                int postsTraites = 0;
                int commentairesTraites = 0;

                for (Post post : postCRUD.afficher()) {
                    if (!"en_attente".equals(post.getStatut())) continue;
                    ModerationService.Result result = moderationService.analyser(post.getContenu());
                    String categorie = moderationService.categoriePour(post.getContenu(), post.getCategorie());
                    postCRUD.modifierModeration(post.getId(), result.statut(), result.score(), result.message(), categorie);
                    postsTraites++;
                }

                for (Entities.Commentaire commentaire : commentaireCRUD.afficher()) {
                    if (!"en_attente".equals(commentaire.getStatut())) continue;
                    ModerationService.Result result = moderationService.analyser(commentaire.getContenu());
                    commentaireCRUD.modifierModeration(commentaire.getId(), result.statut(), result.score(), result.message());
                    commentairesTraites++;
                }
                return new int[]{postsTraites, commentairesTraites};
            }
        };
        task.setOnSucceeded(done -> {
            int[] counts = task.getValue();
            lblStatus.setText("Auto-moderation terminee: " + counts[0] + " post(s), " + counts[1] + " commentaire(s).");
            charger();
        });
        task.setOnFailed(done -> lblStatus.setText("Auto-moderation echouee: " + task.getException().getMessage()));
        Thread thread = new Thread(task, "auto-moderation-admin");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void effacerRecherche(ActionEvent event) {
        tfRecherche.clear();
        cbCategorieFiltre.getSelectionModel().select("Toutes");
        appliquerFiltres();
    }

    @FXML
    public void pagePrecedente(ActionEvent event) {
        if (pageActuelle > 0) {
            pageActuelle--;
            afficherPage();
        }
    }

    @FXML
    public void pageSuivante(ActionEvent event) {
        int nbPages = (int) Math.ceil((double) postsFiltres.size() / POSTS_PAR_PAGE);
        if (pageActuelle < nbPages - 1) {
            pageActuelle++;
            afficherPage();
        }
    }

    private void charger() {
        try {
            tousLesPosts = postCRUD.afficher();
            postsFiltres = new ArrayList<>(tousLesPosts);
            pageActuelle = 0;
            appliquerFiltres();
            mettreAJourVedette();
            mettreAJourDashboard();
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    private void appliquerFiltres() {
        String motCle = tfRecherche == null ? "" : tfRecherche.getText();
        String categorie = cbCategorieFiltre == null ? "Toutes" : cbCategorieFiltre.getValue();
        if (motCle == null || motCle.trim().isEmpty()) {
            postsFiltres = new ArrayList<>(tousLesPosts);
        } else {
            String filtre = motCle.toLowerCase().trim();
            postsFiltres = tousLesPosts.stream()
                    .filter(post -> post.getContenu().toLowerCase().contains(filtre))
                    .collect(Collectors.toList());
        }
        if (categorie != null && !"Toutes".equals(categorie)) {
            postsFiltres = postsFiltres.stream()
                    .filter(post -> categorie.equals(post.getCategorie()))
                    .collect(Collectors.toList());
        }
        afficherPage();
    }

    private void afficherPage() {
        int total = postsFiltres.size();
        int nbPages = Math.max(1, (int) Math.ceil((double) total / POSTS_PAR_PAGE));
        if (pageActuelle >= nbPages) pageActuelle = nbPages - 1;

        int debut = pageActuelle * POSTS_PAR_PAGE;
        int fin = Math.min(debut + POSTS_PAR_PAGE, total);
        pageCourante.setAll(total == 0 ? List.of() : postsFiltres.subList(debut, fin));
        listPosts.refresh();

        lblPage.setText("Page " + (pageActuelle + 1) + " / " + nbPages);
        lblStatus.setText(total + " post(s) affiche(s).");
    }

    private void mettreAJourVedette() {
        Post vedette = null;
        int maxLikes = -1;

        for (Post post : tousLesPosts) {
            int likes = compterLikes(post);
            if (likes > maxLikes) {
                maxLikes = likes;
                vedette = post;
            }
        }

        if (vedette == null || maxLikes <= 0) {
            lblVedetteContenu.setText("Aucun post en vedette pour le moment.");
            lblVedetteLikes.setText("0 like");
            return;
        }

        lblVedetteContenu.setText(preview(vedette.getContenu(), 150));
        lblVedetteLikes.setText(maxLikes + " like(s)");
    }

    private void mettreAJourDashboard() {
        int totalLikes = 0;
        int totalCommentaires = 0;
        Post mostActive = null;
        int bestActivity = -1;
        Map<String, Integer> sentiments = new HashMap<>();

        for (Post post : tousLesPosts) {
            int likes = compterLikes(post);
            int commentaires = compterCommentaires(post);
            totalLikes += likes;
            totalCommentaires += commentaires;
            int activity = likes + commentaires;
            if (activity > bestActivity) {
                bestActivity = activity;
                mostActive = post;
            }
            String sentiment = sentimentCache.get(post.getId());
            if (sentiment != null) sentiments.merge(sentiment, 1, Integer::sum);
        }

        lblDashboardPosts.setText(String.valueOf(tousLesPosts.size()));
        lblDashboardLikes.setText(String.valueOf(totalLikes));
        lblDashboardCommentaires.setText(String.valueOf(totalCommentaires));
        lblMostActivePost.setText(mostActive == null ? "Aucun" : "Publication active (" + bestActivity + ")");
        lblDominantSentiment.setText(sentiments.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Analyse en cours"));
    }

    private int compterLikes(Post post) {
        try {
            return likeCRUD.countLikesByPost(post.getId());
        } catch (SQLException e) {
            return 0;
        }
    }

    private int compterCommentaires(Post post) {
        try {
            return commentaireCRUD.afficherParPost(post.getId()).size();
        } catch (SQLException e) {
            return 0;
        }
    }

    private void accepterPost(Post post) {
        modifierStatut(post, "accepte", "Publication acceptee.");
    }

    private void refuserPost(Post post) {
        modifierStatut(post, "refuse", "Publication refusee.");
    }

    private void modifierStatut(Post post, String statut, String message) {
        try {
            postCRUD.modifierStatut(post.getId(), statut);
            post.setStatut(statut);
            lblStatus.setText(message);
            listPosts.refresh();
            mettreAJourDashboard();
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    private void confirmerSupprimerPost(Post post) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la publication");
        confirm.setContentText("Confirmer la suppression definitive de cette publication ?");
        styliserDialog(confirm.getDialogPane());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    postCRUD.supprimer(post.getId());
                    sentimentCache.remove(post.getId());
                    lblStatus.setText("Publication supprimee.");
                    charger();
                } catch (SQLException e) {
                    afficherErreur(e);
                }
            }
        });
    }

    private String sentimentPour(Post post) {
        String cached = sentimentCache.get(post.getId());
        if (cached != null) return cached;

        sentimentCache.put(post.getId(), "...");
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return sentimentService.analyser(post.getContenu());
            }
        };
        task.setOnSucceeded(event -> {
            sentimentCache.put(post.getId(), task.getValue());
            listPosts.refresh();
            mettreAJourDashboard();
        });
        task.setOnFailed(event -> {
            sentimentCache.put(post.getId(), "?");
            listPosts.refresh();
        });
        Thread thread = new Thread(task, "sentiment-admin-" + post.getId());
        thread.setDaemon(true);
        thread.start();
        return "...";
    }

    private Dialog<Post> buildPostDialog(String title, String header, Post existing) {
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        styliserDialog(dialog.getDialogPane());

        Label label = new Label("Contenu");
        label.getStyleClass().add("input-label");
        TextArea contenu = new TextArea();
        contenu.setPromptText("Ecrivez votre publication...");
        contenu.setWrapText(true);
        contenu.setPrefHeight(150);
        if (existing != null) contenu.setText(existing.getContenu());

        Label categorieLabel = new Label("Categorie");
        categorieLabel.getStyleClass().add("input-label");
        ComboBox<String> categorie = new ComboBox<>(FXCollections.observableArrayList(moderationService.categories()));
        categorie.setMaxWidth(Double.MAX_VALUE);
        categorie.getSelectionModel().select(existing == null ? "Discussion generale" : existing.getCategorie());

        VBox form = new VBox(10, label, contenu, categorieLabel, categorie);
        form.setPadding(new Insets(16));
        form.setPrefWidth(480);
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
            if (existing != null) {
                existing.setContenu(texte);
                existing.setDateCreation(new Timestamp(System.currentTimeMillis()));
                existing.setCategorie(categorie.getValue());
                existing.setUserId(currentUserId());
                return existing;
            }
            Post post = new Post(texte, new Timestamp(System.currentTimeMillis()));
            post.setCategorie(categorie.getValue());
            post.setUserId(currentUserId());
            return post;
        });
        return dialog;
    }

    private boolean isAdmin() {
        return SessionManager.getCurrentUser() != null
                && "ADMIN".equalsIgnoreCase(SessionManager.getCurrentUser().getRole());
    }

    private int currentUserId() {
        return SessionManager.getCurrentUser() == null ? 0 : SessionManager.getCurrentUser().getId();
    }

    private List<String> categoriesFiltre() {
        List<String> categories = new ArrayList<>();
        categories.add("Toutes");
        categories.addAll(moderationService.categories());
        return categories;
    }

    private void ouvrirCommentairesDialog(Post post, boolean adminMode) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/commentaire/AfficherCommentaire.fxml")));
            Node content = loader.load();
            AfficherCommentaireController controller = loader.getController();
            controller.setPostContext(post.getId(), post.getContenu());
            controller.setAdminMode(adminMode);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Commentaires");
            styliserDialog(dialog.getDialogPane());
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            ((Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE)).setText("Fermer");
            dialog.showAndWait();
            listPosts.refresh();
            mettreAJourDashboard();
        } catch (IOException e) {
            lblStatus.setText("Impossible d'ouvrir les commentaires.");
        }
    }

    private void styliserDialog(DialogPane pane) {
        pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/forum-style.css")).toExternalForm());
        pane.getStyleClass().add("dialog-theme");
    }

    private void afficherErreur(Exception e) {
        lblStatus.setText("Erreur : " + e.getMessage());
    }

    private String preview(String contenu, int max) {
        if (contenu == null) return "";
        return contenu.length() > max ? contenu.substring(0, max) + "..." : contenu;
    }

    private class PostAdminCell extends ListCell<Post> {
        private final VBox card = new VBox(10);
        private final Label contenu = new Label();
        private final Label meta = new Label();
        private final Label stats = new Label();
        private final Label sentiment = new Label();
        private final Label statut = new Label();
        private final Label categorie = new Label();
        private final Label moderation = new Label();
        private final ImageView thumbnail = new ImageView();
        private final VBox textBox = new VBox(10);
        private final HBox body = new HBox(12);
        private final Button accepter = new Button("Accepter");
        private final Button refuser = new Button("Refuser");
        private final Button supprimer = new Button("Supprimer");

        PostAdminCell() {
            card.getStyleClass().addAll("post-card", "admin-post-card");
            card.setPadding(new Insets(18));
            body.setAlignment(Pos.CENTER_LEFT);
            contenu.getStyleClass().add("post-content");
            contenu.setWrapText(true);
            meta.getStyleClass().add("status-label");
            stats.getStyleClass().add("status-label");
            sentiment.getStyleClass().add("sentiment-badge");
            statut.getStyleClass().add("status-badge");
            categorie.getStyleClass().add("sentiment-badge");
            moderation.getStyleClass().add("status-label");
            accepter.getStyleClass().add("primary-btn");
            refuser.getStyleClass().add("secondary-btn");
            supprimer.getStyleClass().add("danger-btn");

            HBox bottom = new HBox(14, meta, stats, sentiment);
            bottom.setAlignment(Pos.CENTER_LEFT);
            HBox actions = new HBox(8, statut, categorie, accepter, refuser, supprimer);
            actions.setAlignment(Pos.CENTER_LEFT);
            thumbnail.setFitWidth(100);
            thumbnail.setFitHeight(76);
            thumbnail.setPreserveRatio(false);
            thumbnail.setSmooth(true);
            textBox.getChildren().addAll(contenu, bottom, moderation, actions);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            body.getChildren().add(textBox);
            card.getChildren().add(body);
        }

        @Override
        protected void updateItem(Post post, boolean empty) {
            super.updateItem(post, empty);
            if (empty || post == null) {
                setGraphic(null);
                return;
            }
            contenu.setText(preview(post.getContenu(), 180));
            meta.setText(formatDate(post));
            stats.setText(compterLikes(post) + " like(s) - " + compterCommentaires(post) + " commentaire(s)");
            sentiment.setText(sentimentPour(post));
            statut.setText(libelleStatut(post.getStatut()));
            categorie.setText(post.getCategorie());
            moderation.setText(formatModeration(post.getModerationMessage(), post.getModerationScore()));
            accepter.setOnAction(event -> accepterPost(post));
            refuser.setOnAction(event -> refuserPost(post));
            supprimer.setOnAction(event -> confirmerSupprimerPost(post));
            ajouterThumbnailSiDisponible(post);
            setGraphic(card);
        }

        private void ajouterThumbnailSiDisponible(Post post) {
            body.getChildren().remove(thumbnail);
            thumbnail.setImage(null);
            thumbnail.setOnMouseClicked(null);
            if (post.getImagePath() == null || post.getImagePath().isBlank()) return;

            chargerImageAsync(post.getImagePath(), thumbnail, () -> {
                if (!body.getChildren().contains(thumbnail) && thumbnail.getImage() != null) {
                    body.getChildren().add(thumbnail);
                }
            });
            thumbnail.setOnMouseClicked(event -> ouvrirImageDialog(post.getImagePath()));
        }
    }

    private String libelleStatut(String statut) {
        return switch (statut) {
            case "accepte" -> "Accepte";
            case "refuse" -> "Refuse";
            default -> "En attente";
        };
    }

    private String formatModeration(String message, Double score) {
        String scoreText = score == null ? "" : " (score " + String.format(Locale.US, "%.2f", score) + ")";
        return message + scoreText;
    }

    private String formatDate(Post post) {
        return post.getDateCreation() == null ? "" : post.getDateCreation().toString().substring(0, 16);
    }

    private void ouvrirImageDialog(String imagePath) {
        ImageView fullImage = new ImageView();
        fullImage.setFitWidth(760);
        fullImage.setFitHeight(520);
        fullImage.setPreserveRatio(true);
        fullImage.setSmooth(true);
        chargerImageAsync(imagePath, fullImage, null);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Image de la publication");
        styliserDialog(dialog.getDialogPane());
        dialog.getDialogPane().setContent(fullImage);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE)).setText("Fermer");
        dialog.showAndWait();
    }

    private void chargerImageAsync(String imagePath, ImageView imageView, Runnable onSuccess) {
        new Thread(() -> {
            try {
                String source = resolveImageSource(imagePath);
                if (source == null) return;
                Image image = new Image(source, false);
                if (image.isError()) return;
                Platform.runLater(() -> {
                    imageView.setImage(image);
                    if (onSuccess != null) onSuccess.run();
                });
            } catch (Exception ignored) {
            }
        }, "image-load-admin").start();
    }

    private String resolveImageSource(String imagePath) {
        try {
            if (imagePath == null || imagePath.isBlank()) return null;
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                return imagePath;
            }
            String resourcePath = toResourcePath(imagePath);
            URL resource = getClass().getResource(resourcePath);
            if (resource != null) {
                return resource.toExternalForm();
            }

            Path sourceResource = Path.of("src", "main", "resources", resourcePath.substring(1));
            if (Files.exists(sourceResource)) {
                return sourceResource.toUri().toString();
            }

            Path runtimeResource = Path.of("target", "classes", resourcePath.substring(1));
            return Files.exists(runtimeResource) ? runtimeResource.toUri().toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String toResourcePath(String imagePath) {
        String normalized = imagePath.replace("\\", "/").trim();
        String resourcesPrefix = "src/main/resources/";
        int resourcesIndex = normalized.indexOf(resourcesPrefix);
        if (resourcesIndex >= 0) {
            normalized = normalized.substring(resourcesIndex + resourcesPrefix.length());
        }

        int imagesIndex = normalized.indexOf("/images/");
        if (resourcesIndex < 0 && imagesIndex >= 0) {
            normalized = normalized.substring(imagesIndex + 1);
        }
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }
}
