package org.example;

import java.sql.Connection;

public class TestConnexion {
    public static void main(String[] args) {
        Connection cnx = DataSource.getInstance().getConnection();
        if (cnx != null) {
            System.out.println(" Connexion réussie à la base de données !");
        } else {
            System.out.println(" Connexion échouée !");
        }
    }
}