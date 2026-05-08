package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MyDataBase {

    private static final String HOST_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
    private static final String URL = "jdbc:mysql://localhost:3306/marketplace_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static MyDataBase instance;
    private static String lastError;
    private Connection connection;

    private MyDataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initSchemaIfMissing();
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
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
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS marketplace_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            st.executeUpdate("USE marketplace_db");
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS vente (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "titre VARCHAR(255) NOT NULL, " +
                            "description TEXT, " +
                            "prix DOUBLE NOT NULL, " +
                            "categorie VARCHAR(100), " +
                            "nom_artiste VARCHAR(150))"
            );
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS achat (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "nom_oeuvre VARCHAR(255) NOT NULL, " +
                            "nom_acheteur VARCHAR(150) NOT NULL, " +
                            "prix DOUBLE NOT NULL, " +
                            "date_achat DATE NOT NULL)"
            );
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
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
