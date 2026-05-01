package Entities;

import java.sql.Date;

public class Souscription {

    private int idSouscription;
    private int idUser;
    private String nomClient;
    private Date dateDebut;
    private Date dateFin;
    private String statut;
    private int idAbonnement;

    public Souscription() {}

    public Souscription(int idUser, String nomClient, Date dateDebut, Date dateFin, String statut, int idAbonnement) {
        this.idUser = idUser;
        this.nomClient = nomClient;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.idAbonnement = idAbonnement;
    }

    public Souscription(int idSouscription, int idUser, String nomClient, Date dateDebut, Date dateFin, String statut, int idAbonnement) {
        this.idSouscription = idSouscription;
        this.idUser = idUser;
        this.nomClient = nomClient;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.idAbonnement = idAbonnement;
    }

    public int getIdSouscription() {
        return idSouscription;
    }

    public void setIdSouscription(int idSouscription) {
        this.idSouscription = idSouscription;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdAbonnement() {
        return idAbonnement;
    }

    public void setIdAbonnement(int idAbonnement) {
        this.idAbonnement = idAbonnement;
    }

    @Override
    public String toString() {
        return "Souscription{" +
                "idSouscription=" + idSouscription +
                ", idUser=" + idUser +
                ", nomClient='" + nomClient + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", statut='" + statut + '\'' +
                ", idAbonnement=" + idAbonnement +
                '}';
    }
}
