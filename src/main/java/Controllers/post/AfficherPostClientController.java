package Controllers.post;

import Controllers.commentaire.AfficherCommentaireController;
import Entities.Like;
import Entities.Post;
import Services.CommentaireCRUD;
import Services.LikeCRUD;
import Services.PostCRUD;
import Services.SentimentService;
import Services.TraductionService;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class AfficherPostClientController implements Initializable {

    @FXML private FlowPane feedGrid;
    @FXML private TextField tfRecherche;
    @FXML private Label lblPage;
    @FXML private Label lblStatus;

    private final PostCRUD postCRUD = new PostCRUD();
    private final CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private final LikeCRUD likeCRUD = new LikeCRUD();
    private final SentimentService sentimentService = new SentimentService();
    private final TraductionService traductionService = new TraductionService();

    private final Map<Integer, Integer> likedPosts = new HashMap<>();
    private final Map<Integer, String> sentimentCache = new HashMap<>();
    private final ObservableList<Post> pageCourante = FXCollections.observableArrayList();

    private List<Post> tousLesPosts = new ArrayList<>();
    private List<Post> postsFiltres = new ArrayList<>();
    private static final int POSTS_PAR_PAGE = 5;
    private int pageActuelle = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tfRecherche.textProperty().addListener((obs, oldValue, newValue) -> {
            pageActuelle = 0;
            appliquerFiltre(newValue);
        });
        chargerLikesPrecedents();
        charger();
    }

    @FXML
    public void ouvrirDialogAjouter(ActionEvent event) {
        Dialog<Post> dialog = buildPostDialog("Nouveau post", "Creer une publication", null);
        dialog.showAndWait().ifPresent(post -> {
            try {
                postCRUD.ajouter(post);
                lblStatus.setText("Publication envoyee. Elle apparaitra apres acceptation.");
                charger();
            } catch (SQLException e) {
                lblStatus.setText("Erreur : " + e.getMessage());
            }
        });
    }

    @FXML
    public void rafraichir(ActionEvent event) {
        sentimentCache.clear();
        likedPosts.clear();
        chargerLikesPrecedents();
        charger();
    }

    @FXML
    public void effacerRecherche(ActionEvent event) {
        tfRecherche.clear();
        appliquerFiltre("");
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

    private void chargerLikesPrecedents() {
        try {
            for (Like like : likeCRUD.afficher()) {
                likedPosts.put(like.getPostId(), like.getId());
            }
        } catch (SQLException ignored) {
        }
    }

    private void charger() {
        try {
            tousLesPosts = postCRUD.afficher().stream()
                    .filter(post -> "accepte".equals(post.getStatut()))
                    .collect(Collectors.toList());
            postsFiltres = new ArrayList<>(tousLesPosts);
            pageActuelle = 0;
            afficherPage();
        } catch (SQLException e) {
            lblStatus.setText("Erreur : " + e.getMessage());
        }
    }

    private void appliquerFiltre(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            postsFiltres = new ArrayList<>(tousLesPosts);
        } else {
            String filtre = motCle.toLowerCase().trim();
            postsFiltres = tousLesPosts.stream()
                    .filter(post -> post.getContenu().toLowerCase().contains(filtre))
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

        feedGrid.getChildren().clear();
        if (pageCourante.isEmpty()) {
            Label empty = new Label("Aucune publication acceptee pour le moment. Revenez bientot pour decouvrir les nouveautes de la communaute.");
            empty.getStyleClass().add("heading-subtitle");
            empty.setWrapText(true);
            empty.setPrefWidth(720);
            feedGrid.getChildren().add(empty);
        } else {
            for (Post post : pageCourante) {
                feedGrid.getChildren().add(creerCard(post));
            }
        }

        lblPage.setText("Page " + (pageActuelle + 1) + " / " + nbPages);
        lblStatus.setText(total + " publication(s) dans le fil.");
    }

    private Node creerCard(Post post) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("post-card", "feed-card");
        Node imageArea = creerImagePostArea(post);

        Label sentiment = new Label(sentimentPour(post));
        sentiment.getStyleClass().add("sentiment-badge");

        Label date = new Label(formatDate(post));
        date.getStyleClass().add("status-label");

        HBox header = new HBox(10, new Label("Forum Artevia"), date, sentiment);
        header.getStyleClass().add("feed-card-header");
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(date, Priority.ALWAYS);

        Label contenu = new Label(post.getContenu());
        contenu.getStyleClass().add("post-content");
        contenu.setWrapText(true);
        contenu.setMinHeight(110);

        Button likeButton = new Button();
        likeButton.getStyleClass().add(likedPosts.containsKey(post.getId()) ? "danger-btn" : "secondary-btn");
        mettreAJourLikeButton(likeButton, post);
        likeButton.setOnAction(event -> {
            toggleLike(post);
            mettreAJourLikeButton(likeButton, post);
        });

        Button comments = new Button(compterCommentaires(post) + " commentaire(s)");
        comments.getStyleClass().add("secondary-btn");
        comments.setOnAction(event -> ouvrirCommentairesDialog(post));

        Button translate = new Button("Traduire");
        translate.getStyleClass().add("primary-btn");
        translate.setOnAction(event -> traduirePost(post, contenu, translate));

        Button edit = new Button("Modifier");
        edit.getStyleClass().add("secondary-btn");
        edit.setOnAction(event -> ouvrirDialogModifier(post));

        Button delete = new Button("Supprimer");
        delete.getStyleClass().add("danger-btn");
        delete.setOnAction(event -> confirmerSupprimerPost(post));

        HBox actions = new HBox(8, likeButton, comments, translate);
        HBox crudActions = new HBox(8, edit, delete);
        actions.setAlignment(Pos.CENTER_LEFT);
        crudActions.setAlignment(Pos.CENTER_LEFT);
        if (imageArea != null) {
            card.getChildren().add(imageArea);
        }
        card.getChildren().addAll(header, contenu, actions, crudActions);
        return card;
    }

    private void ouvrirDialogModifier(Post post) {
        Dialog<Post> dialog = buildPostDialog("Modifier le post", "Edition de la publication", post);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                postCRUD.modifier(updated);
                sentimentCache.remove(updated.getId());
                lblStatus.setText("Publication modifiee.");
                charger();
            } catch (SQLException e) {
                lblStatus.setText("Erreur : " + e.getMessage());
            }
        });
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
                    likedPosts.remove(post.getId());
                    sentimentCache.remove(post.getId());
                    lblStatus.setText("Publication supprimee.");
                    charger();
                } catch (SQLException e) {
                    lblStatus.setText("Erreur : " + e.getMessage());
                }
            }
        });
    }

    private Dialog<Post> buildPostDialog(String title, String header, Post existing) {
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        styliserDialog(dialog.getDialogPane());

        Label label = new Label("Contenu");
        label.getStyleClass().add("input-label");
        TextArea contenu = new TextArea();
        contenu.setPromptText("Partagez une idee, une oeuvre ou une question...");
        contenu.setWrapText(true);
        contenu.setPrefHeight(150);
        if (existing != null) contenu.setText(existing.getContenu());

        String[] imagePath = {existing == null ? null : existing.getImagePath()};
        boolean[] imageRemoved = {false};
        VBox imageBox = new VBox(8);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setMinHeight(104);
        imageBox.setStyle("-fx-border-color: #d4956a; -fx-border-style: dashed; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;");
        Runnable[] refreshImageBox = new Runnable[1];
        refreshImageBox[0] = () -> {
            imageBox.getChildren().clear();
            if (imagePath[0] == null || imagePath[0].isBlank()) {
                Label prompt = new Label("Ajouter une photo (optionnel)");
                prompt.getStyleClass().add("status-label");
                imageBox.getChildren().add(prompt);
                return;
            }

            ImageView preview = new ImageView();
            preview.setFitWidth(420);
            preview.setFitHeight(220);
            preview.setPreserveRatio(true);
            chargerImageAsync(imagePath[0], preview);
            Button remove = new Button("Supprimer la photo");
            remove.getStyleClass().add("danger-btn");
            remove.setOnAction(event -> {
                imagePath[0] = null;
                imageRemoved[0] = true;
                refreshImageBox[0].run();
            });
            imageBox.getChildren().addAll(preview, remove);
        };
        imageBox.setOnMouseClicked(event -> {
            if (imagePath[0] != null && !imagePath[0].isBlank()) return;
            choisirPhoto(dialog, imagePath, imageRemoved, refreshImageBox[0]);
        });
        refreshImageBox[0].run();

        VBox form = new VBox(10, label, contenu, imageBox);
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
                existing.setImagePath(imageRemoved[0] ? null : imagePath[0]);
                return existing;
            }
            Post post = new Post(texte, new Timestamp(System.currentTimeMillis()));
            post.setImagePath(imagePath[0]);
            return post;
        });
        return dialog;
    }

    private void choisirPhoto(Dialog<Post> dialog, String[] imagePath, boolean[] imageRemoved, Runnable refreshImageBox) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir une photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
            File file = chooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file == null) return;

            imagePath[0] = copierPhoto(file);
            imageRemoved[0] = false;
            refreshImageBox.run();
        } catch (Exception ignored) {
            lblStatus.setText("Impossible de charger la photo.");
        }
    }

    private String copierPhoto(File source) throws IOException {
        Path imagesDir = Path.of("src", "main", "resources", "images", "posts");
        Files.createDirectories(imagesDir);
        String safeName = source.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destination = imagesDir.resolve("post_" + System.currentTimeMillis() + "_" + safeName);
        Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString().replace("\\", "/");
    }

    private void mettreAJourLikeButton(Button button, Post post) {
        int count = compterLikes(post);
        boolean liked = likedPosts.containsKey(post.getId());
        button.setText((liked ? "Like " : "Like ") + count);
        button.getStyleClass().setAll(liked ? "danger-btn" : "secondary-btn");
    }

    private void toggleLike(Post post) {
        try {
            if (!likedPosts.containsKey(post.getId())) {
                likeCRUD.ajouter(new Like(post.getId(), new Timestamp(System.currentTimeMillis())));
                likeCRUD.afficher().stream()
                        .filter(like -> like.getPostId() == post.getId())
                        .reduce((first, second) -> second)
                        .ifPresent(like -> likedPosts.put(post.getId(), like.getId()));
                lblStatus.setText("Publication aimee.");
            } else {
                likeCRUD.supprimer(likedPosts.get(post.getId()));
                likedPosts.remove(post.getId());
                lblStatus.setText("Like retire.");
            }
        } catch (SQLException e) {
            lblStatus.setText("Erreur : " + e.getMessage());
        }
    }

    private void traduirePost(Post post, Label contenu, Button button) {
        if ("Original".equals(button.getText())) {
            contenu.setText(post.getContenu());
            button.setText("Traduire");
            return;
        }

        ChoiceDialog<String> targetDialog = new ChoiceDialog<>("en", List.of("en", "fr", "ar"));
        targetDialog.setTitle("Traduire");
        targetDialog.setHeaderText("Choisir la langue cible");
        targetDialog.setContentText("Langue :");
        styliserDialog(targetDialog.getDialogPane());

        targetDialog.showAndWait().ifPresent(target -> {
            button.setDisable(true);
            contenu.setText("Traduction en cours...");
            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    return traductionService.traduire(post.getContenu(), "auto", target);
                }
            };
            task.setOnSucceeded(event -> {
                contenu.setText(task.getValue());
                button.setText("Original");
                button.setDisable(false);
            });
            task.setOnFailed(event -> {
                contenu.setText(post.getContenu());
                button.setDisable(false);
                lblStatus.setText("Traduction echouee.");
            });
            Thread thread = new Thread(task, "traduction-client-" + post.getId());
            thread.setDaemon(true);
            thread.start();
        });
    }

    private void ouvrirCommentairesDialog(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/commentaire/AfficherCommentaire.fxml")));
            Node content = loader.load();
            AfficherCommentaireController controller = loader.getController();
            controller.setPostContext(post.getId(), post.getContenu());
            controller.setAdminMode(false);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Commentaires");
            styliserDialog(dialog.getDialogPane());
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            ((Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE)).setText("Fermer");
            dialog.showAndWait();
            afficherPage();
        } catch (IOException e) {
            lblStatus.setText("Impossible d'ouvrir les commentaires.");
        }
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
            afficherPage();
        });
        task.setOnFailed(event -> {
            sentimentCache.put(post.getId(), "?");
            afficherPage();
        });
        Thread thread = new Thread(task, "sentiment-client-" + post.getId());
        thread.setDaemon(true);
        thread.start();
        return "...";
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

    private Node creerImagePostArea(Post post) {
        if (post.getImagePath() == null || post.getImagePath().isBlank()) {
            return null;
        }

        StackPane placeholder = new StackPane();
        Rectangle background = new Rectangle(430, 180);
        background.setArcWidth(18);
        background.setArcHeight(18);
        background.setStyle("-fx-fill: #f0d5c0;");
        Label icon = new Label("Image");
        icon.setStyle("-fx-font-size: 34px;");
        placeholder.getChildren().addAll(background, icon);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(430);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        StackPane imageArea = new StackPane(placeholder);
        imageArea.setPrefSize(430, 180);
        chargerImageAsync(post.getImagePath(), imageView, imageArea, placeholder);
        return imageArea;
    }

    private void chargerImageAsync(String imagePath, ImageView imageView) {
        chargerImageAsync(imagePath, imageView, null, null);
    }

    private void chargerImageAsync(String imagePath, ImageView imageView, StackPane imageArea, Node placeholder) {
        new Thread(() -> {
            try {
                String source = resolveImageSource(imagePath);
                if (source == null) {
                    Platform.runLater(() -> {
                        if (imageArea != null) imageArea.getChildren().clear();
                    });
                    return;
                }
                Image image = new Image(source, false);
                if (image.isError()) {
                    Platform.runLater(() -> {
                        if (imageArea != null) imageArea.getChildren().clear();
                    });
                    return;
                }
                Platform.runLater(() -> {
                    imageView.setImage(image);
                    if (imageArea != null) imageArea.getChildren().setAll(imageView);
                });
            } catch (Exception ignored) {
                Platform.runLater(() -> {
                    if (imageArea != null) {
                        imageArea.getChildren().remove(placeholder);
                    }
                });
            }
        }, "image-load-client").start();
    }

    private String resolveImageSource(String imagePath) {
        try {
            if (imagePath == null || imagePath.isBlank()) return null;
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://") || imagePath.startsWith("file:")) {
                return imagePath;
            }
            File file = Path.of(imagePath).toFile();
            return file.exists() ? file.toURI().toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void styliserDialog(DialogPane pane) {
        pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        pane.getStyleClass().add("dialog-theme");
    }

    private String formatDate(Post post) {
        return post.getDateCreation() == null ? "" : post.getDateCreation().toString().substring(0, 16);
    }
}
