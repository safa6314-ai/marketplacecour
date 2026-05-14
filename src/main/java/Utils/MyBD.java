package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {

    private final String url = "jdbc:mysql://localhost:3306/artevia";
    private final String user = "root";
    private final String password = "";

    private Connection cnx;

    private static MyBD instance;

    private MyBD() {

        try {

            cnx = DriverManager.getConnection(url, user, password);

            System.out.println("Connexion à la base de données réussie !");

        } catch (SQLException e) {

            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static MyBD getInstance() {

        if (instance == null) {
            instance = new MyBD();
        }

        return instance;
    }

    public Connection getConn() {
        return cnx;
    }

    public Connection getConnection() {
        return cnx;
    }
}