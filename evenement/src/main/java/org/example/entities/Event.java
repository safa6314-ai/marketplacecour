package org.example.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event {

    private int id_event;
    private String titre;
    private LocalDateTime date_debut;
    private LocalDateTime date_fin;
    private String lieu;
    private int capacite;
    private String type;
    private String statut;

    // Constructeur vide
    public Event() {}

    // Constructeur avec ID
    public Event(int id_event, String titre, LocalDateTime date_debut, LocalDateTime date_fin,
                 String lieu, int capacite, String type, String statut) {
        this.id_event = id_event;
        this.titre = titre;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.lieu = lieu;
        this.capacite = capacite;
        this.type = type;
        this.statut = statut;
    }

    // Constructeur sans ID (pour insertion)
    public Event(String titre, LocalDateTime date_debut, LocalDateTime date_fin,
                 String lieu, int capacite, String type, String statut) {
        this.titre = titre;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.lieu = lieu;
        this.capacite = capacite;
        this.type = type;
        this.statut = statut;
    }

    // Getters & Setters
    public int getId_event() { return id_event; }
    public void setId_event(int id_event) { this.id_event = id_event; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public LocalDateTime getDate_debut() { return date_debut; }
    public void setDate_debut(LocalDateTime date_debut) { this.date_debut = date_debut; }

    public LocalDateTime getDate_fin() { return date_fin; }
    public void setDate_fin(LocalDateTime date_fin) { this.date_fin = date_fin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    // Méthodes utilitaires
    public String getDateDebutFormatee() {
        return date_debut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getDateFinFormatee() {
        return date_fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public String toString() {
        return "Event{" +
                "id_event=" + id_event +
                ", titre='" + titre + '\'' +
                ", date_debut=" + date_debut +
                ", lieu='" + lieu + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}