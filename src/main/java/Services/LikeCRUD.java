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
        String req = "INSERT INTO likes (post_id, date_creation) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, l.getPostId());
            ps.setTimestamp(2, l.getDateCreation());
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
