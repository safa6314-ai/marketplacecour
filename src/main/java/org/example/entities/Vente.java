package org.example.entities;
public class Vente {

    private int id;
    private String titre;
    private String description;
    private double prix;
    private String categorie;
    private String nomArtiste;

    public Vente() {
    }

    public Vente(String titre, String description, double prix, String categorie, String nomArtiste) {
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.categorie = categorie;
        this.nomArtiste = nomArtiste;
    }

    public Vente(int id, String titre, String description, double prix, String categorie, String nomArtiste) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.categorie = categorie;
        this.nomArtiste = nomArtiste;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getNomArtiste() {
        return nomArtiste;
    }

    public void setNomArtiste(String nomArtiste) {
        this.nomArtiste = nomArtiste;
    }

    @Override
    public String toString() {
        return "Vente{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", categorie='" + categorie + '\'' +
                ", nomArtiste='" + nomArtiste + '\'' +
                '}';
    }
}