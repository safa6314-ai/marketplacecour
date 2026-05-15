package org.example.services;

import org.example.entities.Cours;
import org.example.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class AdvancedPedagogyService {

    private final Connection connection;
    private final CoursService coursService;

    public AdvancedPedagogyService() {
        this.connection = MyConnection.getInstance().getConnection();
        this.coursService = new CoursService();
    }

    public void inscrireEtudiant(int coursId, String emailEtudiant) {
        String sql = """
                INSERT INTO inscriptions (cours_id, email_etudiant, statut)
                VALUES (?, ?, 'INSCRIT')
                ON DUPLICATE KEY UPDATE statut='INSCRIT'
                """;

        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, coursId);
            ps.setString(2, emailEtudiant);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur inscription etudiant : " + e.getMessage(), e);
        }
    }

    public void enregistrerProgression(int chapitreId, String emailEtudiant, String statut) {
        String sql = """
                INSERT INTO progressions (chapitre_id, email_etudiant, statut)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE statut=VALUES(statut)
                """;

        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, chapitreId);
            ps.setString(2, emailEtudiant);
            ps.setString(3, statut);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur progression etudiant : " + e.getMessage(), e);
        }
    }

    public List<Cours> recommanderCours(Cours coursCourant) {
        return coursService.afficher().stream()
                .filter(cours -> cours.getId() != coursCourant.getId())
                .filter(cours -> sameValue(cours.getCategorie(), coursCourant.getCategorie())
                        || sameValue(cours.getNiveau(), coursCourant.getNiveau()))
                .limit(3)
                .toList();
    }

    public int recommanderChapitre(int chapitreId, String emailEtudiant) {
        ensureRecommendationTable();

        String sql = """
                INSERT IGNORE INTO recommandations_chapitres (chapitre_id, email_etudiant)
                VALUES (?, ?)
                """;

        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, chapitreId);
            ps.setString(2, emailEtudiant);
            ps.executeUpdate();
            return compterRecommandationsChapitre(chapitreId);
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur recommandation chapitre : " + e.getMessage(), e);
        }
    }

    public int compterRecommandationsChapitre(int chapitreId) {
        ensureRecommendationTable();

        String sql = "SELECT COUNT(*) FROM recommandations_chapitres WHERE chapitre_id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, chapitreId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur comptage recommandation chapitre : " + e.getMessage(), e);
        }
    }

    private void ensureRecommendationTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS recommandations_chapitres (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    chapitre_id INT NOT NULL,
                    email_etudiant VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_recommandation_chapitre_etudiant (chapitre_id, email_etudiant)
                )
                """;

        try (Statement statement = requireConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur creation table recommandations : " + e.getMessage(), e);
        }
    }

    private Connection requireConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalStateException("Connexion MySQL indisponible.");
            }
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Connexion MySQL indisponible : " + e.getMessage(), e);
        }
    }

    private static boolean sameValue(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
    }
}
