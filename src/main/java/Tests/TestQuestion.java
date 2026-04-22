package Tests;

import Entities.Question;
import Services.QuestionCRUD;

public class TestQuestion {
    public static void main(String[] args) {

        QuestionCRUD qc = new QuestionCRUD();

        try {
            // Ajouter
            Question q1 = new Question("Quelle est la capitale de la France ?", "Géographie", "Facile");
            qc.ajouter(q1);

            // Afficher
            System.out.println(qc.afficher());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}