package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {
    String url = "jdbc:mysql://localhost:3306/quiz_artevia";
    String user = "root";
    String password = "";
    private Connection cnx;

    private final String url = "jdbc:mysql://localhost:3306/" + System.getProperty("db.name", "artevia");
    private final String user = "root";
    private final String password = "";

    private Connection conn;


    private static MyBD instance;

    private MyBD() {
        try {

            this.cnx = DriverManager.getConnection(this.url, this.user, this.password);
        } catch (SQLException e) {
            this.cnx = null;
        }

    }

    public static MyBD getInstance() {
        return instance == null ? (instance = new MyBD()) : instance;
    }

    public Connection getCnx() {
        return this.cnx;
    }

    public void setCnx(Connection cnx) {
        this.cnx = cnx;
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
