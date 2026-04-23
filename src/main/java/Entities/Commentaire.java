
package Entities;

import java.sql.Timestamp;

public class Commentaire {

    private int id;
    private String contenu;
    private Timestamp dateCreation;
    private int postId;

    public Commentaire() {}

    public Commentaire(String contenu, Timestamp dateCreation, int postId) {
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.postId = postId;
    }

    public Commentaire(int id, String contenu, Timestamp dateCreation, int postId) {
        this.id = id;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.postId = postId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateCreation=" + dateCreation +
                ", postId=" + postId +
                '}';
    }
}