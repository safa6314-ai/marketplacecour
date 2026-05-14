package org.example.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Participation {

    private int id_participation;
    private int id_event;
    private int id_personne;
    private LocalDateTime date_inscription;
    private String statut_presence;
    private String code_billet;

    // Constructeur vide
    public Participation() {}

    // Constructeur avec ID
    public Participation(int id_participation, int id_event, int id_personne,
                         LocalDateTime date_inscription, String statut_presence, String code_billet) {
        this.id_participation = id_participation;
        this.id_event = id_event;
        this.id_personne = id_personne;
        this.date_inscription = date_inscription;
        this.statut_presence = statut_presence;
        this.code_billet = code_billet;
    }

    // Constructeur sans ID
    public Participation(int id_event, int id_personne, String statut_presence, String code_billet) {
        this.id_event = id_event;
        this.id_personne = id_personne;
        this.date_inscription = LocalDateTime.now();
        this.statut_presence = statut_presence;
        this.code_billet = code_billet;
    }

    // Constructeur simplifié
    public Participation(int id_event, int id_personne) {
        this.id_event = id_event;
        this.id_personne = id_personne;
        this.date_inscription = LocalDateTime.now();
        this.statut_presence = "inscrit";
        this.code_billet = null;
    }

    // Getters & Setters
    public int getId_participation() { return id_participation; }
    public void setId_participation(int id_participation) { this.id_participation = id_participation; }

    public int getId_event() { return id_event; }
    public void setId_event(int id_event) { this.id_event = id_event; }

    public int getId_personne() { return id_personne; }
    public void setId_personne(int id_personne) { this.id_personne = id_personne; }

    public LocalDateTime getDate_inscription() { return date_inscription; }
    public void setDate_inscription(LocalDateTime date_inscription) { this.date_inscription = date_inscription; }

    public String getStatut_presence() { return statut_presence; }
    public void setStatut_presence(String statut_presence) { this.statut_presence = statut_presence; }

    public String getCode_billet() { return code_billet; }
    public void setCode_billet(String code_billet) { this.code_billet = code_billet; }

    // Méthodes utilitaires
    public String getDateInscriptionFormatee() {
        return date_inscription.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean estPresent() { return "présent".equals(this.statut_presence); }
    public boolean estInscrit() { return "inscrit".equals(this.statut_presence); }
    public boolean estAbsent()  { return "absent".equals(this.statut_presence); }

    @Override
    public String toString() {
        return "Participation{" +
                "id_participation=" + id_participation +
                ", id_event=" + id_event +
                ", id_personne=" + id_personne +
                ", date_inscription=" + date_inscription +
                ", statut_presence='" + statut_presence + '\'' +
                ", code_billet='" + code_billet + '\'' +
                '}';
    }
}