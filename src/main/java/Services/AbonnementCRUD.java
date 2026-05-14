package Services;

import Entities.Abonnement;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonnementCRUD implements InterfaceCRUD<Abonnement> {

    private final Connection conn;

    public AbonnementCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Abonnement a) throws SQLException {
        String req = "INSERT INTO abonnement (id_user, nom, prix, duree_mois, description) VALUES (?, ?, ?, ?, ?)";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, a.getIdUser());
                ps.setString(2, a.getNom());
                ps.setDouble(3, a.getPrix());
                ps.setInt(4, a.getDureeMois());
                ps.setString(5, a.getDescription());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        a.setIdAbonnement(keys.getInt(1));
                    }
                }
            }
            System.out.println("Abonnement ajoute !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout abonnement: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(Abonnement a) throws SQLException {
        String req = "UPDATE abonnement SET id_user=?, nom=?, prix=?, duree_mois=?, description=? WHERE id_abonnement=?";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, a.getIdUser());
                ps.setString(2, a.getNom());
                ps.setDouble(3, a.getPrix());
                ps.setInt(4, a.getDureeMois());
                ps.setString(5, a.getDescription());
                ps.setInt(6, a.getIdAbonnement());
                ps.executeUpdate();
            }
            System.out.println("Abonnement modifie !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification abonnement: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM abonnement WHERE id_abonnement=?";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            System.out.println("Abonnement supprime !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression abonnement: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Abonnement> afficher() throws SQLException {
        String req = "SELECT * FROM abonnement ORDER BY id_abonnement";
        List<Abonnement> list = new ArrayList<>();

        if (conn == null) {
            System.out.println("No database connection. Returning empty list.");
            return list;
        }

        try {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(req)) {
                while (rs.next()) {
                    list.add(mapAbonnement(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage abonnement: " + e.getMessage());
            throw e;
        }

        return list;
    }

    @Override
    public Abonnement getById(int id) throws SQLException {
        String req = "SELECT * FROM abonnement WHERE id_abonnement=?";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapAbonnement(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur getById abonnement: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public List<Abonnement> afficherParUser(int idUser) throws SQLException {
        String req = "SELECT * FROM abonnement WHERE id_user=? ORDER BY id_abonnement";
        List<Abonnement> list = new ArrayList<>();
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, idUser);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapAbonnement(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur afficherParUser abonnement: " + e.getMessage());
            throw e;
        }
        return list;
    }

    private Abonnement mapAbonnement(ResultSet rs) throws SQLException {
        Abonnement a = new Abonnement();
        a.setIdAbonnement(rs.getInt("id_abonnement"));
        a.setIdUser(rs.getInt("id_user"));
        a.setNom(rs.getString("nom"));
        a.setPrix(rs.getDouble("prix"));
        a.setDureeMois(rs.getInt("duree_mois"));
        a.setDescription(rs.getString("description"));
        return a;
    }
}

