package Services;

import Entities.Achievement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AchievementService extends BaseCRUD {

    private final List<Achievement> definitions = List.of(
            new Achievement(1, "Premier Quiz", "Repondre a une premiere question.", "first"),
            new Achievement(2, "5 bonnes reponses", "Atteindre 5 bonnes reponses.", "five-correct"),
            new Achievement(3, "Expert Renaissance", "Reussir une question de categorie Renaissance.", "renaissance"),
            new Achievement(4, "Sans erreur", "Enchainer 3 bonnes reponses sans erreur.", "clean"),
            new Achievement(5, "Niveau difficile reussi", "Repondre correctement a une question difficile.", "hard"),
            new Achievement(6, "10 questions repondues", "Repondre a 10 questions.", "ten")
    );

    public AchievementService() {
        initialiserTables();
    }

    public List<Achievement> verifierSucces(QuizStats stats) {
        List<Achievement> debloques = new ArrayList<>();
        ajouterSiNouveau(debloques, 1, stats.getQuestionsRepondues() >= 1);
        ajouterSiNouveau(debloques, 2, stats.getBonnesReponses() >= 5);
        ajouterSiNouveau(debloques, 3, stats.isCategorieRenaissanceReussie());
        ajouterSiNouveau(debloques, 4, stats.getSerieBonnesReponses() >= 3 && stats.getErreurs() == 0);
        ajouterSiNouveau(debloques, 5, stats.isDifficileReussi());
        ajouterSiNouveau(debloques, 6, stats.getQuestionsRepondues() >= 10);
        return debloques;
    }

    public List<Achievement> afficherDebloques() {
        List<Achievement> achievements = new ArrayList<>();
        try {
            String req = """
                    SELECT a.id, a.titre, a.description, a.badge_icon, ua.date_obtention
                    FROM achievements a
                    INNER JOIN user_achievements ua ON ua.achievement_id = a.id
                    ORDER BY ua.date_obtention DESC
                    """;
            Statement st = getConnection().createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Achievement achievement = new Achievement(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("badge_icon")
                );
                Timestamp date = rs.getTimestamp("date_obtention");
                if (date != null) {
                    achievement.setDateObtention(date.toLocalDateTime());
                }
                achievements.add(achievement);
            }
        } catch (SQLException e) {
            return List.of();
        }

        return achievements;
    }

    private void ajouterSiNouveau(List<Achievement> debloques, int id, boolean condition) {
        if (!condition || estDebloque(id)) {
            return;
        }

        sauvegarderDeblocage(id);
        definitionParId(id).ifPresent(debloques::add);
    }

    private java.util.Optional<Achievement> definitionParId(int id) {
        return definitions.stream().filter(achievement -> achievement.getId() == id).findFirst();
    }

    private boolean estDebloque(int achievementId) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM user_achievements WHERE achievement_id=?");
            ps.setInt(1, achievementId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private void sauvegarderDeblocage(int achievementId) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO user_achievements (achievement_id) VALUES (?)");
            ps.setInt(1, achievementId);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void initialiserTables() {
        try {
            Statement st = getConnection().createStatement();
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        titre VARCHAR(100),
                        description TEXT,
                        badge_icon VARCHAR(255)
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user_achievements (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        achievement_id INT,
                        date_obtention TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            for (Achievement achievement : definitions) {
                PreparedStatement ps = getConnection().prepareStatement(
                        "INSERT IGNORE INTO achievements (id, titre, description, badge_icon) VALUES (?, ?, ?, ?)");
                ps.setInt(1, achievement.getId());
                ps.setString(2, achievement.getTitre());
                ps.setString(3, achievement.getDescription());
                ps.setString(4, achievement.getBadgeIcon());
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    public static class QuizStats {
        private int questionsRepondues;
        private int bonnesReponses;
        private int serieBonnesReponses;
        private int erreurs;
        private boolean categorieRenaissanceReussie;
        private boolean difficileReussi;

        public int getQuestionsRepondues() {
            return questionsRepondues;
        }

        public void incrementerQuestionsRepondues() {
            questionsRepondues++;
        }

        public int getBonnesReponses() {
            return bonnesReponses;
        }

        public void incrementerBonnesReponses() {
            bonnesReponses++;
            serieBonnesReponses++;
        }

        public int getSerieBonnesReponses() {
            return serieBonnesReponses;
        }

        public void reinitialiserSerie() {
            serieBonnesReponses = 0;
        }

        public int getErreurs() {
            return erreurs;
        }

        public void incrementerErreurs() {
            erreurs++;
            reinitialiserSerie();
        }

        public boolean isCategorieRenaissanceReussie() {
            return categorieRenaissanceReussie;
        }

        public void setCategorieRenaissanceReussie(boolean categorieRenaissanceReussie) {
            this.categorieRenaissanceReussie = categorieRenaissanceReussie;
        }

        public boolean isDifficileReussi() {
            return difficileReussi;
        }

        public void setDifficileReussi(boolean difficileReussi) {
            this.difficileReussi = difficileReussi;
        }
    }
}
