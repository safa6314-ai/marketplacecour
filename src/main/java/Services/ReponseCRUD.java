package Services;

import Entities.Reponse;
import Interfaces.InterfaceCRUD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReponseCRUD extends BaseCRUD implements InterfaceCRUD<Reponse> {

    public ReponseCRUD() {
        initialiserSchemaQcm();
    }

    @Override
    public void ajouter(Reponse reponse) throws SQLException {
        Connection connection = getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            if (reponse.isCorrect()) {
                retirerAncienneReponseCorrecte(reponse.getQuestion_id(), 0);
            }

            String req = "INSERT INTO reponse (contenu, " + colonneCorrecte() + ", question_id) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, reponse.getContenu());
                ps.setBoolean(2, reponse.isCorrect());
                ps.setInt(3, reponse.getQuestion_id());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        reponse.setId(keys.getInt(1));
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public void modifier(Reponse reponse) throws SQLException {
        Connection connection = getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            if (reponse.isCorrect()) {
                retirerAncienneReponseCorrecte(reponse.getQuestion_id(), reponse.getId());
            }

            String req = "UPDATE reponse SET contenu=?, " + colonneCorrecte() + "=?, question_id=? WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(req)) {
                ps.setString(1, reponse.getContenu());
                ps.setBoolean(2, reponse.isCorrect());
                ps.setInt(3, reponse.getQuestion_id());
                ps.setInt(4, reponse.getId());
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM reponse WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Reponse> afficher() throws SQLException {
        List<Reponse> list = new ArrayList<>();
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reponse ORDER BY id")) {
            while (rs.next()) {
                list.add(mapperReponse(rs));
            }
        }
        return list;
    }

    public List<Reponse> afficherParQuestion(int questionId) throws SQLException {
        return afficherParQuestionId(questionId);
    }

    public List<Reponse> afficherParQuestionId(int questionId) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM reponse WHERE question_id=? ORDER BY id")) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reponses.add(mapperReponse(rs));
                }
            }
        }
        return reponses;
    }

    protected Reponse mapperReponse(ResultSet rs) throws SQLException {
        Reponse reponse = new Reponse();
        reponse.setId(rs.getInt("id"));
        reponse.setContenu(rs.getString("contenu"));
        reponse.setCorrect(lireCorrect(rs));
        reponse.setQuestion_id(rs.getInt("question_id"));
        reponse.setDateCreation(lireDateCreation(rs));
        return reponse;
    }

    protected void retirerAncienneReponseCorrecte(int questionId, int reponseIdAConserver) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE reponse SET " + colonneCorrecte() + "=false WHERE question_id=? AND id<>?")) {
            ps.setInt(1, questionId);
            ps.setInt(2, reponseIdAConserver);
            ps.executeUpdate();
        }
    }

    protected boolean existeDoublon(int questionId, String contenu, int idAExclure) throws SQLException {
        String req = "SELECT COUNT(*) FROM reponse WHERE question_id=? AND LOWER(TRIM(contenu))=LOWER(TRIM(?)) AND id<>?";
        try (PreparedStatement ps = getConnection().prepareStatement(req)) {
            ps.setInt(1, questionId);
            ps.setString(2, contenu);
            ps.setInt(3, idAExclure);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    protected String colonneCorrecte() throws SQLException {
        if (colonneTableExiste("est_correcte")) {
            return "est_correcte";
        }
        if (colonneTableExiste("isCorrect")) {
            return "isCorrect";
        }
        return "isCorrect";
    }

    protected boolean lireCorrect(ResultSet rs) throws SQLException {
        if (colonneExiste(rs, "est_correcte")) {
            return rs.getBoolean("est_correcte");
        }
        if (colonneExiste(rs, "isCorrect")) {
            return rs.getBoolean("isCorrect");
        }
        return false;
    }

    protected LocalDateTime lireDateCreation(ResultSet rs) throws SQLException {
        String[] colonnesPossibles = {"date_creation", "created_at", "date", "createdAt"};
        for (String colonne : colonnesPossibles) {
            if (colonneExiste(rs, colonne)) {
                Timestamp timestamp = rs.getTimestamp(colonne);
                return timestamp != null ? timestamp.toLocalDateTime() : null;
            }
        }
        return null;
    }

    private boolean colonneTableExiste(String colonne) throws SQLException {
        try (ResultSet columns = getConnection().getMetaData().getColumns(null, null, "reponse", colonne)) {
            if (columns.next()) {
                return true;
            }
        }
        try (ResultSet columns = getConnection().getMetaData().getColumns(null, null, "reponse", colonne.toLowerCase())) {
            return columns.next();
        }
    }

    private boolean colonneExiste(ResultSet rs, String colonne) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (metaData.getColumnLabel(i).equalsIgnoreCase(colonne)) {
                return true;
            }
        }
        return false;
    }

    private void initialiserSchemaQcm() {
        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS question (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      contenu VARCHAR(500) NOT NULL,
                      categorie VARCHAR(100) NOT NULL,
                      niveau VARCHAR(50) NOT NULL
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS reponse (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      contenu VARCHAR(500) NOT NULL,
                      isCorrect BOOLEAN NOT NULL DEFAULT false,
                      question_id INT NOT NULL,
                      CONSTRAINT fk_reponse_question
                        FOREIGN KEY (question_id) REFERENCES question(id)
                        ON DELETE CASCADE
                    )
                    """);
            if (colonneTableExiste("est_correcte") && colonneTableExiste("isCorrect")) {
                st.executeUpdate("UPDATE reponse SET isCorrect=est_correcte WHERE isCorrect=false");
            }
        } catch (SQLException e) {
            System.out.println("Impossible de verifier la table reponse: " + e.getMessage());
        }
    }
}
