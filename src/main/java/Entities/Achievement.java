package Entities;

import java.time.LocalDateTime;

public class Achievement {

    private int id;
    private String titre;
    private String description;
    private String badgeIcon;
    private LocalDateTime dateObtention;

    public Achievement() {
    }

    public Achievement(int id, String titre, String description, String badgeIcon) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.badgeIcon = badgeIcon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBadgeIcon() {
        return badgeIcon;
    }

    public void setBadgeIcon(String badgeIcon) {
        this.badgeIcon = badgeIcon;
    }

    public LocalDateTime getDateObtention() {
        return dateObtention;
    }

    public void setDateObtention(LocalDateTime dateObtention) {
        this.dateObtention = dateObtention;
    }
}
