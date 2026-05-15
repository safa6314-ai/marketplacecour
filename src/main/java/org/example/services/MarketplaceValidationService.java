package org.example.services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MarketplaceValidationService {

    private MarketplaceValidationService() {
    }

    public static void validateDatabaseSchema(Connection connection) throws SQLException {
        if (connection == null) {
            throw new SQLException("Connexion Marketplace indisponible. Impossible de valider le schema.");
        }

        createTablesIfMissing(connection);
        addMissingColumns(connection);
    }

    private static void createTablesIfMissing(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS achat (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "nom_oeuvre VARCHAR(255) NOT NULL, " +
                            "nom_acheteur VARCHAR(150) NOT NULL, " +
                            "prix DOUBLE NOT NULL, " +
                            "date_achat DATE NOT NULL, " +
                            "statut VARCHAR(30) NOT NULL DEFAULT 'En attente')"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS vente (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "titre VARCHAR(255) NOT NULL, " +
                            "description TEXT, " +
                            "prix DOUBLE NOT NULL, " +
                            "categorie VARCHAR(100), " +
                            "nom_artiste VARCHAR(150), " +
                            "id_achat INT NULL, " +
                            "quantite INT NOT NULL DEFAULT 1, " +
                            "image_path VARCHAR(500) DEFAULT NULL)"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS rating (" +
                            "id_rating INT AUTO_INCREMENT PRIMARY KEY, " +
                            "id_vente INT NOT NULL, " +
                            "customer_id VARCHAR(100) NOT NULL DEFAULT 'Client test', " +
                            "note INT NOT NULL, " +
                            "date_rating DATE NOT NULL, " +
                            "UNIQUE KEY uk_rating_user_vente (id_vente, customer_id))"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS loyalty_transactions (" +
                            "id_transaction INT AUTO_INCREMENT PRIMARY KEY, " +
                            "customer_id VARCHAR(100) NOT NULL, " +
                            "points INT NOT NULL, " +
                            "type VARCHAR(40) NOT NULL, " +
                            "reason VARCHAR(255), " +
                            "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "external_ref VARCHAR(120) UNIQUE)"
            );

            st.executeUpdate(paymentTableSql("simulated_card_payments"));
            st.executeUpdate(paymentTableSql("flouci_payments"));
            st.executeUpdate(paymentTableSql("konnect_payments"));
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS stripe_checkout_payments (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "payment_ref VARCHAR(120) NOT NULL UNIQUE, " +
                            "stripe_session_id VARCHAR(255) NOT NULL UNIQUE, " +
                            "customer_id VARCHAR(100) NOT NULL, " +
                            "amount DOUBLE NOT NULL, " +
                            "currency VARCHAR(10) NOT NULL DEFAULT 'usd', " +
                            "status VARCHAR(40) NOT NULL, " +
                            "checkout_url TEXT, " +
                            "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)"
            );
        }
    }

    private static String paymentTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "payment_ref VARCHAR(120) NOT NULL UNIQUE, " +
                "customer_id VARCHAR(100) NOT NULL, " +
                "amount DOUBLE NOT NULL, " +
                "currency VARCHAR(10) NOT NULL DEFAULT 'TND', " +
                "status VARCHAR(40) NOT NULL DEFAULT 'MOCK', " +
                "provider_response TEXT, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
    }

    private static void addMissingColumns(Connection connection) throws SQLException {
        addColumnIfMissing(connection, "vente", "titre", "VARCHAR(255) NOT NULL DEFAULT 'Sans titre'");
        addColumnIfMissing(connection, "vente", "description", "TEXT");
        addColumnIfMissing(connection, "vente", "prix", "DOUBLE NOT NULL DEFAULT 0");
        addColumnIfMissing(connection, "vente", "categorie", "VARCHAR(100)");
        addColumnIfMissing(connection, "vente", "nom_artiste", "VARCHAR(150)");
        addColumnIfMissing(connection, "vente", "id_achat", "INT NULL");
        addColumnIfMissing(connection, "vente", "quantite", "INT NOT NULL DEFAULT 1");
        addColumnIfMissing(connection, "vente", "image_path", "VARCHAR(500) DEFAULT NULL");

        addColumnIfMissing(connection, "achat", "nom_oeuvre", "VARCHAR(255) NOT NULL DEFAULT 'Oeuvre'");
        addColumnIfMissing(connection, "achat", "nom_acheteur", "VARCHAR(150) NOT NULL DEFAULT 'Client'");
        addColumnIfMissing(connection, "achat", "prix", "DOUBLE NOT NULL DEFAULT 0");
        addColumnIfMissing(connection, "achat", "date_achat", "DATE NULL");
        addColumnIfMissing(connection, "achat", "statut", "VARCHAR(30) NOT NULL DEFAULT 'En attente'");

        addColumnIfMissing(connection, "rating", "id_vente", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing(connection, "rating", "customer_id", "VARCHAR(100) NOT NULL DEFAULT 'Client test'");
        addColumnIfMissing(connection, "rating", "note", "INT NOT NULL DEFAULT 1");
        addColumnIfMissing(connection, "rating", "date_rating", "DATE NULL");
    }

    private static void addColumnIfMissing(Connection connection, String tableName, String columnName,
                                           String definition) throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            return;
        }

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
            System.out.println("[MARKETPLACE DB] Colonne ajoutee : " + tableName + "." + columnName);
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String databaseName = connection.getCatalog();

        try (ResultSet columns = metaData.getColumns(databaseName, null, tableName, columnName)) {
            if (columns.next()) {
                return true;
            }
        }

        try (ResultSet columns = metaData.getColumns(databaseName, null, tableName.toUpperCase(), columnName)) {
            return columns.next();
        }
    }
}
