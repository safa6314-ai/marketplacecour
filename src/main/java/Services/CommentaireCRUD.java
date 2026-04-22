package Services;

import Entities.Commentaire;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireCRUD implements InterfaceCRUD<Commentaire> {

    Connection conn;

    public CommentaireCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    // CREATE
    @Override
    public void ajouter(Commentaire c) throws SQLException {

        String req = "INSERT INTO commentaire (contenu, date_creation, post_id) VALUES ('"
                + c.getContenu() + "', '"
                + c.getDateCreation() + "', "
                + c.getPostId() + ")";

        Statement st = conn.createStatement();
        st.executeUpdate(req);

        System.out.println("Commentaire ajouté !");
    }

    // UPDATE
    @Override
    public void modifier(Commentaire c) throws SQLException {

        String req = "UPDATE commentaire SET contenu='"
                + c.getContenu() + "', date_creation='"
                + c.getDateCreation() + "', post_id="
                + c.getPostId() + " WHERE id="
                + c.getId();

        Statement st = conn.createStatement();
        st.executeUpdate(req);

        System.out.println("Commentaire modifié !");
    }

    // DELETE
    @Override
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM commentaire WHERE id=" + id;

        Statement st = conn.createStatement();
        st.executeUpdate(req);

        System.out.println("Commentaire supprimé !");
    }

    // READ
    @Override
    public List<Commentaire> afficher() throws SQLException {

        String req = "SELECT * FROM commentaire";
        List<Commentaire> list = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId(rs.getInt("id"));
            c.setContenu(rs.getString("contenu"));
            c.setDateCreation(rs.getTimestamp("date_creation"));
            c.setPostId(rs.getInt("post_id"));

            list.add(c);
        }

        return list;
    }
    public List<Commentaire> afficherParPost(int postId) throws SQLException {

        String req = "SELECT * FROM commentaire WHERE post_id = " + postId;
        List<Commentaire> list = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId(rs.getInt("id"));
            c.setContenu(rs.getString("contenu"));
            c.setDateCreation(rs.getTimestamp("date_creation"));
            c.setPostId(rs.getInt("post_id"));

            list.add(c);
        }

        return list;
    }
}
