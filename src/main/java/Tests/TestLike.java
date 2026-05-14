package Tests;

import Entities.Like;
import Services.LikeCRUD;

import java.sql.Timestamp;

public class TestLike {

    public static void main(String[] args) {

        LikeCRUD lc = new LikeCRUD();

        try {

            Like l = new Like(2, new Timestamp(System.currentTimeMillis()));
            lc.ajouter(l);


            System.out.println("Tous les likes:");
            System.out.println(lc.afficher());


            System.out.println("Nombre de likes du post 2:");
            System.out.println(lc.countLikesByPost(2));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
