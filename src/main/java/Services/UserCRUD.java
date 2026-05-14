package Services;

import Entites.User;
import Interffaces.InterfaceCRUD;
import Utils.MyBD;
import Utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserCRUD implements InterfaceCRUD<User> {
    private final Connection conn;

    public UserCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(User User) throws SQLException {
        String req = "INSERT INTO users (username, email, password, first_name, last_name, role, status, phone, phone_verified) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, User.getUsername());
        pst.setString(2, User.getEmail());
        pst.setString(3, PasswordUtils.hashPassword(User.getPassword()));
        pst.setString(4, User.getFirst_name());
        pst.setString(5, User.getLast_name());
        pst.setString(6, User.getRole());
        pst.setString(7, User.getStatus());
        pst.setString(8, User.getPhone());
        pst.setBoolean(9, User.isPhone_verified());

        pst.executeUpdate();
        System.out.println("Utilisateur ajoute");
    }

    @Override
    public void modifier(User User) throws SQLException {
        User oldUser = getById(User.getId());
        String passwordToSave = preparePasswordForUpdate(User.getPassword(), oldUser);

        String req = "UPDATE users SET username=?, email=?, password=?, first_name=?, last_name=?, role=?, status=?, profile_image=?, phone=?, phone_verified=? WHERE id=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, User.getUsername());
        pst.setString(2, User.getEmail());
        pst.setString(3, passwordToSave);
        pst.setString(4, User.getFirst_name());
        pst.setString(5, User.getLast_name());
        pst.setString(6, User.getRole());
        pst.setString(7, User.getStatus());
        pst.setString(8, User.getProfile_image());
        pst.setString(9, User.getPhone());
        pst.setBoolean(10, User.isPhone_verified());
        pst.setInt(11, User.getId());

        pst.executeUpdate();
        System.out.println("Utilisateur modifie");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM users WHERE id=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();

        System.out.println("Utilisateur supprime");
    }

    @Override
    public List<User> afficher() throws SQLException {
        String req = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            users.add(mapUser(rs));
        }

        return users;
    }

    public List<User> rechercher(String keyword) throws SQLException {
        String req = "SELECT * FROM users WHERE username LIKE ? OR email LIKE ?";
        List<User> users = new ArrayList<>();

        PreparedStatement pst = conn.prepareStatement(req);
        String searchValue = "%" + keyword + "%";
        pst.setString(1, searchValue);
        pst.setString(2, searchValue);

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            users.add(mapUser(rs));
        }

        return users;
    }

    public User login(String email, String password) throws SQLException {
        User User = getByEmail(email);

        if (User != null && PasswordUtils.checkPassword(password, User.getPassword())) {
            return User;
        }

        return null;
    }

    public int countAllUsers() throws SQLException {
        return countByQuery("SELECT COUNT(*) FROM users");
    }

    public int countByRole(String role) throws SQLException {
        String req = "SELECT COUNT(*) FROM users WHERE role = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, role);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }

        return 0;
    }

    public int countBlockedUsers() throws SQLException {
        return countByQuery("SELECT COUNT(*) FROM users WHERE status = 'BLOCKED'");
    }

    public User getById(int id) throws SQLException {
        String req = "SELECT * FROM users WHERE id = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return mapUser(rs);
        }

        return null;
    }

    public User getByEmail(String email) throws SQLException {
        String req = "SELECT * FROM users WHERE email = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return mapUser(rs);
        }

        return null;
    }

    public User getByPhone(String phone) throws SQLException {
        String req = "SELECT * FROM users WHERE phone = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, phone);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return mapUser(rs);
        }

        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        return getByEmail(email) != null;
    }

    public boolean phoneExists(String phone) throws SQLException {
        return getByPhone(phone) != null;
    }

    public void updatePassword(int userId, String newPassword) throws SQLException {
        String req = "UPDATE users SET password = ? WHERE id = ?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, PasswordUtils.hashPassword(newPassword));
        pst.setInt(2, userId);
        pst.executeUpdate();
    }

    private int countByQuery(String req) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            return rs.getInt(1);
        }

        return 0;
    }

    private String preparePasswordForUpdate(String newPassword, User oldUser) {
        if (oldUser == null) {
            return PasswordUtils.hashPassword(newPassword);
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return oldUser.getPassword();
        }

        if (newPassword.equals(oldUser.getPassword())) {
            return oldUser.getPassword();
        }

        if (PasswordUtils.isBCryptHash(newPassword)) {
            return newPassword;
        }

        return PasswordUtils.hashPassword(newPassword);
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setFirst_name(rs.getString("first_name"));
        u.setLast_name(rs.getString("last_name"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setProfile_image(rs.getString("profile_image"));
        u.setPhone(rs.getString("phone"));
        u.setPhone_verified(rs.getBoolean("phone_verified"));
        u.setCreated_at(rs.getTimestamp("created_at"));
        return u;
    }
}


