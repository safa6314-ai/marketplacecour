package org.example.entities;

public class Avis {

    private int id;
    private int chapitreId;
    private String commentaire;
    private int note;

    public Avis() {}

    public Avis(int chapitreId, String commentaire, int note) {
        this.chapitreId = chapitreId;
        this.commentaire = commentaire;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChapitreId() { return chapitreId; }
    public void setChapitreId(int chapitreId) { this.chapitreId = chapitreId; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
}
