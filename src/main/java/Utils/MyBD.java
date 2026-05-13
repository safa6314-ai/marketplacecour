package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {

    private final String url = "jdbc:mysql://localhost:3306/" + System.getProperty("db.name", "artevia");
    private final String user = "root";
    private final String password = "";

    private Connection conn;

    private static MyBD instance;

    private MyBD() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection etablie !!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
