package org.example.services;

import org.example.entities.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvent implements IService<Event> {

    private Connection cnx;

    public ServiceEvent() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Event event) throws SQLException {
        String req = "INSERT INTO evenement (titre, date_debut, date_fin, lieu, capacite, type, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getTitre());
            ps.setTimestamp(2, Timestamp.valueOf(event.getDate_debut()));
            ps.setTimestamp(3, Timestamp.valueOf(event.getDate_fin()));
            ps.setString(4, event.getLieu());
            ps.setInt(5, event.getCapacite());
            ps.setString(6, event.getType());
            ps.setString(7, event.getStatut());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) event.setId_event(keys.getInt(1));
            }
        }
    }

    @Override
    public void update(Event event) throws SQLException {
        String req = "UPDATE evenement SET titre=?, date_debut=?, date_fin=?, lieu=?, " +
                "capacite=?, type=?, statut=? WHERE id_event=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, event.getTitre());
            ps.setTimestamp(2, Timestamp.valueOf(event.getDate_debut()));
            ps.setTimestamp(3, Timestamp.valueOf(event.getDate_fin()));
            ps.setString(4, event.getLieu());
            ps.setInt(5, event.getCapacite());
            ps.setString(6, event.getType());
            ps.setString(7, event.getStatut());
            ps.setInt(8, event.getId_event());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Event event) throws SQLException {
        String req = "DELETE FROM evenement WHERE id_event = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, event.getId_event());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Event> getAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM evenement ORDER BY date_debut ASC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) events.add(mapRow(rs));
        }
        return events;
    }

    public Event getById(int id) throws SQLException {
        String req = "SELECT * FROM evenement WHERE id_event = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Event> getByStatut(String statut) throws SQLException {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM evenement WHERE statut = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(mapRow(rs));
            }
        }
        return events;
    }

    public List<Event> getByType(String type) throws SQLException {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM evenement WHERE type = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(mapRow(rs));
            }
        }
        return events;
    }

    public List<Event> searchByTitre(String motCle) throws SQLException {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM evenement WHERE titre LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, "%" + motCle + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(mapRow(rs));
            }
        }
        return events;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("id_event"),
                rs.getString("titre"),
                rs.getTimestamp("date_debut").toLocalDateTime(),
                rs.getTimestamp("date_fin").toLocalDateTime(),
                rs.getString("lieu"),
                rs.getInt("capacite"),
                rs.getString("type"),
                rs.getString("statut")
        );
    }
}