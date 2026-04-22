package Tests;

import Entities.Reponse;
import Services.ReponseCRUD;

public class TestReponse {
    public static void main(String[] args) {

        ReponseCRUD rc = new ReponseCRUD();

        try {

            Reponse r1 = new Reponse("Paris", true, 1);
            rc.ajouter(r1);

            System.out.println(rc.afficher());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}