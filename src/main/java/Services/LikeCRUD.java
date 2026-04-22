package Services;

import Entities.Like;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LikeCRUD implements InterfaceCRUD<Like> {

    Connection conn;

    public LikeCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    // ✅ AJOUTER LIKE
    @Override
    public void ajouter(Like l) throws SQLException {
        String req = "INSERT INTO likes (post_id, date_creation) VALUES ("
                + l.getPostId() + ", '"
                + l.getDateCreation() + "')";
        Statement st = conn.createStatement();
        st.executeUpdate(req);
        System.out.println("Like ajouté !");
    }

    // ❌ INUTILE MAIS OBLIGATOIRE SI INTERFACE
    @Override
    public void modifier(Like l) throws SQLException {
        System.out.println("❌ Modification non supportée pour Like");
    }

    // ✅ SUPPRIMER LIKE
    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM likes WHERE id=" + id;
        Statement st = conn.createStatement();
        st.executeUpdate(req);
        System.out.println("Like supprimé !");
    }

    // ✅ AFFICHER
    @Override
    public List<Like> afficher() throws SQLException {
        String req = "SELECT * FROM likes";
        List<Like> list = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Like l = new Like();
            l.setId(rs.getInt("id"));
            l.setPostId(rs.getInt("post_id"));
            l.setDateCreation(rs.getTimestamp("date_creation"));
            list.add(l);
        }
        return list;
    }

    // 🔥 COMPTER LES LIKES D’UN POST
    public int countLikesByPost(int postId) throws SQLException {
        String req = "SELECT COUNT(*) FROM likes WHERE post_id=" + postId;
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
}
