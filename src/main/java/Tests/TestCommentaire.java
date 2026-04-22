
package Tests;

import Services.CommentaireCRUD;
import Entities.Commentaire;

import java.sql.Timestamp;

public class TestCommentaire {

    public static void main(String[] args) {

        CommentaireCRUD cc = new CommentaireCRUD();

        try {
            // 🔹 CREATE (⚠️ post_id doit exister)
            Commentaire c = new Commentaire(
                    "Super post 😍",
                    new Timestamp(System.currentTimeMillis()),
                    2
            );
            cc.ajouter(c);

            // 🔹 READ
            System.out.println("Après ajout:");
            System.out.println(cc.afficher());

            // 🔹 UPDATE
            Commentaire c2 = new Commentaire(
                    1,
                    "Commentaire modifié 😎",
                    new Timestamp(System.currentTimeMillis()),
                    2
            );
            cc.modifier(c2);

            System.out.println("Après modification:");
            System.out.println(cc.afficher());

            // 🔹 DELETE
            cc.supprimer(1);

            System.out.println("Après suppression:");
            System.out.println(cc.afficher());

            // 🔥 NOUVELLE FONCTIONNALITÉ
            System.out.println("Commentaires du post 2:");
            System.out.println(cc.afficherParPost(2));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}