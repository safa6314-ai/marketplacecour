package Entities;

import java.sql.Timestamp;

public class Post {
    public static final String CATEGORIE_ART = "Art";
    public static final String CATEGORIE_COURS = "Cours";
    public static final String CATEGORIE_EVENEMENT = "Evenement";
    public static final String CATEGORIE_MARCHE = "Marche";
    public static final String CATEGORIE_DISCUSSION_GENERALE = "Discussion generale";

    private int id;
    private String contenu;
    private Timestamp dateCreation;
    private String imagePath;
    private String statut = "en_attente";
    private String categorie = CATEGORIE_DISCUSSION_GENERALE;
    private String moderationReason = "En attente de moderation.";


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

    public String getCategorie() {
        return categorie == null || categorie.isBlank() ? CATEGORIE_DISCUSSION_GENERALE : categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = normaliserCategorie(categorie);
    }

    public String getModerationReason() {
        return moderationReason == null || moderationReason.isBlank() ? "Aucune raison indiquee." : moderationReason;
    }

    public void setModerationReason(String moderationReason) {
        this.moderationReason = moderationReason == null || moderationReason.isBlank()
                ? "Aucune raison indiquee."
                : moderationReason;
    }

    public static java.util.List<String> categoriesDisponibles() {
        return java.util.List.of(
                CATEGORIE_ART,
                CATEGORIE_COURS,
                CATEGORIE_EVENEMENT,
                CATEGORIE_MARCHE,
                CATEGORIE_DISCUSSION_GENERALE
        );
    }

    public static String normaliserCategorie(String categorie) {
        if (categorie == null || categorie.isBlank()) {
            return CATEGORIE_DISCUSSION_GENERALE;
        }
        String value = categorie.trim().toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a");
        return switch (value) {
            case "art" -> CATEGORIE_ART;
            case "cours" -> CATEGORIE_COURS;
            case "evenement", "event" -> CATEGORIE_EVENEMENT;
            case "marche", "market", "marketplace" -> CATEGORIE_MARCHE;
            default -> CATEGORIE_DISCUSSION_GENERALE;
        };
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateCreation=" + dateCreation +
                ", imagePath='" + imagePath + '\'' +
                ", statut='" + getStatut() + '\'' +
                ", categorie='" + getCategorie() + '\'' +
                ", moderationReason='" + getModerationReason() + '\'' +
                '}';
    }
}
