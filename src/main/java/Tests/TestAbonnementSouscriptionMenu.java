package Tests;

import Entities.Abonnement;
import Entities.Souscription;
import Services.AbonnementCRUD;
import Services.SouscriptionCRUD;

import java.sql.Date;
import java.util.Scanner;

public class TestAbonnementSouscriptionMenu {

    public static void main(String[] args) {
        AbonnementCRUD abonnementCRUD = new AbonnementCRUD();
        SouscriptionCRUD souscriptionCRUD = new SouscriptionCRUD();
        Scanner sc = new Scanner(System.in);

        boolean running = true;

        while (running) {
            afficherMenu();
            int choix = lireInt(sc, "Votre choix: ");

            try {
                switch (choix) {
                    case 1:
                        ajouterAbonnement(sc, abonnementCRUD);
                        break;
                    case 2:
                        modifierAbonnement(sc, abonnementCRUD);
                        break;
                    case 3:
                        supprimerAbonnement(sc, abonnementCRUD);
                        break;
                    case 4:
                        System.out.println(abonnementCRUD.afficher());
                        break;
                    case 5:
                        ajouterSouscription(sc, abonnementCRUD, souscriptionCRUD);
                        break;
                    case 6:
                        modifierSouscription(sc, abonnementCRUD, souscriptionCRUD);
                        break;
                    case 7:
                        supprimerSouscription(sc, souscriptionCRUD);
                        break;
                    case 8:
                        System.out.println(souscriptionCRUD.afficher());
                        break;
                    case 0:
                        running = false;
                        System.out.println("Au revoir !");
                        break;
                    default:
                        System.out.println("Choix invalide.");
                }
            } catch (Exception e) {
                System.out.println("Erreur: " + e.getMessage());
            }
        }

        sc.close();
    }

    private static void afficherMenu() {
        System.out.println("\n===== MENU CRUD =====");
        System.out.println("1. Ajouter Abonnement");
        System.out.println("2. Modifier Abonnement");
        System.out.println("3. Supprimer Abonnement");
        System.out.println("4. Afficher Abonnements");
        System.out.println("5. Ajouter Souscription");
        System.out.println("6. Modifier Souscription");
        System.out.println("7. Supprimer Souscription");
        System.out.println("8. Afficher Souscriptions");
        System.out.println("0. Quitter");
    }

    private static void ajouterAbonnement(Scanner sc, AbonnementCRUD abonnementCRUD) throws Exception {
        int idUser = lireInt(sc, "ID user: ");
        String nom = lireString(sc, "Nom abonnement: ");
        double prix = lireDouble(sc, "Prix: ");
        int duree = lireInt(sc, "Duree en mois: ");
        String description = lireString(sc, "Description: ");

        Abonnement a = new Abonnement(idUser, nom, prix, duree, description);
        abonnementCRUD.ajouter(a);
    }

    private static void modifierAbonnement(Scanner sc, AbonnementCRUD abonnementCRUD) throws Exception {
        int id = lireInt(sc, "ID abonnement a modifier: ");
        if (!abonnementExiste(abonnementCRUD, id)) {
            System.out.println("ID abonnement introuvable. Modification annulee.");
            return;
        }

        int idUser = lireInt(sc, "Nouveau ID user: ");
        String nom = lireString(sc, "Nouveau nom: ");
        double prix = lireDouble(sc, "Nouveau prix: ");
        int duree = lireInt(sc, "Nouvelle duree en mois: ");
        String description = lireString(sc, "Nouvelle description: ");

        Abonnement a = new Abonnement(id, idUser, nom, prix, duree, description);
        abonnementCRUD.modifier(a);
    }

    private static void supprimerAbonnement(Scanner sc, AbonnementCRUD abonnementCRUD) throws Exception {
        int id = lireInt(sc, "ID abonnement a supprimer: ");
        if (!abonnementExiste(abonnementCRUD, id)) {
            System.out.println("ID abonnement introuvable. Suppression annulee.");
            return;
        }

        abonnementCRUD.supprimer(id);
    }

