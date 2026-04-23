package org.example;

import org.example.entities.Chapitres;
import org.example.entities.Cours;
import org.example.services.ChapitreService;
import org.example.services.CoursService;

import java.util.List;

public class App {

    public static void main(String[] args) {

        CoursService coursService = new CoursService();
        ChapitreService chapitreService = new ChapitreService();

        // --- Cours ---
        Cours c = new Cours("Java pour débutants", "Apprendre Java de zéro", 49.99, "Programmation");
        coursService.ajouter(c);

        List<Cours> tousCours = coursService.afficher();
        System.out.println("=== Liste des cours ===");
        for (Cours cours : tousCours) {
            System.out.println(cours);
        }

        // --- Chapitres ---
        Chapitres ch = new Chapitres("Introduction", "Bienvenue dans le cours !", 1, 1);
        chapitreService.ajouter(ch);

        List<Chapitres> chapitresDuCours = chapitreService.getByCoursId(1);
        System.out.println("=== Chapitres du cours 1 ===");
        for (Chapitres chapitre : chapitresDuCours) {
            System.out.println(chapitre);
        }
    }
}
