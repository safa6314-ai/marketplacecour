package org.example.services;

import org.example.entities.Cours;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursService {

    private Connection connection;

    public CoursService() {
        this.connection = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Cours cours) {
        String sql = "INSERT INTO cours (titre, description, prix, categorie) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setDouble(3, cours.getPrix());
            ps.setString(4, cours.getCategorie());
            ps.executeUpdate();
            System.out.println("Cours ajouté avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    public void modifier(Cours cours) {
        String sql = "UPDATE cours SET titre=?, description=?, prix=?, categorie=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getDescription());
            ps.setDouble(3, cours.getPrix());
            ps.setString(4, cours.getCategorie());
            ps.setInt(5, cours.getId());
            ps.executeUpdate();
            System.out.println("Cours modifié avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM cours WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Cours supprimé avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public List<Cours> afficher() {
        List<Cours> liste = new ArrayList<>();
        String sql = "SELECT * FROM cours";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitre(rs.getString("titre"));
                c.setDescription(rs.getString("description"));
                c.setPrix(rs.getDouble("prix"));
                c.setCategorie(rs.getString("categorie"));
                liste.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage : " + e.getMessage());
        }
        return liste;
    }

    public Cours getById(int id) {
        String sql = "SELECT * FROM cours WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitre(rs.getString("titre"));
                c.setDescription(rs.getString("description"));
                c.setPrix(rs.getDouble("prix"));
                c.setCategorie(rs.getString("categorie"));
                return c;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
        }
        return null;
    }
}
