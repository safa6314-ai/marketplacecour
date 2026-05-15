package org.example.utils;

import org.example.services.MarketplaceValidationService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MarketplaceMyDataBase {

    private static final String HOST_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
    private static final String DATABASE_NAME = "artevia";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME + "?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static MarketplaceMyDataBase instance;
    private static String lastError;
    private Connection connection;

    private MarketplaceMyDataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initSchemaIfMissing();
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            MarketplaceValidationService.validateDatabaseSchema(connection);
            lastError = null;
            System.out.println("Connexion reussie.");
        } catch (Exception e) {
            connection = null;
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            System.out.println("Erreur DB: " + e.getMessage());
        }
    }

    private void initSchemaIfMissing() throws Exception {
        try (Connection bootstrapConnection = DriverManager.getConnection(HOST_URL, USER, PASSWORD);
             Statement st = bootstrapConnection.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            st.executeUpdate("USE " + DATABASE_NAME);
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
                    "CREATE TABLE IF NOT EXISTS achat (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "nom_oeuvre VARCHAR(255) NOT NULL, " +
                            "nom_acheteur VARCHAR(150) NOT NULL, " +
                            "prix DOUBLE NOT NULL, " +
                            "date_achat DATE NOT NULL, " +
                            "statut VARCHAR(30) NOT NULL DEFAULT 'En attente')"
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
        }
    }

    public static MarketplaceMyDataBase getInstance() {
        if (instance == null) {
            instance = new MarketplaceMyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public static String getLastError() {
        return lastError;
    }
}
