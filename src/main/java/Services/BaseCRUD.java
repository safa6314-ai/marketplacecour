package Services;

import Utils.MyBD;

import java.sql.Connection;
import java.sql.SQLException;

abstract class BaseCRUD {

    private Connection cnx;

    protected BaseCRUD() {
        cnx = MyBD.getInstance().getCnx();
    }

    protected Connection getConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyBD.getInstance().getCnx();
        }

        if (cnx == null) {
            throw new SQLException("Base de donnees non connectee. Verifiez que MySQL est demarre et que la base quiz_artevia existe.");
        }

        return cnx;
    }
}
