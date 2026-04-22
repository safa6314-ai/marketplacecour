package Entities;

import java.sql.Timestamp;

public class Post {

    private int id;
    private String contenu;
    private Timestamp dateCreation;

    // constructeur vide (OBLIGATOIRE pour ResultSet)
    public Post() {}

    // constructeur sans id
    public Post(String contenu, Timestamp dateCreation) {
        this.contenu = contenu;
        this.dateCreation = dateCreation;
    }

    // constructeur complet
    public Post(int id, String contenu, Timestamp dateCreation) {
        this.id = id;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
    }

    // getters / setters
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

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
