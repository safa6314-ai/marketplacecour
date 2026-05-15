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
    public void ajouter(Reponse r) throws SQLException {
        Connection connection = getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            if (r.isCorrect()) {
                retirerAncienneReponseCorrecte(r.getQuestion_id(), 0);
            }

            String req = "INSERT INTO reponse (question_id, contenu, " + colonneCorrecte() + ") VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, r.getQuestion_id());
            ps.setString(2, r.getContenu());
            ps.setBoolean(3, r.isCorrect());
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public void modifier(Reponse r) throws SQLException {
        Connection connection = getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            if (r.isCorrect()) {
                retirerAncienneReponseCorrecte(r.getQuestion_id(), r.getId());
            }

            String req = "UPDATE reponse SET question_id=?, contenu=?, " + colonneCorrecte() + "=? WHERE id=?";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, r.getQuestion_id());
            ps.setString(2, r.getContenu());
            ps.setBoolean(3, r.isCorrect());
            ps.setInt(4, r.getId());
            ps.executeUpdate();
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
        String req = "DELETE FROM reponse WHERE id=?";
        PreparedStatement ps = getConnection().prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Reponse> afficher() throws SQLException {
        List<Reponse> list = new ArrayList<>();
        String req = "SELECT * FROM reponse";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            list.add(mapperReponse(rs));
        }

        return list;
    }

    protected Reponse mapperReponse(ResultSet rs) throws SQLException {
        Reponse r = new Reponse();
        r.setId(rs.getInt("id"));
        r.setContenu(rs.getString("contenu"));
        r.setCorrect(lireCorrect(rs));
        r.setQuestion_id(rs.getInt("question_id"));
        r.setDateCreation(lireDateCreation(rs));
        return r;
    }

    protected void retirerAncienneReponseCorrecte(int questionId, int reponseIdAConserver) throws SQLException {
        String req = "UPDATE reponse SET " + colonneCorrecte() + "=false WHERE question_id=? AND id<>?";
        PreparedStatement ps = getConnection().prepareStatement(req);
        ps.setInt(1, questionId);
        ps.setInt(2, reponseIdAConserver);
        ps.executeUpdate();
    }

    protected boolean existeDoublon(int questionId, String contenu, int idAExclure) throws SQLException {
        String req = "SELECT COUNT(*) FROM reponse WHERE question_id=? AND LOWER(TRIM(contenu))=LOWER(TRIM(?)) AND id<>?";
        PreparedStatement ps = getConnection().prepareStatement(req);
        ps.setInt(1, questionId);
        ps.setString(2, contenu);
        ps.setInt(3, idAExclure);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    protected String colonneCorrecte() throws SQLException {
        if (colonneTableExiste("est_correcte")) {
            return "est_correcte";
        }
        if (colonneTableExiste("isCorrect")) {
            return "isCorrect";
        }
        return "est_correcte";
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
        ResultSet columns = getConnection().getMetaData().getColumns(null, null, "reponse", colonne);
        if (columns.next()) {
            return true;
        }

        columns = getConnection().getMetaData().getColumns(null, null, "reponse", colonne.toLowerCase());
        return columns.next();
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
        try {
            Statement st = getConnection().createStatement();
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS reponse (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        question_id INT NOT NULL,
                        contenu VARCHAR(255) NOT NULL,
                        est_correcte BOOLEAN DEFAULT false,
                        FOREIGN KEY (question_id) REFERENCES question(id)
                    )
                    """);

            if (!colonneTableExiste("est_correcte")) {
                st.executeUpdate("ALTER TABLE reponse ADD COLUMN est_correcte BOOLEAN DEFAULT false");
            }

            if (colonneTableExiste("isCorrect")) {
                st.executeUpdate("UPDATE reponse SET est_correcte=isCorrect WHERE est_correcte=false");
            }
        } catch (SQLException ignored) {
        }
    }
}
