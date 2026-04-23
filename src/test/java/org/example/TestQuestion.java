package org.example;

import org.example.entities.Question;
import org.example.services.QuestionCRUD;

public class TestQuestion {
    public static void main(String[] args) {

        QuestionCRUD qc = new QuestionCRUD();

        try {
            Question q = new Question("Capitale de la France ?", "Geo", "Facile");
            qc.ajouter(q);

            System.out.println(qc.afficher());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}