package org.example.services;

import org.example.entities.Participation;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceParticipation implements IService<Participation> {

    private Connection cnx;

    public ServiceParticipation() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Participation p) throws SQLException {
        if (estDejaInscrit(p.getId_event(), p.getId_personne())) {
            throw new IllegalStateException("Cette personne est déjà inscrite !");
        }
        String req = "INSERT INTO participation_evenement " +
                "(id_event, id_personne, date_inscription, statut_presence, code_billet) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getId_event());
            ps.setInt(2, p.getId_personne());
            ps.setTimestamp(3, Timestamp.valueOf(
                    p.getDate_inscription() != null ? p.getDate_inscription() : LocalDateTime.now()));
            ps.setString(4, p.getStatut_presence());
            ps.setString(5, p.getCode_billet());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId_participation(keys.getInt(1));
            }
        }
    }

    @Override
    public void update(Participation p) throws SQLException {
        String req = "UPDATE participation_evenement SET id_event=?, id_personne=?, " +
                "date_inscription=?, statut_presence=?, code_billet=? " +
                "WHERE id_participation=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, p.getId_event());
            ps.setInt(2, p.getId_personne());
            ps.setTimestamp(3, Timestamp.valueOf(p.getDate_inscription()));
            ps.setString(4, p.getStatut_presence());
            ps.setString(5, p.getCode_billet());
            ps.setInt(6, p.getId_participation());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Participation p) throws SQLException {
        String req = "DELETE FROM participation_evenement WHERE id_participation = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, p.getId_participation());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Participation> getAll() throws SQLException {
        List<Participation> list = new ArrayList<>();
        String req = "SELECT * FROM participation_evenement ORDER BY date_inscription DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Participation> getByEvent(int idEvent) throws SQLException {
        List<Participation> list = new ArrayList<>();
        String req = "SELECT * FROM participation_evenement WHERE id_event = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idEvent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Participation> getByPersonne(int idPersonne) throws SQLException {
        List<Participation> list = new ArrayList<>();
        String req = "SELECT * FROM participation_evenement WHERE id_personne = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idPersonne);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countInscrits(int idEvent) throws SQLException {
        String req = "SELECT COUNT(*) FROM participation_evenement WHERE id_event = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idEvent);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean estDejaInscrit(int idEvent, int idPersonne) throws SQLException {
        String req = "SELECT COUNT(*) FROM participation_evenement WHERE id_event=? AND id_personne=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idEvent);
            ps.setInt(2, idPersonne);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void updateStatut(int idParticipation, String nouveauStatut) throws SQLException {
        String req = "UPDATE participation_evenement SET statut_presence=? WHERE id_participation=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, idParticipation);
            ps.executeUpdate();
        }
    }

    private Participation mapRow(ResultSet rs) throws SQLException {
        return new Participation(
                rs.getInt("id_participation"),
                rs.getInt("id_event"),
                rs.getInt("id_personne"),
                rs.getTimestamp("date_inscription").toLocalDateTime(),
                rs.getString("statut_presence"),
                rs.getString("code_billet")
        );
    }
}