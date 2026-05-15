package org.example.services;

import org.example.entities.MarketplacePayment;
import org.example.utils.MarketplaceMyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarketplacePaymentService {

    private final Connection connection;

    public MarketplacePaymentService() {
        connection = MarketplaceMyDataBase.getInstance().getConnection();
        validateSchema();
    }

    public void saveMockPayment(String provider, String paymentRef, String customerId, double amount,
                                String currency, String status, String providerResponse) throws SQLException {
        String tableName = tableForProvider(provider);
        String sql = "INSERT INTO " + tableName +
                " (payment_ref, customer_id, amount, currency, status, provider_response) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, paymentRef);
            ps.setString(2, customerId);
            ps.setDouble(3, amount);
            ps.setString(4, currency == null || currency.isBlank() ? "TND" : currency);
            ps.setString(5, status == null || status.isBlank() ? "MOCK" : status);
            ps.setString(6, providerResponse);
            ps.executeUpdate();
        }
    }

    public List<MarketplacePayment> findPayments(String provider) throws SQLException {
        String tableName = tableForProvider(provider);
        String sql = "SELECT * FROM " + tableName + " ORDER BY created_at DESC, id DESC";
        List<MarketplacePayment> payments = new ArrayList<>();

        try (PreparedStatement ps = requireConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MarketplacePayment payment = new MarketplacePayment();
                payment.setId(rs.getInt("id"));
                payment.setPaymentRef(rs.getString("payment_ref"));
                payment.setCustomerId(rs.getString("customer_id"));
                payment.setAmount(rs.getDouble("amount"));
                payment.setCurrency(rs.getString("currency"));
                payment.setStatus(rs.getString("status"));
                payment.setProviderResponse(rs.getString("provider_response"));
                payment.setCreatedAt(rs.getTimestamp("created_at"));
                payments.add(payment);
            }
        }

        return payments;
    }

    private String tableForProvider(String provider) {
        String normalized = provider == null ? "" : provider.trim().toLowerCase();

        if ("flouci".equals(normalized)) {
            return "flouci_payments";
        }

        if ("konnect".equals(normalized)) {
            return "konnect_payments";
        }

        return "simulated_card_payments";
    }

    private void validateSchema() {
        if (connection == null) {
            return;
        }
        try {
            MarketplaceValidationService.validateDatabaseSchema(connection);
        } catch (SQLException e) {
            System.err.println("[MARKETPLACE DB] Validation paiement impossible : " + e.getMessage());
        }
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Connexion Marketplace indisponible.");
        }
        return connection;
    }
}
