//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {
    String url = "jdbc:mysql://localhost:3306/quiz_artevia";
    String user = "root";
    String password = "";
    private Connection cnx;
    private static MyBD instance;

    private MyBD() {
        try {
            this.cnx = DriverManager.getConnection(this.url, this.user, this.password);
            System.out.println("Connection établie !!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
    }
}
