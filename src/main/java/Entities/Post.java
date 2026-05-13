package Entities;

import java.sql.Timestamp;

public class Post {

    private int id;
    private String contenu;
    private Timestamp dateCreation;
    private String imagePath;
    private String statut = "en_attente";


    public Post() {}


    public Post(String contenu, Timestamp dateCreation) {
        this.contenu = contenu;
        this.dateCreation = dateCreation;
    }

    public Post(String contenu, Timestamp dateCreation, String imagePath) {
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.imagePath = imagePath;
    }


    public Post(int id, String contenu, Timestamp dateCreation) {
        this.id = id;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
    }

    public Post(int id, String contenu, Timestamp dateCreation, String imagePath) {
        this.id = id;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.imagePath = imagePath;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getStatut() {
        return statut == null || statut.isBlank() ? "en_attente" : statut;
    }

    public void setStatut(String statut) {
        this.statut = statut == null || statut.isBlank() ? "en_attente" : statut;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateCreation=" + dateCreation +
                ", imagePath='" + imagePath + '\'' +
                ", statut='" + getStatut() + '\'' +
                '}';
    }
}
