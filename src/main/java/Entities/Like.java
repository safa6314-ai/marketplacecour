package Entities;

import java.sql.Timestamp;

public class Like {

    private int id;
    private int postId;
    private Timestamp dateCreation;

    public Like() {}

    public Like(int postId, Timestamp dateCreation) {
        this.postId = postId;
        this.dateCreation = dateCreation;
    }

    public Like(int id, int postId, Timestamp dateCreation) {
        this.id = id;
        this.postId = postId;
        this.dateCreation = dateCreation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", postId=" + postId +
                ", dateCreation=" + dateCreation +
                '}';
    }
}