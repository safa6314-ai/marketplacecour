package Services;

import Entites.User;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserCRUD implements InterfaceCRUD<User> {
    Connection conn;

    public UserCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String req =
                "insert into users (username, email, password, first_name, last_name, role, status) " +
                        "values('" + user.getUsername() + "','"
                        + user.getEmail() + "','"
                        + user.getPassword() + "','"
                        + user.getFirst_name() + "','"
                        + user.getLast_name() + "','"
                        + user.getRole() + "','"
                        + user.getStatus() + "')";

        Statement st = conn.createStatement();
        st.executeUpdate(req);
        System.out.println("Utilisateur ajouté !!");
    }

    @Override
    public void modifier(User user) throws SQLException {
        String req = "update users set username=?, email=?, password=?, first_name=?, last_name=?, role=?, status=? where id=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, user.getUsername());
        pst.setString(2, user.getEmail());
        pst.setString(3, user.getPassword());
        pst.setString(4, user.getFirst_name());
        pst.setString(5, user.getLast_name());
        pst.setString(6, user.getRole());
        pst.setString(7, user.getStatus());
        pst.setInt(8, user.getId());

        pst.executeUpdate();
        System.out.println("Utilisateur modifié");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "delete from users where id=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();

        System.out.println("Utilisateur supprimé");
    }

    @Override
    public List<User> afficher() throws SQLException {

        String req = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setPassword(rs.getString("password"));
            u.setFirst_name(rs.getString("first_name"));
            u.setLast_name(rs.getString("last_name"));
            u.setRole(rs.getString("role"));
            u.setStatus(rs.getString("status"));
            u.setCreated_at(rs.getTimestamp("created_at"));

            users.add(u);
        }

        return users;
    }
}