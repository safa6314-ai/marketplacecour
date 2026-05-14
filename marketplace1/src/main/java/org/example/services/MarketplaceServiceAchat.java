package org.example.services;

import org.example.entities.MarketplaceAchat;
import org.example.utils.MarketplaceMyDataBase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceServiceAchat {

    private final Connection connection;

    public MarketplaceServiceAchat() {
        connection = MarketplaceMyDataBase.getInstance().getConnection();
        ensureStatutColumn();
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            String details = MarketplaceMyDataBase.getLastError() == null ? "" : " Cause: " + MarketplaceMyDataBase.getLastError();
            throw new SQLException("Connexion MySQL indisponible. Verifiez XAMPP et la dependance mysql-connector-j." + details);
        }
        return connection;
    }

    private void ensureStatutColumn() {
        if (connection == null) {
            return;
        }
        try {
            if (!columnExists("achat", "statut")) {
                Statement st = connection.createStatement();
                st.executeUpdate("ALTER TABLE achat ADD COLUMN statut VARCHAR(30) NOT NULL DEFAULT 'En attente'");
            }
        } catch (SQLException ignored) {
            // Le CRUD affichera l'erreur SQL exacte si la colonne reste indisponible.
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                return true;
            }
        }
        try (ResultSet columns = metaData.getColumns(null, null, tableName.toUpperCase(), columnName)) {
            return columns.next();
        }
    }

    public void ajouter(MarketplaceAchat a) throws SQLException {
        String req = "INSERT INTO achat (nom_oeuvre, nom_acheteur, prix, date_achat, statut) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, a.getNomOeuvre());
        ps.setString(2, a.getNomAcheteur());
        ps.setDouble(3, a.getPrix());
        ps.setDate(4, a.getDateAchat());
        ps.setString(5, a.getStatut());
        ps.executeUpdate();
    }

    public void modifier(MarketplaceAchat a) throws SQLException {
        String req = "UPDATE achat SET nom_oeuvre = ?, nom_acheteur = ?, prix = ?, date_achat = ?, statut = ? WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, a.getNomOeuvre());
        ps.setString(2, a.getNomAcheteur());
        ps.setDouble(3, a.getPrix());
        ps.setDate(4, a.getDateAchat());
        ps.setString(5, a.getStatut());
        ps.setInt(6, a.getId());
        ps.executeUpdate();
    }

    public void confirmer(int id) throws SQLException {
        changerStatut(id, "Confirme");
    }

    public void refuser(int id) throws SQLException {
        changerStatut(id, "Refuse");
    }

    private void changerStatut(int id, String statut) throws SQLException {
        String req = "UPDATE achat SET statut = ? WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, statut);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM achat WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<MarketplaceAchat> afficher() throws SQLException {
        List<MarketplaceAchat> list = new ArrayList<>();
        String req = "SELECT * FROM achat";
        Statement st = requireConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            MarketplaceAchat a = new MarketplaceAchat();
            a.setId(rs.getInt("id"));
            a.setNomOeuvre(rs.getString("nom_oeuvre"));
            a.setNomAcheteur(rs.getString("nom_acheteur"));
            a.setPrix(rs.getDouble("prix"));
            a.setDateAchat(rs.getDate("date_achat"));
            a.setStatut(rs.getString("statut"));
            list.add(a);
        }
        return list;
    }
}
