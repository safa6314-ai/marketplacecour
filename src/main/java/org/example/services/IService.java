package org.example.services;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void ajouter(T item) throws SQLException;

    void modifier(T item) throws SQLException;

    void supprimer(int id) throws SQLException;

    List<T> afficher() throws SQLException;
}
