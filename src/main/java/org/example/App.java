package org.example;

import org.example.entities.Achat;
import org.example.entities.Vente;
import org.example.services.ServiceAchat;
import org.example.services.ServiceVente;

import java.sql.Date;
import java.sql.SQLException;

public class App {

    public static void main(String[] args) {

        ServiceVente sv = new ServiceVente();
        ServiceAchat sa = new ServiceAchat();

        try {
            Vente v = new Vente(
                    "Dragon Mystique",
                    "Illustration fantasy d'un dragon magique",
                    200.0,
                    "Fantasy",
                    "Ahmed"
            );
            sv.ajouter(v);

            Vente vModif = new Vente(
                    1,
                    "Dragon Légendaire",
                    "Illustration fantasy modifiée",
                    300.0,
                    "Fantasy Premium",
                    "Ahmed"
            );
            sv.modifier(vModif);

            Achat a = new Achat(
                    "Dragon Légendaire",
                    "Client1",
                    300.0,
                    new Date(System.currentTimeMillis())
            );
            sa.ajouter(a);

            Achat aModif = new Achat(
                    2,
                    "Dragon Légendaire",
                    "Client Modifié",
                    350.0,
                    new Date(System.currentTimeMillis())
            );
            sa.modifier(aModif);

            // DELETE si besoin :
            // sv.supprimer(2);
            // sa.supprimer(1);

            System.out.println("===== LISTE DES VENTES =====");
            for (Vente vente : sv.afficherAll()) {
                System.out.println(vente);
            }

            System.out.println("===== LISTE DES ACHATS =====");
            for (Achat achat : sa.afficher()) {
                System.out.println(achat);
            }

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}