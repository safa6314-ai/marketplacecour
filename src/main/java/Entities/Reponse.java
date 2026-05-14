package Entities;

import java.time.LocalDateTime;

public class Reponse {

    private int id;
    private String contenu;
    private boolean correct;
    private int questionId;
    private LocalDateTime dateCreation;

    public Reponse() {
    }

    public Reponse(int id, String contenu, boolean correct, int questionId) {
        this.id = id;
        this.contenu = contenu;
        this.correct = correct;
        this.questionId = questionId;
    }

    public Reponse(String contenu, boolean correct, int questionId) {
        this.contenu = contenu;
        this.correct = correct;
        this.questionId = questionId;
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

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuestion_id() {
        return questionId;
    }

    public void setQuestion_id(int questionId) {
        this.questionId = questionId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return (correct ? "[Correct] " : "[Faux] ") + contenu;
    }
}
