package org.example.entities;
public class Vente {

    private int id;
    private String titre;
    private String description;
    private double prix;
    private String categorie;
    private String nomArtiste;
    private int quantite;
    private String imagePath;

    public Vente() {
    }

    public Vente(String titre, String description, double prix, String categorie, String nomArtiste) {
        this(titre, description, prix, categorie, nomArtiste, 1);
    }

    public Vente(String titre, String description, double prix, String categorie, String nomArtiste, int quantite) {
        this(titre, description, prix, categorie, nomArtiste, quantite, null);
    }

    public Vente(String titre, String description, double prix, String categorie, String nomArtiste, int quantite, String imagePath) {
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.categorie = categorie;
        this.nomArtiste = nomArtiste;
        this.quantite = quantite;
        this.imagePath = imagePath;
    }

    public Vente(int id, String titre, String description, double prix, String categorie, String nomArtiste) {
        this(id, titre, description, prix, categorie, nomArtiste, 1);
    }

    public Vente(int id, String titre, String description, double prix, String categorie, String nomArtiste, int quantite) {
        this(id, titre, description, prix, categorie, nomArtiste, quantite, null);
    }

    public Vente(int id, String titre, String description, double prix, String categorie, String nomArtiste, int quantite, String imagePath) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.categorie = categorie;
        this.nomArtiste = nomArtiste;
        this.quantite = quantite;
        this.imagePath = imagePath;
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

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
                ", quantite=" + quantite +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
