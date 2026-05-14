package Interfaces;

import java.sql.SQLException;
import java.util.List;

public interface InterfaceCRUD<T> {
    void ajouter(T var1) throws SQLException;

    void modifier(T var1) throws SQLException;

    void supprimer(int var1) throws SQLException;

    List<T> afficher() throws SQLException;


    default T getById(int id) throws SQLException {
        return null;
    }
}
