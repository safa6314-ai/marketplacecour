package org.example.entities;
import java.sql.Date;

public class Achat {

    private int id;
    private String nomOeuvre;
    private String nomAcheteur;
    private double prix;
    private Date dateAchat;

    public Achat() {
    }

    public Achat(String nomOeuvre, String nomAcheteur, double prix, Date dateAchat) {
        this.nomOeuvre = nomOeuvre;
        this.nomAcheteur = nomAcheteur;
        this.prix = prix;
        this.dateAchat = dateAchat;
    }

    public Achat(int id, String nomOeuvre, String nomAcheteur, double prix, Date dateAchat) {
        this.id = id;
        this.nomOeuvre = nomOeuvre;
        this.nomAcheteur = nomAcheteur;
        this.prix = prix;
        this.dateAchat = dateAchat;
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

    @Override
    public String toString() {
        return "Achat{" +
                "id=" + id +
                ", nomOeuvre='" + nomOeuvre + '\'' +
                ", nomAcheteur='" + nomAcheteur + '\'' +
                ", prix=" + prix +
                ", dateAchat=" + dateAchat +
                '}';
    }
}