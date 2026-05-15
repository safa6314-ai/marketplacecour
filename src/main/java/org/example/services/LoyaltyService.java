package org.example.services;

import org.example.entities.LoyaltyTransaction;
import org.example.utils.MarketplaceMyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LoyaltyService {

    private final Connection connection;

    public LoyaltyService() {
        connection = MarketplaceMyDataBase.getInstance().getConnection();
        validateSchema();
    }

    public void addPoints(String customerId, int points, String type, String reason, String externalRef)
            throws SQLException {
        String sql = "INSERT INTO loyalty_transactions " +
                "(customer_id, points, type, reason, created_at, external_ref) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.setInt(2, points);
            ps.setString(3, type);
            ps.setString(4, reason);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.setString(6, externalRef);
            ps.executeUpdate();
        }
    }

    public int totalPoints(String customerId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(points), 0) AS total FROM loyalty_transactions WHERE customer_id = ?";

        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public List<LoyaltyTransaction> history(String customerId) throws SQLException {
        String sql = "SELECT * FROM loyalty_transactions WHERE customer_id = ? ORDER BY created_at DESC, id_transaction DESC";
        List<LoyaltyTransaction> history = new ArrayList<>();

        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoyaltyTransaction transaction = new LoyaltyTransaction();
                    transaction.setIdTransaction(rs.getInt("id_transaction"));
                    transaction.setCustomerId(rs.getString("customer_id"));
                    transaction.setPoints(rs.getInt("points"));
                    transaction.setType(rs.getString("type"));
                    transaction.setReason(rs.getString("reason"));
                    transaction.setCreatedAt(rs.getTimestamp("created_at"));
                    transaction.setExternalRef(rs.getString("external_ref"));
                    history.add(transaction);
                }
            }
        }

        return history;
    }

    private void validateSchema() {
        if (connection == null) {
            return;
        }
        try {
            MarketplaceValidationService.validateDatabaseSchema(connection);
        } catch (SQLException e) {
            System.err.println("[MARKETPLACE DB] Validation loyalty impossible : " + e.getMessage());
        }
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Connexion Marketplace indisponible.");
        }
        return connection;
    }
}
