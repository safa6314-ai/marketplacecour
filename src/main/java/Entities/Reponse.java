package Entities;

public class Reponse {

    private int id;
    private String contenu;
    private boolean isCorrect;
    private int question_id;

    // Constructeur vide
    public Reponse() {}

    // Constructeur avec paramètres
    public Reponse(int id, String contenu, boolean isCorrect, int question_id) {
        this.id = id;
        this.contenu = contenu;
        this.isCorrect = isCorrect;
        this.question_id = question_id;
    }

    public Reponse(String contenu, boolean isCorrect, int question_id) {
        this.contenu = contenu;
        this.isCorrect = isCorrect;
        this.question_id = question_id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public int getQuestion_id() { return question_id; }
    public void setQuestion_id(int question_id) { this.question_id = question_id; }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", isCorrect=" + isCorrect +
                ", question_id=" + question_id +
                '}';
    }
}