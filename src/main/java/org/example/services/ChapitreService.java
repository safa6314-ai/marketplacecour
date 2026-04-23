package org.example.services;

import org.example.entities.Chapitres;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChapitreService {

    private Connection connection;

    public ChapitreService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Chapitres chapitre) {
        String sql = "INSERT INTO chapitres (titre, contenu, ordre, cours_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chapitre.getTitre());
            ps.setString(2, chapitre.getContenu());
            ps.setInt(3, chapitre.getOrdre());
            ps.setInt(4, chapitre.getCoursId());
            ps.executeUpdate();
            System.out.println("Chapitre ajouté avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    public void modifier(Chapitres chapitre) {
        String sql = "UPDATE chapitres SET titre=?, contenu=?, ordre=?, cours_id=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chapitre.getTitre());
            ps.setString(2, chapitre.getContenu());
            ps.setInt(3, chapitre.getOrdre());
            ps.setInt(4, chapitre.getCoursId());
            ps.setInt(5, chapitre.getId());
            ps.executeUpdate();
            System.out.println("Chapitre modifié avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM chapitres WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Chapitre supprimé avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public List<Chapitres> afficher() {
        List<Chapitres> liste = new ArrayList<>();
        String sql = "SELECT * FROM chapitres ORDER BY ordre";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Chapitres ch = new Chapitres();
                ch.setId(rs.getInt("id"));
                ch.setTitre(rs.getString("titre"));
                ch.setContenu(rs.getString("contenu"));
                ch.setOrdre(rs.getInt("ordre"));
                ch.setCoursId(rs.getInt("cours_id"));
                liste.add(ch);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage : " + e.getMessage());
        }
        return liste;
    }

    public List<Chapitres> getByCoursId(int coursId) {
        List<Chapitres> liste = new ArrayList<>();
        String sql = "SELECT * FROM chapitres WHERE cours_id=? ORDER BY ordre";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Chapitres ch = new Chapitres();
                ch.setId(rs.getInt("id"));
                ch.setTitre(rs.getString("titre"));
                ch.setContenu(rs.getString("contenu"));
                ch.setOrdre(rs.getInt("ordre"));
                ch.setCoursId(rs.getInt("cours_id"));
                liste.add(ch);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par cours : " + e.getMessage());
        }
        return liste;
    }
}
