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
            ensureUserIdColumn();
            ensureStatutColumn();
            ensureModerationScoreColumn();
            ensureModerationMessageColumn();
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

    private void ensureUserIdColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM commentaire LIKE 'user_id'")) {
            if (!rs.next()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE commentaire ADD COLUMN user_id INT NULL");
                }
            }
        }

        addForeignKeyIfMissing(
                "fk_commentaire_user",
                "ALTER TABLE commentaire ADD CONSTRAINT fk_commentaire_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL"
        );
    }

    private void addForeignKeyIfMissing(String constraintName, String ddl) throws SQLException {
        String req = "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS "
                + "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = ? AND CONSTRAINT_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, "commentaire");
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
            System.out.println("Contrainte FK commentaire ignoree: " + e.getMessage());
        }
    }

    private void ensureModerationScoreColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM commentaire LIKE 'moderation_score'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE commentaire ADD COLUMN moderation_score DOUBLE DEFAULT NULL");
            }
        }
    }

    private void ensureModerationMessageColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM commentaire LIKE 'moderation_message'")) {
            if (rs.next()) return;

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE commentaire ADD COLUMN moderation_message VARCHAR(500) DEFAULT NULL");
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
        String req = "INSERT INTO commentaire (contenu, date_creation, post_id, user_id, statut, moderation_score, moderation_message) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, c.getContenu());
            ps.setTimestamp(2, c.getDateCreation());
            ps.setInt(3, c.getPostId());
            if (c.getUserId() <= 0) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, c.getUserId());
            }
            ps.setString(5, c.getStatut());
            if (c.getModerationScore() == null) {
                ps.setNull(6, java.sql.Types.DOUBLE);
            } else {
                ps.setDouble(6, c.getModerationScore());
            }
            ps.setString(7, c.getModerationMessage());
            ps.executeUpdate();
        }

        System.out.println("Commentaire ajoute !");
    }

    @Override
    public void modifier(Commentaire c) throws SQLException {
        requireConnection();
        String req = "UPDATE commentaire SET contenu=?, date_creation=?, post_id=?, user_id=?, statut=?, moderation_score=?, moderation_message=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, c.getContenu());
            ps.setTimestamp(2, c.getDateCreation());
            ps.setInt(3, c.getPostId());
            if (c.getUserId() <= 0) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, c.getUserId());
            }
            ps.setString(5, c.getStatut());
            if (c.getModerationScore() == null) {
                ps.setNull(6, java.sql.Types.DOUBLE);
            } else {
                ps.setDouble(6, c.getModerationScore());
            }
            ps.setString(7, c.getModerationMessage());
            ps.setInt(8, c.getId());
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

    public void modifierModeration(int commentaireId, String statut, double score, String message) throws SQLException {
        requireConnection();
        String req = "UPDATE commentaire SET statut=?, moderation_score=?, moderation_message=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, statut);
            ps.setDouble(2, score);
            ps.setString(3, message);
            ps.setInt(4, commentaireId);
            ps.executeUpdate();
        }

        System.out.println("Moderation commentaire modifiee !");
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
        c.setUserId(rs.getInt("user_id"));
        c.setStatut(rs.getString("statut"));
        double score = rs.getDouble("moderation_score");
        c.setModerationScore(rs.wasNull() ? null : score);
        c.setModerationMessage(rs.getString("moderation_message"));
        return c;
    }
}
