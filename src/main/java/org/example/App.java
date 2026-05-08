package org.example;

import org.example.entities.Event;
import org.example.entities.Participation;
import org.example.services.ServiceEvent;
import org.example.services.ServiceParticipation;

import java.time.LocalDateTime;
import java.util.List;

public class App {
    public static void main(String[] args) {

        ServiceEvent serviceEvent = new ServiceEvent();
        ServiceParticipation serviceParticipation = new ServiceParticipation();

        try {
            // ADD événement
            Event e = new Event(
                    "Exposition Picasso",
                    LocalDateTime.of(2024, 6, 1, 10, 0),
                    LocalDateTime.of(2024, 6, 5, 18, 0),
                    "Tunis", 100, "exposition", "actif"
            );
            serviceEvent.add(e);
            System.out.println(" ADD OK : " + e);

            //GET ALL
            List<Event> events = serviceEvent.getAll();
            System.out.println("\n=== GET ALL ===");
            for (Event ev : events) System.out.println(ev);

            //UPDATE
            e.setTitre("Exposition Picasso - Édition Spéciale");
            e.setCapacite(150);
            serviceEvent.update(e);
            System.out.println("\n UPDATE OK : " + e);

            //ADD participation
            Participation p = new Participation(e.getId_event(), 1);
            serviceParticipation.add(p);
            System.out.println("\n ADD Participation OK : " + p);

            //COUNT inscrits
            int nb = serviceParticipation.countInscrits(e.getId_event());
            System.out.println(" Nombre d'inscrits : " + nb);
/*
            /DELETE participation
            serviceParticipation.delete(p);
            System.out.println("\n DELETE Participation OK !");

            //DELETE événement
            serviceEvent.delete(e);
            System.out.println(" DELETE Événement OK !");
*/
        } catch (Exception ex) {
            System.out.println(" Erreur : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}