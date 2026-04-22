package Tests;

import Services.PostCRUD;
import Entities.Post;

import java.sql.Timestamp;

public class TestPost {

    public static void main(String[] args) {

        PostCRUD pc = new PostCRUD();

        try {
            // CREATE
            Post p = new Post(
                    "Mon premier post 🔥",
                    new Timestamp(System.currentTimeMillis())
            );
            pc.ajouter(p);

            // READ
            System.out.println("Après ajout:");
            System.out.println(pc.afficher());

            // UPDATE (id = 1 ⚠️ doit exister)
            Post p2 = new Post(
                    1,
                    "Post modifié 😎",
                    new Timestamp(System.currentTimeMillis())
            );
            pc.modifier(p2);

            System.out.println("Après modification:");
            System.out.println(pc.afficher());

            // DELETE
            pc.supprimer(1);

            System.out.println("Après suppression:");
            System.out.println(pc.afficher());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}