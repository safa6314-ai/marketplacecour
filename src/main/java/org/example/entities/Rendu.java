package org.example.entities;

public class Rendu {

    private int id;
    private int chapitreId;
    private String nomFichier;
    private String cheminFichier;

    public Rendu() {}

    public Rendu(int chapitreId, String nomFichier, String cheminFichier) {
        this.chapitreId = chapitreId;
        this.nomFichier = nomFichier;
        this.cheminFichier = cheminFichier;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChapitreId() { return chapitreId; }
    public void setChapitreId(int chapitreId) { this.chapitreId = chapitreId; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }
}
