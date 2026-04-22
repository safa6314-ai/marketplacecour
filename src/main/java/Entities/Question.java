package Entities;

public class Question {

    private int id;
    private String contenu;
    private String categorie;
    private String niveau;

    // Constructeur vide
    public Question() {}

    // Constructeur avec paramètres
    public Question(int id, String contenu, String categorie, String niveau) {
        this.id = id;
        this.contenu = contenu;
        this.categorie = categorie;
        this.niveau = niveau;
    }

    public Question(String contenu, String categorie, String niveau) {
        this.contenu = contenu;
        this.categorie = categorie;
        this.niveau = niveau;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", categorie='" + categorie + '\'' +
                ", niveau='" + niveau + '\'' +
                '}';
    }
}