    private static void ajouterSouscription(Scanner sc, AbonnementCRUD abonnementCRUD, SouscriptionCRUD souscriptionCRUD) throws Exception {
        int idUser = lireInt(sc, "ID user: ");
        String nomClient = lireString(sc, "Nom client: ");
        Date dateDebut = Date.valueOf(lireString(sc, "Date debut (yyyy-mm-dd): "));
        Date dateFin = Date.valueOf(lireString(sc, "Date fin (yyyy-mm-dd): "));
        String statut = lireString(sc, "Statut: ");
        int idAbonnement = lireInt(sc, "ID abonnement (FK): ");

        if (!abonnementExiste(abonnementCRUD, idAbonnement)) {
            System.out.println("ID abonnement (FK) introuvable. Ajout annule.");
            return;
        }

        Souscription s = new Souscription(idUser, nomClient, dateDebut, dateFin, statut, idAbonnement);
        souscriptionCRUD.ajouter(s);
    }

    private static void modifierSouscription(Scanner sc, AbonnementCRUD abonnementCRUD, SouscriptionCRUD souscriptionCRUD) throws Exception {
        int id = lireInt(sc, "ID souscription a modifier: ");
        if (!souscriptionExiste(souscriptionCRUD, id)) {
            System.out.println("ID souscription introuvable. Modification annulee.");
            return;
        }

        int idUser = lireInt(sc, "Nouveau ID user: ");
        String nomClient = lireString(sc, "Nouveau nom client: ");
        Date dateDebut = Date.valueOf(lireString(sc, "Nouvelle date debut (yyyy-mm-dd): "));
        Date dateFin = Date.valueOf(lireString(sc, "Nouvelle date fin (yyyy-mm-dd): "));
        String statut = lireString(sc, "Nouveau statut: ");
        int idAbonnement = lireInt(sc, "Nouveau ID abonnement (FK): ");

        if (!abonnementExiste(abonnementCRUD, idAbonnement)) {
            System.out.println("Nouveau ID abonnement (FK) introuvable. Modification annulee.");
            return;
        }

        Souscription s = new Souscription(id, idUser, nomClient, dateDebut, dateFin, statut, idAbonnement);
        souscriptionCRUD.modifier(s);
    }

    private static void supprimerSouscription(Scanner sc, SouscriptionCRUD souscriptionCRUD) throws Exception {
        int id = lireInt(sc, "ID souscription a supprimer: ");
        if (!souscriptionExiste(souscriptionCRUD, id)) {
            System.out.println("ID souscription introuvable. Suppression annulee.");
            return;
        }

        souscriptionCRUD.supprimer(id);
    }

    private static boolean abonnementExiste(AbonnementCRUD abonnementCRUD, int id) throws Exception {
        for (Abonnement a : abonnementCRUD.afficher()) {
            if (a.getIdAbonnement() == id) {
                return true;
            }
        }
        return false;
    }

    private static boolean souscriptionExiste(SouscriptionCRUD souscriptionCRUD, int id) throws Exception {
        for (Souscription s : souscriptionCRUD.afficher()) {
            if (s.getIdSouscription() == id) {
                return true;
            }
        }
        return false;
    }

    private static int lireInt(Scanner sc, String message) {
        System.out.print(message);
        while (!sc.hasNextInt()) {
            System.out.print("Entrer un entier valide: ");
            sc.next();
        }
        int value = sc.nextInt();
        sc.nextLine();
        return value;
    }

    private static double lireDouble(Scanner sc, String message) {
        System.out.print(message);
        while (!sc.hasNextDouble()) {
            System.out.print("Entrer un nombre valide: ");
            sc.next();
        }
        double value = sc.nextDouble();
        sc.nextLine();
        return value;
    }

    private static String lireString(Scanner sc, String message) {
        System.out.print(message);
        return sc.nextLine();
    }
}
