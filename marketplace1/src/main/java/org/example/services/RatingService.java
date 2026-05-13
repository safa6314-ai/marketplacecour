package org.example.services;

import org.example.entities.Rating;
import org.example.utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RatingService {
    private final Connection connection;

    public RatingService() {
        connection = MyDataBase.getInstance().getConnection();
        ensureRatingTable();
    }

    public void ajouterOuModifier(int idVente, String customerId, int note) throws SQLException {
        if (note < 1 || note > 5) {
            throw new SQLException("La note doit etre comprise entre 1 et 5.");
        }
        Integer existingId = findExistingRatingId(idVente, customerId);
        if (existingId != null) {
            String updateSql = "UPDATE rating SET note = ?, date_rating = ? WHERE id_rating = ?";
            try (PreparedStatement ps = requireConnection().prepareStatement(updateSql)) {
                ps.setInt(1, note);
                ps.setDate(2, Date.valueOf(LocalDate.now()));
                ps.setInt(3, existingId);
                ps.executeUpdate();
            }
            return;
        }

        String insertSql = "INSERT INTO rating (id_vente, customer_id, note, date_rating) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(insertSql)) {
            ps.setInt(1, idVente);
            ps.setString(2, customerId);
            ps.setInt(3, note);
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
        }
    }

    private Integer findExistingRatingId(int idVente, String customerId) throws SQLException {
        String sql = "SELECT id_rating FROM rating WHERE id_vente = ? AND customer_id = ? LIMIT 1";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, idVente);
            ps.setString(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id_rating") : null;
            }
        }
    }

    public List<Rating> afficherNotes(int idVente) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM rating WHERE id_vente = ? ORDER BY date_rating DESC, id_rating DESC";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, idVente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rating rating = new Rating();
                    rating.setIdRating(rs.getInt("id_rating"));
                    rating.setIdVente(rs.getInt("id_vente"));
                    rating.setCustomerId(rs.getString("customer_id"));
                    rating.setNote(rs.getInt("note"));
                    rating.setDateRating(rs.getDate("date_rating"));
                    ratings.add(rating);
                }
            }
        }
        return ratings;
    }

    public double moyenne(int idVente) throws SQLException {
        String sql = "SELECT COALESCE(AVG(note), 0) AS moyenne FROM rating WHERE id_vente = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, idVente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("moyenne") : 0;
            }
        }
    }

    public int nombreNotes(int idVente) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM rating WHERE id_vente = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, idVente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public int noteUtilisateur(int idVente, String customerId) throws SQLException {
        String sql = "SELECT note FROM rating WHERE id_vente = ? AND customer_id = ? LIMIT 1";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, idVente);
            ps.setString(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("note") : 0;
            }
        }
    }

    public void supprimer(int idRating) throws SQLException {
        try (PreparedStatement ps = requireConnection().prepareStatement("DELETE FROM rating WHERE id_rating = ?")) {
            ps.setInt(1, idRating);
            ps.executeUpdate();
        }
    }

    private void ensureRatingTable() {
        if (connection == null) {
            return;
        }
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS rating (" +
                            "id_rating INT AUTO_INCREMENT PRIMARY KEY, " +
                            "id_vente INT NOT NULL, " +
                            "customer_id VARCHAR(100) NOT NULL, " +
                            "note INT NOT NULL, " +
                            "date_rating DATE NOT NULL, " +
                            "UNIQUE KEY uk_rating_user_vente (id_vente, customer_id))"
            );
            if (!columnExists("rating", "customer_id")) {
                st.executeUpdate("ALTER TABLE rating ADD COLUMN customer_id VARCHAR(100) NOT NULL DEFAULT 'Client test'");
            }
            if (!columnExists("rating", "date_rating")) {
                st.executeUpdate("ALTER TABLE rating ADD COLUMN date_rating DATE NOT NULL DEFAULT (CURRENT_DATE)");
            }
        } catch (SQLException ignored) {
            // Le message exact sera remonte lors de l'utilisation du service.
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

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            String details = MyDataBase.getLastError() == null ? "" : " Cause: " + MyDataBase.getLastError();
            throw new SQLException("Connexion MySQL indisponible." + details);
        }
        return connection;
    }
}
