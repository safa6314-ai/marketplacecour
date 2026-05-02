package org.example.services;

import org.example.entities.Achat;
import org.example.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceAchat {

    private final Connection connection;

    public ServiceAchat() {
        connection = MyDataBase.getInstance().getConnection();
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            String details = MyDataBase.getLastError() == null ? "" : " Cause: " + MyDataBase.getLastError();
            throw new SQLException("Connexion MySQL indisponible. Verifiez XAMPP et la dependance mysql-connector-j." + details);
        }
        return connection;
    }

    public void ajouter(Achat a) throws SQLException {
        String req = "INSERT INTO achat (nom_oeuvre, nom_acheteur, prix, date_achat) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, a.getNomOeuvre());
        ps.setString(2, a.getNomAcheteur());
        ps.setDouble(3, a.getPrix());
        ps.setDate(4, a.getDateAchat());
        ps.executeUpdate();
    }

    public void modifier(Achat a) throws SQLException {
        String req = "UPDATE achat SET nom_oeuvre = ?, nom_acheteur = ?, prix = ?, date_achat = ? WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, a.getNomOeuvre());
        ps.setString(2, a.getNomAcheteur());
        ps.setDouble(3, a.getPrix());
        ps.setDate(4, a.getDateAchat());
        ps.setInt(5, a.getId());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM achat WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Achat> afficher() throws SQLException {
        List<Achat> list = new ArrayList<>();
        String req = "SELECT * FROM achat";
        Statement st = requireConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Achat a = new Achat();
            a.setId(rs.getInt("id"));
            a.setNomOeuvre(rs.getString("nom_oeuvre"));
            a.setNomAcheteur(rs.getString("nom_acheteur"));
            a.setPrix(rs.getDouble("prix"));
            a.setDateAchat(rs.getDate("date_achat"));
            list.add(a);
        }
        return list;
    }
}
