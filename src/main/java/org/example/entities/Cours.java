package org.example.entities;

public class Cours {

    private int id;
    private String titre;
    private String description;
    private double prix;
    private String categorie;
    private String niveau;

    public Cours() {}

    public Cours(String titre, String description, double prix, String categorie) {
        this(titre, description, prix, categorie, "Debutant");
    }

    public Cours(String titre, String description, double prix, String categorie, String niveau) {
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.categorie = categorie;
        this.niveau = niveau;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    @Override
    public String toString() {
        return "Cours{id=" + id + ", titre='" + titre + "', niveau='" + niveau + "', prix=" + prix + "}";
    }
}
