package Services;

import Entities.Commentaire;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CommentaireCRUD implements InterfaceCRUD<Commentaire> {

    Connection conn;

    public CommentaireCRUD() {
        conn = MyBD.getInstance().getConn();
        if (conn == null) {
            System.out.println("Connexion base de donnees indisponible pour CommentaireCRUD.");
            return;
        }
        try {
            ensureStatutColumn();
        } catch (SQLException e) {
            System.out.println("Impossible de verifier la colonne statut commentaire: " + e.getMessage());
        }
    }

    private void ensureStatutColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM commentaire LIKE 'statut'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE commentaire ADD COLUMN statut VARCHAR(20) DEFAULT 'en_attente'");
            }
        }
    }

    private void requireConnection() throws SQLException {
        if (conn == null) {
            throw new SQLException("Connexion base de donnees indisponible.");
        }
    }

    @Override
    public void ajouter(Commentaire c) throws SQLException {
        requireConnection();
        String req = "INSERT INTO commentaire (contenu, date_creation, post_id, statut) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, c.getContenu());
            ps.setTimestamp(2, c.getDateCreation());
            ps.setInt(3, c.getPostId());
            ps.setString(4, c.getStatut());
            ps.executeUpdate();
        }

        System.out.println("Commentaire ajoute !");
    }

    @Override
    public void modifier(Commentaire c) throws SQLException {
        requireConnection();
        String req = "UPDATE commentaire SET contenu=?, date_creation=?, post_id=?, statut=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, c.getContenu());
            ps.setTimestamp(2, c.getDateCreation());
            ps.setInt(3, c.getPostId());
            ps.setString(4, c.getStatut());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        }

        System.out.println("Commentaire modifie !");
    }

    public void modifierStatut(int commentaireId, String statut) throws SQLException {
        requireConnection();
        String req = "UPDATE commentaire SET statut=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, statut);
            ps.setInt(2, commentaireId);
            ps.executeUpdate();
        }

        System.out.println("Statut commentaire modifie !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        requireConnection();
        String req = "DELETE FROM commentaire WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        System.out.println("Commentaire supprime !");
    }

    @Override
    public List<Commentaire> afficher() throws SQLException {
        requireConnection();
        String req = "SELECT * FROM commentaire";
        List<Commentaire> list = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapCommentaire(rs));
            }
        }

        return list;
    }

    public List<Commentaire> afficherParPost(int postId) throws SQLException {
        requireConnection();
        String req = "SELECT * FROM commentaire WHERE post_id = ?";
        List<Commentaire> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCommentaire(rs));
                }
            }
        }

        return list;
    }

    private Commentaire mapCommentaire(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setId(rs.getInt("id"));
        c.setContenu(rs.getString("contenu"));
        c.setDateCreation(rs.getTimestamp("date_creation"));
        c.setPostId(rs.getInt("post_id"));
        c.setStatut(rs.getString("statut"));
        return c;
    }
}
