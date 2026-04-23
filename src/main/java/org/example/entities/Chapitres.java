package org.example.entities;

public class Chapitres {

    private int id;
    private String titre;
    private String contenu;
    private int ordre;
    private int coursId;

    public Chapitres() {}

    public Chapitres(String titre, String contenu, int ordre, int coursId) {
        this.titre = titre;
        this.contenu = contenu;
        this.ordre = ordre;
        this.coursId = coursId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    @Override
    public String toString() {
        return "Chapitres{id=" + id + ", titre='" + titre + "', ordre=" + ordre + "}";
    }
}
