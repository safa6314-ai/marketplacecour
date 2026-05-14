package org.example;

import org.example.entities.MarketplaceAchat;
import org.example.entities.MarketplaceVente;
import org.example.services.MarketplaceServiceAchat;
import org.example.services.MarketplaceServiceVente;

import java.sql.Date;
import java.sql.SQLException;

public class MarketplaceApp {

    public static void main(String[] args) {

        MarketplaceServiceVente sv = new MarketplaceServiceVente();
        MarketplaceServiceAchat sa = new MarketplaceServiceAchat();

        try {
            MarketplaceVente v = new MarketplaceVente(
                    "Dragon Mystique",
                    "Illustration fantasy d'un dragon magique",
                    200.0,
                    "Fantasy",
                    "Ahmed"
            );
            sv.ajouter(v);

            MarketplaceVente vModif = new MarketplaceVente(
                    1,
                    "Dragon Légendaire",
                    "Illustration fantasy modifiée",
                    300.0,
                    "Fantasy Premium",
                    "Ahmed"
            );
            sv.modifier(vModif);

            MarketplaceAchat a = new MarketplaceAchat(
                    "Dragon Légendaire",
                    "Client1",
                    300.0,
                    new Date(System.currentTimeMillis())
            );
            sa.ajouter(a);

            MarketplaceAchat aModif = new MarketplaceAchat(
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
            for (MarketplaceVente vente : sv.afficherAll()) {
                System.out.println(vente);
            }

            System.out.println("===== LISTE DES ACHATS =====");
            for (MarketplaceAchat achat : sa.afficher()) {
                System.out.println(achat);
            }

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}