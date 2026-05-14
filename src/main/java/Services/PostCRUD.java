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
            ensureStatutColumn();
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

    private void ensureStatutColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM post LIKE 'statut'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE post ADD COLUMN statut VARCHAR(20) DEFAULT 'en_attente'");
            }
        }
    }

    private void requireConnection() throws SQLException {
        if (conn == null) {
            throw new SQLException("Connexion base de donnees indisponible.");
        }
    }


    @Override
    public void ajouter(Post post) throws SQLException {
        requireConnection();

        String req = "INSERT INTO post(contenu, date_creation, image_path, statut) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, post.getContenu());
            ps.setTimestamp(2, post.getDateCreation());
            ps.setString(3, post.getImagePath());
            ps.setString(4, post.getStatut());
            ps.executeUpdate();
        }

        System.out.println("Post ajouté !");
    }


    @Override
    public void modifier(Post post) throws SQLException {
        requireConnection();

        String req = "UPDATE post SET contenu=?, date_creation=?, image_path=?, statut=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, post.getContenu());
            ps.setTimestamp(2, post.getDateCreation());
            ps.setString(3, post.getImagePath());
            ps.setString(4, post.getStatut());
            ps.setInt(5, post.getId());
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
                p.setStatut(rs.getString("statut"));

                posts.add(p);
            }
        }

        return posts;
    }
}
