package Services;

import Entities.Like;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LikeCRUD implements InterfaceCRUD<Like> {

    Connection conn;

    public LikeCRUD() {
        conn = MyBD.getInstance().getConn();
        if (conn == null) {
            System.out.println("Connexion base de donnees indisponible pour LikeCRUD.");
            return;
        }
        try {
            ensureUserIdColumn();
        } catch (SQLException e) {
            System.out.println("Impossible de verifier la colonne user_id likes: " + e.getMessage());
        }
    }

    private void ensureUserIdColumn() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SHOW COLUMNS FROM likes LIKE 'user_id'")) {
            if (!rs.next()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE likes ADD COLUMN user_id INT NULL");
                }
            }
        }

        addForeignKeyIfMissing(
                "fk_likes_user",
                "ALTER TABLE likes ADD CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL"
        );
    }

    private void addForeignKeyIfMissing(String constraintName, String ddl) throws SQLException {
        String req = "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS "
                + "WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = ? AND CONSTRAINT_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, "likes");
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
            System.out.println("Contrainte FK likes ignoree: " + e.getMessage());
        }
    }

    private void requireConnection() throws SQLException {
        if (conn == null) {
            throw new SQLException("Connexion base de donnees indisponible.");
        }
    }

    @Override
    public void ajouter(Like l) throws SQLException {
        requireConnection();
        String req = "INSERT INTO likes (post_id, user_id, date_creation) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, l.getPostId());
            if (l.getUserId() <= 0) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, l.getUserId());
            }
            ps.setTimestamp(3, l.getDateCreation());
            ps.executeUpdate();
        }

        System.out.println("Like ajoute !");
    }

    @Override
    public void modifier(Like l) {
        System.out.println("Modification non supportee pour Like");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        requireConnection();
        String req = "DELETE FROM likes WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        System.out.println("Like supprime !");
    }

    @Override
    public List<Like> afficher() throws SQLException {
        requireConnection();
        String req = "SELECT * FROM likes";
        List<Like> list = new ArrayList<>();

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Like l = new Like();
                l.setId(rs.getInt("id"));
                l.setPostId(rs.getInt("post_id"));
                l.setUserId(rs.getInt("user_id"));
                l.setDateCreation(rs.getTimestamp("date_creation"));
                list.add(l);
            }
        }

        return list;
    }

    public int countLikesByPost(int postId) throws SQLException {
        requireConnection();
        String req = "SELECT COUNT(*) FROM likes WHERE post_id=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
