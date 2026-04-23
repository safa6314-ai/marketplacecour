package org.example.services;

import org.example.entities.Question;
import org.example.interfaces.InterfaceCRUD;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionCRUD implements InterfaceCRUD<Question> {

    Connection cnx = MyConnection.getInstance().getConnection();

    @Override
    public void ajouter(Question q) throws SQLException {
        String req = "INSERT INTO question (contenu, categorie, niveau) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, q.getContenu());
        ps.setString(2, q.getCategorie());
        ps.setString(3, q.getNiveau());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Question q) throws SQLException {
        String req = "UPDATE question SET contenu=?, categorie=?, niveau=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, q.getContenu());
        ps.setString(2, q.getCategorie());
        ps.setString(3, q.getNiveau());
        ps.setInt(4, q.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM question WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Question> afficher() throws SQLException {
        List<Question> list = new ArrayList<>();
        String req = "SELECT * FROM question";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Question q = new Question(
                    rs.getInt("id"),
                    rs.getString("contenu"),
                    rs.getString("categorie"),
                    rs.getString("niveau")
            );
            list.add(q);
        }
        return list;
    }
}