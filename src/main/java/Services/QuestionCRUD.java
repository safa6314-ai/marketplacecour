package Services;

import Entities.Question;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionCRUD implements InterfaceCRUD<Question> {

    private final Connection conn;

    public QuestionCRUD() {
        conn = MyBD.getInstance().getConn();
        if (conn != null) {
            try {
                ensureTable();
            } catch (SQLException e) {
                System.out.println("Impossible de verifier la table question: " + e.getMessage());
            }
        }
    }

    private void ensureTable() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS question (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      contenu VARCHAR(500) NOT NULL,
                      categorie VARCHAR(100) NOT NULL,
                      niveau VARCHAR(50) NOT NULL
                    )
                    """);
        }
    }

    private void requireConnection() throws SQLException {
        if (conn == null) {
            throw new SQLException("Connexion base de donnees indisponible.");
        }
    }

    @Override
    public void ajouter(Question question) throws SQLException {
        requireConnection();
        String req = "INSERT INTO question (contenu, categorie, niveau) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, question.getContenu());
            ps.setString(2, question.getCategorie());
            ps.setString(3, question.getNiveau());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    question.setId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Question question) throws SQLException {
        requireConnection();
        String req = "UPDATE question SET contenu=?, categorie=?, niveau=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, question.getContenu());
            ps.setString(2, question.getCategorie());
            ps.setString(3, question.getNiveau());
            ps.setInt(4, question.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        requireConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Question> afficher() throws SQLException {
        requireConnection();
        List<Question> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM question ORDER BY id")) {
            while (rs.next()) {
                list.add(mapQuestion(rs));
            }
        }
        return list;
    }

    @Override
    public Question getById(int id) throws SQLException {
        requireConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM question WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapQuestion(rs) : null;
            }
        }
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        return new Question(
                rs.getInt("id"),
                rs.getString("contenu"),
                rs.getString("categorie"),
                rs.getString("niveau")
        );
    }
}
