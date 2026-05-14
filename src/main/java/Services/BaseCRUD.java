package Services;

import Utils.MyBD;

import java.sql.Connection;
import java.sql.SQLException;

abstract class BaseCRUD {

    private Connection cnx;

    protected BaseCRUD() {

        cnx = MyBD.getInstance().getConnection();
    }

    protected Connection getConnection() throws SQLException {

        if (cnx == null || cnx.isClosed()) {

            cnx = MyBD.getInstance().getConnection();
        }

        if (cnx == null) {

            throw new SQLException(
                    "Base de données non connectée. Vérifiez que MySQL est démarré et que la base quiz_artevia existe."
            );
        }

        return cnx;
    }
}