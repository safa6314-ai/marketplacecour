package Entities;

public class Abonnement {

    private int idAbonnement;
    private int idUser;
    private String nom;
    private double prix;
    private int dureeMois;
    private String description;

    public Abonnement() {}

    public Abonnement(int idUser, String nom, double prix, int dureeMois, String description) {
        this.idUser = idUser;
        this.nom = nom;
        this.prix = prix;
        this.dureeMois = dureeMois;
        this.description = description;
    }

    public Abonnement(int idAbonnement, int idUser, String nom, double prix, int dureeMois, String description) {
        this.idAbonnement = idAbonnement;
        this.idUser = idUser;
        this.nom = nom;
        this.prix = prix;
        this.dureeMois = dureeMois;
        this.description = description;
    }

    public int getIdAbonnement() {
        return idAbonnement;
    }

    public void setIdAbonnement(int idAbonnement) {
        this.idAbonnement = idAbonnement;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getDureeMois() {
        return dureeMois;
    }

    public void setDureeMois(int dureeMois) {
        this.dureeMois = dureeMois;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Abonnement{" +
                "idAbonnement=" + idAbonnement +
                ", idUser=" + idUser +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", dureeMois=" + dureeMois +
                ", description='" + description + '\'' +
                '}';
    }
}
