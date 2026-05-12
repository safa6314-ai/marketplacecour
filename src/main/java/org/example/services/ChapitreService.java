package org.example.services;

import org.example.entities.Chapitres;
import org.example.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChapitreService {

    private final Connection connection;

    public ChapitreService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Chapitres chapitre) {
        String sql = "INSERT INTO chapitres (titre, contenu, ordre, cours_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, chapitre.getTitre());
            ps.setString(2, chapitre.getContenu());
            ps.setInt(3, chapitre.getOrdre());
            ps.setInt(4, chapitre.getCoursId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    chapitre.setId(keys.getInt(1));
                }
            }
            System.out.println("Chapitre ajoute avec succes.");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de l'ajout du chapitre : " + e.getMessage(), e);
        }
    }

    public void modifier(Chapitres chapitre) {
        String sql = "UPDATE chapitres SET titre=?, contenu=?, ordre=?, cours_id=? WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, chapitre.getTitre());
            ps.setString(2, chapitre.getContenu());
            ps.setInt(3, chapitre.getOrdre());
            ps.setInt(4, chapitre.getCoursId());
            ps.setInt(5, chapitre.getId());
            ps.executeUpdate();
            System.out.println("Chapitre modifie avec succes.");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la modification du chapitre : " + e.getMessage(), e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM chapitres WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Chapitre supprime avec succes.");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la suppression du chapitre : " + e.getMessage(), e);
        }
    }

    public List<Chapitres> afficher() {
        List<Chapitres> liste = new ArrayList<>();
        String sql = "SELECT * FROM chapitres ORDER BY ordre";
        try (Statement st = requireConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Chapitres ch = mapChapitre(rs);
                liste.add(ch);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors du chargement des chapitres  : " + e.getMessage(), e);
        }
        return liste;
    }

    public List<Chapitres> getByCoursId(int coursId) {
        List<Chapitres> liste = new ArrayList<>();
        String sql = "SELECT * FROM chapitres WHERE cours_id=? ORDER BY ordre";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chapitres ch = mapChapitre(rs);
                    liste.add(ch);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la recherche des chapitres : " + e.getMessage(), e);
        }
        return liste;
    }

    private static Chapitres mapChapitre(ResultSet rs) throws SQLException {
        Chapitres ch = new Chapitres();
        ch.setId(rs.getInt("id"));
        ch.setTitre(rs.getString("titre"));
        ch.setContenu(rs.getString("contenu"));
        ch.setOrdre(rs.getInt("ordre"));
        ch.setCoursId(rs.getInt("cours_id"));
        return ch;
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
