package org.example.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/artevia"
            + "?createDatabaseIfNotExist=true"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8"
            + "&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static MyConnection instance;
    private Connection connection;

    private MyConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            initializeSchema();
            System.out.println("Connexion a la base de donnees reussie.");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeSchema() throws SQLException {
        String createCoursTable = """
                CREATE TABLE IF NOT EXISTS cours (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    titre VARCHAR(255) NOT NULL,
                    description TEXT,
                    prix DOUBLE DEFAULT 0,
                    categorie VARCHAR(100),
                    niveau VARCHAR(50) DEFAULT 'Debutant'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        String createChapitresTable = """
                CREATE TABLE IF NOT EXISTS chapitres (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    titre VARCHAR(255) NOT NULL,
                    contenu TEXT,
                    pdf_path TEXT,
                    ordre INT,
                    cours_id INT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        String createAvisTable = """
                CREATE TABLE IF NOT EXISTS avis (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    chapitre_id INT NOT NULL,
                    commentaire TEXT,
                    note INT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        String createRendusTable = """
                CREATE TABLE IF NOT EXISTS rendus (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    chapitre_id INT NOT NULL,
                    nom_fichier VARCHAR(255),
                    chemin_fichier TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        String createInscriptionsTable = """
                CREATE TABLE IF NOT EXISTS inscriptions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    cours_id INT NOT NULL,
                    email_etudiant VARCHAR(255) NOT NULL,
                    statut VARCHAR(50) DEFAULT 'INSCRIT',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_inscription_cours_email (cours_id, email_etudiant)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        String createProgressionsTable = """
                CREATE TABLE IF NOT EXISTS progressions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    chapitre_id INT NOT NULL,
                    email_etudiant VARCHAR(255) NOT NULL,
                    statut VARCHAR(50) DEFAULT 'Not started',
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_progression_chapitre_email (chapitre_id, email_etudiant)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        try (var statement = connection.createStatement()) {
            statement.executeUpdate(createCoursTable);
            statement.executeUpdate(createChapitresTable);
            statement.executeUpdate(createAvisTable);
            statement.executeUpdate(createRendusTable);
            statement.executeUpdate(createInscriptionsTable);
            statement.executeUpdate(createProgressionsTable);
        }

        addColumnIfMissing("cours", "titre", "VARCHAR(255) NOT NULL DEFAULT ''");
        addColumnIfMissing("cours", "description", "TEXT");
        addColumnIfMissing("cours", "prix", "DOUBLE DEFAULT 0");
        addColumnIfMissing("cours", "categorie", "VARCHAR(100)");
        addColumnIfMissing("cours", "niveau", "VARCHAR(50) DEFAULT 'Debutant'");

        addColumnIfMissing("chapitres", "titre", "VARCHAR(255) NOT NULL DEFAULT ''");
        addColumnIfMissing("chapitres", "contenu", "TEXT");
        addColumnIfMissing("chapitres", "pdf_path", "TEXT");
        addColumnIfMissing("chapitres", "ordre", "INT");
        addColumnIfMissing("chapitres", "cours_id", "INT");

        addColumnIfMissing("avis", "chapitre_id", "INT NOT NULL");
        addColumnIfMissing("avis", "commentaire", "TEXT");
        addColumnIfMissing("avis", "note", "INT NOT NULL DEFAULT 1");
        addColumnIfMissing("avis", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

        addColumnIfMissing("rendus", "chapitre_id", "INT NOT NULL");
        addColumnIfMissing("rendus", "nom_fichier", "VARCHAR(255)");
        addColumnIfMissing("rendus", "chemin_fichier", "TEXT");
        addColumnIfMissing("rendus", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

        addColumnIfMissing("inscriptions", "cours_id", "INT NOT NULL");
        addColumnIfMissing("inscriptions", "email_etudiant", "VARCHAR(255) NOT NULL");
        addColumnIfMissing("inscriptions", "statut", "VARCHAR(50) DEFAULT 'INSCRIT'");
        addColumnIfMissing("inscriptions", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

        addColumnIfMissing("progressions", "chapitre_id", "INT NOT NULL");
        addColumnIfMissing("progressions", "email_etudiant", "VARCHAR(255) NOT NULL");
        addColumnIfMissing("progressions", "statut", "VARCHAR(50) DEFAULT 'Not started'");
        addColumnIfMissing("progressions", "updated_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) throws SQLException {
        if (columnExists(tableName, columnName)) {
            return;
        }

        try (var statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return columns.next();
        }
    }
}
