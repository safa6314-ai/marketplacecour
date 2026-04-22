package Services;

import Entities.Post;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostCRUD implements InterfaceCRUD<Post> {

    Connection conn;

    public PostCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    // CREATE
    @Override
    public void ajouter(Post post) throws SQLException {

        String req = "INSERT INTO post(contenu, date_creation) VALUES ('"
                + post.getContenu() + "', '"
                + post.getDateCreation() + "')";

        Statement st = conn.createStatement();
        st.executeUpdate(req);

        System.out.println("Post ajouté !");
    }

    // UPDATE
    @Override
    public void modifier(Post post) throws SQLException {

        String req = "UPDATE post SET contenu='"
                + post.getContenu() + "', date_creation='"
                + post.getDateCreation() + "' WHERE id="
                + post.getId();

        Statement st = conn.createStatement();
        st.executeUpdate(req);

        System.out.println("Post modifié !");
    }

    // DELETE
    @Override
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM post WHERE id=" + id;

        Statement st = conn.createStatement();
        st.executeUpdate(req);

        System.out.println("Post supprimé !");
    }

    // READ
    @Override
    public List<Post> afficher() throws SQLException {

        String req = "SELECT * FROM post";
        List<Post> posts = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Post p = new Post();
            p.setId(rs.getInt("id"));
            p.setContenu(rs.getString("contenu"));
            p.setDateCreation(rs.getTimestamp("date_creation"));

            posts.add(p);
        }

        return posts;
    }
}