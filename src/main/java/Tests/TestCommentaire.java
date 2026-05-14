package Tests;

import Entities.Commentaire;
import Services.CommentaireCRUD;

import java.sql.Timestamp;

public class TestCommentaire {

    public static void main(String[] args) {

        CommentaireCRUD cc = new CommentaireCRUD();

        try {

            Commentaire c = new Commentaire(
                    "Super post 😍",
                    new Timestamp(System.currentTimeMillis()),
                    2
            );
            cc.ajouter(c);


            System.out.println("Après ajout:");
            System.out.println(cc.afficher());


            Commentaire c2 = new Commentaire(
                    1,
                    "Commentaire modifié 😎",
                    new Timestamp(System.currentTimeMillis()),
                    2
            );
            cc.modifier(c2);

            System.out.println("Après modification:");
            System.out.println(cc.afficher());

            cc.supprimer(1);

            System.out.println("Après suppression:");
            System.out.println(cc.afficher());


            System.out.println("Commentaires du post 2:");
            System.out.println(cc.afficherParPost(2));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


















    }
}