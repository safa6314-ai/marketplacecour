package org.example.services;

import org.example.entities.Rendu;
import org.example.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RenduService {

    private final Connection connection;

    public RenduService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Rendu rendu) {
        if (hasRenduForChapitre(rendu.getChapitreId())) {
            throw new IllegalStateException("Un seul rendu est autorise pour ce chapitre.");
        }

        String sql = "INSERT INTO rendus (chapitre_id, nom_fichier, chemin_fichier) VALUES (?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, rendu.getChapitreId());
            ps.setString(2, rendu.getNomFichier());
            ps.setString(3, rendu.getCheminFichier());
            ps.executeUpdate();
            System.out.println("Rendu ajoute avec succes.");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de l'ajout du rendu : " + e.getMessage(), e);
        }
    }

    public boolean hasRenduForChapitre(int chapitreId) {
        return countByChapitre(chapitreId) > 0;
    }

    public int countByChapitre(int chapitreId) {
        String sql = "SELECT COUNT(*) FROM rendus WHERE chapitre_id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, chapitreId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors du comptage des rendus : " + e.getMessage(), e);
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
