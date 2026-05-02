package org.example.services;

import org.example.entities.Vente;
import org.example.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceVente {

    private final Connection connection;

    public ServiceVente() {
        connection = MyDataBase.getInstance().getConnection();
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            String details = MyDataBase.getLastError() == null ? "" : " Cause: " + MyDataBase.getLastError();
            throw new SQLException("Connexion MySQL indisponible. Verifiez XAMPP et la dependance mysql-connector-j." + details);
        }
        return connection;
    }

    public void ajouter(Vente v) throws SQLException {
        String req = "INSERT INTO vente (titre, description, prix, categorie, nom_artiste) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, v.getTitre());
        ps.setString(2, v.getDescription());
        ps.setDouble(3, v.getPrix());
        ps.setString(4, v.getCategorie());
        ps.setString(5, v.getNomArtiste());
        ps.executeUpdate();
    }

    public void modifier(Vente v) throws SQLException {
        String req = "UPDATE vente SET titre = ?, description = ?, prix = ?, categorie = ?, nom_artiste = ? WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, v.getTitre());
        ps.setString(2, v.getDescription());
        ps.setDouble(3, v.getPrix());
        ps.setString(4, v.getCategorie());
        ps.setString(5, v.getNomArtiste());
        ps.setInt(6, v.getId());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM vente WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Vente> afficherAll() throws SQLException {
        List<Vente> list = new ArrayList<>();
        String req = "SELECT * FROM vente";
        Statement st = requireConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Vente v = new Vente();
            v.setId(rs.getInt("id"));
            v.setTitre(rs.getString("titre"));
            v.setDescription(rs.getString("description"));
            v.setPrix(rs.getDouble("prix"));
            v.setCategorie(rs.getString("categorie"));
            v.setNomArtiste(rs.getString("nom_artiste"));
            list.add(v);
        }
        return list;
    }
}
