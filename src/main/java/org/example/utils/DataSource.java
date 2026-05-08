package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {

    private static DataSource instance;
    private Connection connection;

    private final String URL      = "jdbc:mysql://localhost:3306/esprit";
    private final String USER     = "root";
    private final String PASSWORD = "";

    private DataSource() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion réussie !");
        } catch (ClassNotFoundException e) {
            System.out.println(" Driver introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.out.println(" Erreur de connexion : " + e.getMessage());
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}