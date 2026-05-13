package Services;

import Entities.Question;
import Interfaces.InterfaceCRUD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionCRUD extends BaseCRUD implements InterfaceCRUD<Question> {

    @Override
    public void ajouter(Question q) throws SQLException {
        String req = "INSERT INTO question (contenu, categorie, niveau) VALUES (?, ?, ?)";
        PreparedStatement ps = getConnection().prepareStatement(req);
        ps.setString(1, q.getContenu());
        ps.setString(2, q.getCategorie());
        ps.setString(3, q.getNiveau());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Question q) throws SQLException {
        String req = "UPDATE question SET contenu=?, categorie=?, niveau=? WHERE id=?";
        PreparedStatement ps = getConnection().prepareStatement(req);
        ps.setString(1, q.getContenu());
        ps.setString(2, q.getCategorie());
        ps.setString(3, q.getNiveau());
        ps.setInt(4, q.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String reqReponses = "DELETE FROM reponse WHERE question_id=?";
        PreparedStatement psReponses = getConnection().prepareStatement(reqReponses);
        psReponses.setInt(1, id);
        psReponses.executeUpdate();

        String req = "DELETE FROM question WHERE id=?";
        PreparedStatement ps = getConnection().prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Question> afficher() throws SQLException {
        List<Question> list = new ArrayList<>();
        String req = "SELECT * FROM question";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Question q = new Question();
            q.setId(rs.getInt("id"));
            q.setContenu(rs.getString("contenu"));
            q.setCategorie(rs.getString("categorie"));
            q.setNiveau(rs.getString("niveau"));
            list.add(q);
        }

        return list;
    }

}
