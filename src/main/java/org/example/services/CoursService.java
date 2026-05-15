package org.example.services;

import org.example.entities.Cours;
import org.example.utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CoursService {

    private final Connection connection;

    public CoursService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Cours cours) {
        String sql = "INSERT INTO cours (titre, description, prix, categorie, niveau) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setDouble(3, cours.getPrix());
            ps.setString(4, cours.getCategorie());
            ps.setString(5, cours.getNiveau());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    cours.setId(keys.getInt(1));
                }
            }
            System.out.println("Cours ajoute avec succes.");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de l'ajout du cours : " + e.getMessage(), e);
        }
    }

    public void modifier(Cours cours) {
        String sql = "UPDATE cours SET titre=?, description=?, prix=?, categorie=?, niveau=? WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setDouble(3, cours.getPrix());
            ps.setString(4, cours.getCategorie());
            ps.setString(5, cours.getNiveau());
            ps.setInt(6, cours.getId());
            ps.executeUpdate();
            System.out.println("Cours modifie avec succes.");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la modification du cours : " + e.getMessage(), e);
        }
    }

    public void supprimer(int id) {
        String deleteChaptersSql = "DELETE FROM chapitres WHERE cours_id=?";
        String deleteCourseSql = "DELETE FROM cours WHERE id=?";
        try {
            Connection activeConnection = requireConnection();
            try (PreparedStatement ps = activeConnection.prepareStatement(deleteChaptersSql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = activeConnection.prepareStatement(deleteCourseSql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            System.out.println("Cours supprime avec succes.");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la suppression du cours : " + e.getMessage(), e);
        }
    }

    public List<Cours> afficher() {
        List<Cours> liste = new ArrayList<>();
        String sql = "SELECT * FROM cours";
        try (Statement st = requireConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitre(rs.getString("titre"));
                c.setDescription(rs.getString("description"));
                c.setPrix(rs.getDouble("prix"));
                c.setCategorie(rs.getString("categorie"));
                c.setNiveau(rs.getString("niveau"));
                liste.add(c);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors du chargement des cours : " + e.getMessage(), e);
        }
        return liste;
    }

    public Cours getById(int id) {
        String sql = "SELECT * FROM cours WHERE id=?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cours c = new Cours();
                    c.setId(rs.getInt("id"));
                    c.setTitre(rs.getString("titre"));
                    c.setDescription(rs.getString("description"));
                    c.setPrix(rs.getDouble("prix"));
                    c.setCategorie(rs.getString("catègorie"));
                    c.setNiveau(rs.getString("niveau"));
                    return c;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la recherche du cours : " + e.getMessage(), e);
        }
        return null;
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
