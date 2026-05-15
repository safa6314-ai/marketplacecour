package Services;

import Entities.Reponse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReponseService extends ReponseCRUD {

    public void ajouterReponseQcm(Reponse reponse) throws SQLException {
        if (existeDoublon(reponse.getQuestion_id(), reponse.getContenu(), 0)) {
            throw new SQLException("Cette reponse existe deja pour cette question.");
        }

        ajouter(reponse);
    }

    public void modifierReponseQcm(Reponse reponse) throws SQLException {
        if (existeDoublon(reponse.getQuestion_id(), reponse.getContenu(), reponse.getId())) {
            throw new SQLException("Cette reponse existe deja pour cette question.");
        }

        modifier(reponse);
    }

    public boolean reponseExistePourQuestion(int questionId, String contenu, int idAExclure) throws SQLException {
        return existeDoublon(questionId, contenu, idAExclure);
    }

    public List<Reponse> afficherParQuestionId(int questionId) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        String req = "SELECT * FROM reponse WHERE question_id=?";
        PreparedStatement ps = getConnection().prepareStatement(req);
        ps.setInt(1, questionId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            reponses.add(mapperReponse(rs));
        }

        return reponses;
    }
}
