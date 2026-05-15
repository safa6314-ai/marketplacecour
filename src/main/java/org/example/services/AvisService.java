package org.example.services;

import org.example.entities.Avis;
import org.example.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AvisService {

    private final Connection connection;

    public AvisService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Avis avis) {
        String sql = "INSERT INTO avis (chapitre_id, commentaire, note) VALUES (?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, avis.getChapitreId());
            ps.setString(2, avis.getCommentaire());
            ps.setInt(3, avis.getNote());
            ps.executeUpdate();
            System.out.println("Avis ajoute avec succes.");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de l'ajout de l'avis : " + e.getMessage(), e);
        }
    }

    public int getLatestNoteForChapitre(int chapitreId) {
        String sql = "SELECT note FROM avis WHERE chapitre_id=? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, chapitreId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("note") : -1;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors du chargement de la note : " + e.getMessage(), e);
        }
    }

    public String getLatestCommentForChapitre(int chapitreId) {
        String sql = "SELECT commentaire FROM avis WHERE chapitre_id=? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, chapitreId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("commentaire") : "";
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors du chargement de la remarque : " + e.getMessage(), e);
        }
    }

    private Connection requireConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalStateException("Connexion MySQL indisponible. Verifie que MySQL est lance et que le mot de passe est correct.");
            }
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Connexion MySQL indisponible : " + e.getMessage(), e);
        }
    }
}
