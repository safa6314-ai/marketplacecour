package Services;

import Entities.Souscription;
import Interfaces.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SouscriptionCRUD implements InterfaceCRUD<Souscription> {

    private final Connection conn;

    public SouscriptionCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Souscription s) throws SQLException {
        String req = "INSERT INTO souscription (id_user, nom_client, date_debut, date_fin, statut, id_abonnement) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, s.getIdUser());
                ps.setString(2, s.getNomClient());
                ps.setDate(3, s.getDateDebut());
                ps.setDate(4, s.getDateFin());
                ps.setString(5, s.getStatut());
                ps.setInt(6, s.getIdAbonnement());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        s.setIdSouscription(keys.getInt(1));
                    }
                }
            }
            System.out.println("Souscription ajoutee !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout souscription: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(Souscription s) throws SQLException {
        String req = "UPDATE souscription SET id_user=?, nom_client=?, date_debut=?, date_fin=?, statut=?, id_abonnement=? WHERE id_souscription=?";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, s.getIdUser());
                ps.setString(2, s.getNomClient());
                ps.setDate(3, s.getDateDebut());
                ps.setDate(4, s.getDateFin());
                ps.setString(5, s.getStatut());
                ps.setInt(6, s.getIdAbonnement());
                ps.setInt(7, s.getIdSouscription());
                ps.executeUpdate();
            }
            System.out.println("Souscription modifiee !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification souscription: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM souscription WHERE id_souscription=?";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            System.out.println("Souscription supprimee !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression souscription: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Souscription> afficher() throws SQLException {
        String req = "SELECT * FROM souscription ORDER BY id_souscription";
        List<Souscription> list = new ArrayList<>();

        if (conn == null) {
            System.out.println("No database connection. Returning empty list.");
            return list;
        }

        try {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(req)) {
                while (rs.next()) {
                    list.add(mapSouscription(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage souscription: " + e.getMessage());
            throw e;
        }

        return list;
    }

    @Override
    public Souscription getById(int id) throws SQLException {
        String req = "SELECT * FROM souscription WHERE id_souscription=?";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapSouscription(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur getById souscription: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public List<Souscription> afficherParUser(int idUser) throws SQLException {
        String req = "SELECT * FROM souscription WHERE id_user=? ORDER BY id_souscription";
        List<Souscription> list = new ArrayList<>();
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, idUser);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapSouscription(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur afficherParUser souscription: " + e.getMessage());
            throw e;
        }
        return list;
    }

    public void supprimerPourUser(int idSouscription, int idUser) throws SQLException {
        String req = "DELETE FROM souscription WHERE id_souscription=? AND id_user=?";
        try {
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, idSouscription);
                ps.setInt(2, idUser);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Erreur suppression souscription client: " + e.getMessage());
            throw e;
        }
    }

    private Souscription mapSouscription(ResultSet rs) throws SQLException {
        Souscription s = new Souscription();
        s.setIdSouscription(rs.getInt("id_souscription"));
        s.setIdUser(rs.getInt("id_user"));
        s.setNomClient(rs.getString("nom_client"));
        s.setDateDebut(rs.getDate("date_debut"));
        s.setDateFin(rs.getDate("date_fin"));
        s.setStatut(rs.getString("statut"));
        s.setIdAbonnement(rs.getInt("id_abonnement"));
        return s;
    }
}

