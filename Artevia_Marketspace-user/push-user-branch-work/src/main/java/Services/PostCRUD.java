package Services;

import Entities.Post;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostCRUD implements InterfaceCRUD<Post> {

    Connection conn;

    public PostCRUD() {
        conn = MyBD.getInstance().getConn();
        if (conn == null) {
            System.out.println("Connexion base de donnees indisponible pour PostCRUD.");
            return;
        }
        try {
            ensureImagePathColumn();
            ensureUserIdColumn();
            ensureStatutColumn();
            ensureCategorieColumn();
            ensureModerationScoreColumn();
            ensureModerationMessageColumn();
        } catch (SQLException e) {
            System.out.println("Impossible de verifier les colonnes post: " + e.getMessage());
        }
    }

    private void ensureImagePathColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM post LIKE 'image_path'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE post ADD COLUMN image_path VARCHAR(500) DEFAULT NULL");
            }
        }
    }

    private void ensureUserIdColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM post LIKE 'user_id'")) {
            if (!rs.next()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE post ADD COLUMN user_id INT NULL");
                }
            }
        }

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE post ADD INDEX IF NOT EXISTS idx_post_user_id (user_id)");
        } catch (SQLException ignored) {
            // MySQL variants used in class projects may not support IF NOT EXISTS for indexes.
        }

        addForeignKeyIfMissing(
                "fk_post_user",
                "ALTER TABLE post ADD CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL"
        );
    }

    private void ensureStatutColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM post LIKE 'statut'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE post ADD COLUMN statut VARCHAR(20) DEFAULT 'en_attente'");
            }
        }
    }

    private void ensureCategorieColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM post LIKE 'categorie'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE post ADD COLUMN categorie VARCHAR(50) DEFAULT 'Discussion generale'");
            }
        }
    }

    private void ensureModerationScoreColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM post LIKE 'moderation_score'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE post ADD COLUMN moderation_score DOUBLE DEFAULT NULL");
            }
        }
    }

    private void ensureModerationMessageColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM post LIKE 'moderation_message'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE post ADD COLUMN moderation_message VARCHAR(500) DEFAULT NULL");
            }
        }
    }

    private void requireConnection() throws SQLException {
        if (conn == null) {
            throw new SQLException("Connexion base de donnees indisponible.");
        }
    }

    private void addForeignKeyIfMissing(String constraintName, String ddl) throws SQLException {
        String req = "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS "
                + "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = ? AND CONSTRAINT_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, "post");
            ps.setString(2, constraintName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
        } catch (SQLException e) {
            System.out.println("Contrainte FK post ignoree: " + e.getMessage());
        }
    }


    @Override
    public void ajouter(Post post) throws SQLException {
        requireConnection();

        String req = "INSERT INTO post(contenu, date_creation, image_path, user_id, statut, categorie, moderation_score, moderation_message) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, post.getContenu());
            ps.setTimestamp(2, post.getDateCreation());
            ps.setString(3, post.getImagePath());
            if (post.getUserId() <= 0) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, post.getUserId());
            }
            ps.setString(5, post.getStatut());
            ps.setString(6, post.getCategorie());
            if (post.getModerationScore() == null) {
                ps.setNull(7, java.sql.Types.DOUBLE);
            } else {
                ps.setDouble(7, post.getModerationScore());
            }
            ps.setString(8, post.getModerationMessage());
            ps.executeUpdate();
        }

        System.out.println("Post ajouté !");
    }


    @Override
    public void modifier(Post post) throws SQLException {
        requireConnection();

        String req = "UPDATE post SET contenu=?, date_creation=?, image_path=?, user_id=?, statut=?, categorie=?, moderation_score=?, moderation_message=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, post.getContenu());
            ps.setTimestamp(2, post.getDateCreation());
            ps.setString(3, post.getImagePath());
            if (post.getUserId() <= 0) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, post.getUserId());
            }
            ps.setString(5, post.getStatut());
            ps.setString(6, post.getCategorie());
            if (post.getModerationScore() == null) {
                ps.setNull(7, java.sql.Types.DOUBLE);
            } else {
                ps.setDouble(7, post.getModerationScore());
            }
            ps.setString(8, post.getModerationMessage());
            ps.setInt(9, post.getId());
            ps.executeUpdate();
        }

        System.out.println("Post modifié !");
    }


    public void modifierStatut(int postId, String statut) throws SQLException {
        requireConnection();
        String req = "UPDATE post SET statut=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, statut);
            ps.setInt(2, postId);
            ps.executeUpdate();
        }

        System.out.println("Statut post modifie !");
    }

    public void modifierModeration(int postId, String statut, double score, String message, String categorie) throws SQLException {
        requireConnection();
        String req = "UPDATE post SET statut=?, moderation_score=?, moderation_message=?, categorie=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, statut);
            ps.setDouble(2, score);
            ps.setString(3, message);
            ps.setString(4, categorie);
            ps.setInt(5, postId);
            ps.executeUpdate();
        }

        System.out.println("Moderation post modifiee !");
    }


    @Override
    public void supprimer(int id) throws SQLException {
        requireConnection();

        try (PreparedStatement deleteLikes = conn.prepareStatement("DELETE FROM likes WHERE post_id=?");
             PreparedStatement deleteCommentaires = conn.prepareStatement("DELETE FROM commentaire WHERE post_id=?");
             PreparedStatement deletePost = conn.prepareStatement("DELETE FROM post WHERE id=?")) {
            deleteLikes.setInt(1, id);
            deleteLikes.executeUpdate();

            deleteCommentaires.setInt(1, id);
            deleteCommentaires.executeUpdate();

            deletePost.setInt(1, id);
            deletePost.executeUpdate();
        }

        System.out.println("Post supprimé !");
    }


    @Override
    public List<Post> afficher() throws SQLException {
        requireConnection();

        String req = "SELECT * FROM post";
        List<Post> posts = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id"));
                p.setContenu(rs.getString("contenu"));
                p.setDateCreation(rs.getTimestamp("date_creation"));
                p.setImagePath(rs.getString("image_path"));
                p.setUserId(rs.getInt("user_id"));
                p.setStatut(rs.getString("statut"));
                p.setCategorie(rs.getString("categorie"));
                double score = rs.getDouble("moderation_score");
                p.setModerationScore(rs.wasNull() ? null : score);
                p.setModerationMessage(rs.getString("moderation_message"));

                posts.add(p);
            }
        }

        return posts;
    }
}
