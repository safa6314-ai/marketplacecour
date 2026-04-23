package org.example;

import org.example.entities.Reponse;
import org.example.services.ReponseCRUD;

public class TestReponse {
    public static void main(String[] args) {

        ReponseCRUD rc = new ReponseCRUD();

        try {
            Reponse r = new Reponse("Paris", true, 1);
            rc.ajouter(r);

            System.out.println(rc.afficher());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}