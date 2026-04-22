package Services;

import Entities.Reponse;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseCRUD implements InterfaceCRUD<Reponse> {

    Connection cnx;

    public ReponseCRUD() {
        cnx = MyBD.getInstance().getCnx();
    }

    @Override
    public void ajouter(Reponse r) throws SQLException {
        String req = "INSERT INTO reponse (contenu, isCorrect, question_id) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, r.getContenu());
        ps.setBoolean(2, r.isCorrect());
        ps.setInt(3, r.getQuestion_id());
        ps.executeUpdate();
        System.out.println("Réponse ajoutée !");
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
        System.out.println("Réponse modifiée !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM reponse WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Réponse supprimée !");
    }

    @Override
    public List<Reponse> afficher() throws SQLException {
        List<Reponse> list = new ArrayList<>();
        String req = "SELECT * FROM reponse";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Reponse r = new Reponse();
            r.setId(rs.getInt("id"));
            r.setContenu(rs.getString("contenu"));
            r.setCorrect(rs.getBoolean("isCorrect"));
            r.setQuestion_id(rs.getInt("question_id"));
            list.add(r);
        }

        return list;
    }
}