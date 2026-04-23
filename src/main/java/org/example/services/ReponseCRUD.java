package org.example.services;

import org.example.entities.Reponse;
import org.example.interfaces.InterfaceCRUD;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseCRUD implements InterfaceCRUD<Reponse> {

    Connection cnx = MyConnection.getInstance().getConnection();

    @Override
    public void ajouter(Reponse r) throws SQLException {
        String req = "INSERT INTO reponse (contenu, isCorrect, question_id) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, r.getContenu());
        ps.setBoolean(2, r.isCorrect());
        ps.setInt(3, r.getQuestion_id());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Reponse r) throws SQLException {
        String req = "UPDATE reponse SET contenu=?, isCorrect=?, question_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, r.getContenu());
        ps.setBoolean(2, r.isCorrect());
        ps.setInt(3, r.getQuestion_id());
        ps.setInt(4, r.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM reponse WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Reponse> afficher() throws SQLException {
        List<Reponse> list = new ArrayList<>();
        String req = "SELECT * FROM reponse";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Reponse r = new Reponse(
                    rs.getInt("id"),
                    rs.getString("contenu"),
                    rs.getBoolean("isCorrect"),
                    rs.getInt("question_id")
            );
            list.add(r);
        }
        return list;
    }
}