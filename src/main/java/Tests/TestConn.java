package Tests;

import Entites.User;
import Services.UserCRUD;
import Utils.MyBD;

import java.sql.SQLException;

public class TestConn {

    public static void main(String[] args) {

        MyBD bd = MyBD.getInstance();

        User u1 = new User("testuser1", "test1@gmail.com", "123456", "nourii", "Dupont", "ADMIN", "ACTIVE");
        User u2 = new User("testuser2", "test2@gmail.com", "123456", "Ali", "Ben Ali", "ARTIST", "ACTIVE");

        UserCRUD uc = new UserCRUD();

        try {

            // 🔹 INSERT (comme Personne)
            // uc.ajouter(u1);
            // uc.ajouter(u2);

            // 🔹 AFFICHAGE (exactement comme Personne)
           // System.out.println(uc.afficher());

            // 🔹 UPDATE (exemple simple)

            u1.setId(5); // ⚠️ mettre un ID existant
            u1.setUsername("updated_user");
            uc.modifier(u1);


            // 🔹 DELETE (exemple simple)
            /*
            uc.supprimer(1); // ⚠️ mettre un ID existant
            */

        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }
    }
}