package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {

    private static final String URL = "jdbc:mysql://localhost:3306/artevia";
    private static final String User = "root";
    private static final String PASSWORD = "";

    private Connection conn;
    private static MyBD instance;

    private MyBD() {
        try {
            conn = DriverManager.getConnection(URL, User, PASSWORD);
            System.out.println("Connexion Ã©tablie !");
        } catch (SQLException e) {
            System.out.println("Erreur connexion BD : " + e.getMessage());
        }
    }

    public static MyBD getInstance() {
        if (instance == null) {
            instance = new MyBD();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }
}


