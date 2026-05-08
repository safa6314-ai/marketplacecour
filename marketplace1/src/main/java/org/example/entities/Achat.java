package org.example.entities;
import java.sql.Date;

public class Achat {

    private int id;
    private String nomOeuvre;
    private String nomAcheteur;
    private double prix;
    private Date dateAchat;
    private String statut;

    public Achat() {
    }

    public Achat(String nomOeuvre, String nomAcheteur, double prix, Date dateAchat) {
        this(nomOeuvre, nomAcheteur, prix, dateAchat, "En attente");
    }

    public Achat(String nomOeuvre, String nomAcheteur, double prix, Date dateAchat, String statut) {
        this.nomOeuvre = nomOeuvre;
        this.nomAcheteur = nomAcheteur;
        this.prix = prix;
        this.dateAchat = dateAchat;
        this.statut = statut;
    }

    public Achat(int id, String nomOeuvre, String nomAcheteur, double prix, Date dateAchat) {
        this(id, nomOeuvre, nomAcheteur, prix, dateAchat, "En attente");
    }

    public Achat(int id, String nomOeuvre, String nomAcheteur, double prix, Date dateAchat, String statut) {
        this.id = id;
        this.nomOeuvre = nomOeuvre;
        this.nomAcheteur = nomAcheteur;
        this.prix = prix;
        this.dateAchat = dateAchat;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getNomOeuvre() {
        return nomOeuvre;
    }

    public void setNomOeuvre(String nomOeuvre) {
        this.nomOeuvre = nomOeuvre;
    }

    public String getNomAcheteur() {
        return nomAcheteur;
    }

    public void setNomAcheteur(String nomAcheteur) {
        this.nomAcheteur = nomAcheteur;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public Date getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(Date dateAchat) {
        this.dateAchat = dateAchat;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Achat{" +
                "id=" + id +
                ", nomOeuvre='" + nomOeuvre + '\'' +
                ", nomAcheteur='" + nomAcheteur + '\'' +
                ", prix=" + prix +
                ", dateAchat=" + dateAchat +
                ", statut='" + statut + '\'' +
                '}';
    }
}
