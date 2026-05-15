package Controllers.post;

import Entities.Post;
import Services.LikeCRUD;
import Services.PostCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.ResourceBundle;

public class AfficherLikeController implements Initializable {

    @FXML private ListView<Post> listLikes;
    @FXML private Label lblStatus;

    private final PostCRUD postCRUD = new PostCRUD();
    private final LikeCRUD likeCRUD = new LikeCRUD();
    private final ObservableList<Post> posts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listLikes.setItems(posts);
        listLikes.setCellFactory(view -> new LikeCell());
        rafraichir();
    }

    @FXML
    public void rafraichir() {
        try {
            posts.setAll(postCRUD.afficher().stream()
                    .sorted(Comparator.comparingInt(this::countLikes).reversed())
                    .toList());
            lblStatus.setText(likeCRUD.afficher().size() + " like(s) au total.");
        } catch (SQLException e) {
            lblStatus.setText("Erreur : " + e.getMessage());
        }
    }

    private int countLikes(Post post) {
        try {
            return likeCRUD.countLikesByPost(post.getId());
        } catch (SQLException e) {
            return 0;
        }
    }

    private class LikeCell extends ListCell<Post> {
        private final VBox card = new VBox(8);
        private final Label content = new Label();
        private final Label likes = new Label();

        LikeCell() {
            card.getStyleClass().add("post-card");
            content.getStyleClass().add("post-content");
            content.setWrapText(true);
            likes.getStyleClass().add("status-badge");
            card.getChildren().addAll(content, likes);
        }

        @Override
        protected void updateItem(Post post, boolean empty) {
            super.updateItem(post, empty);
            if (empty || post == null) {
                setGraphic(null);
                return;
            }

            String text = post.getContenu() == null ? "" : post.getContenu();
            content.setText(text.length() > 160 ? text.substring(0, 160) + "..." : text);
            likes.setText(countLikes(post) + " like(s)");
            setGraphic(card);
        }
    }
}
