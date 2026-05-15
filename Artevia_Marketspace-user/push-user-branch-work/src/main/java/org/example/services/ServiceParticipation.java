package org.example.services;

import Utils.MyBD;
import org.example.entities.Participation;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ServiceParticipation {
    private final Connection connection;
    private final ServiceEvent serviceEvent;

    public ServiceParticipation() {
        this.connection = MyBD.getInstance().getConn();
        this.serviceEvent = new ServiceEvent();
        ensureSchema();
    }

    public void reserver(int eventId, int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Utilisateur non connecte.");
        }
        if (existeParticipation(eventId, userId)) {
            throw new IllegalArgumentException("Vous avez deja reserve cet evenement.");
        }
        if (serviceEvent.countAvailableSeats(eventId) <= 0) {
            throw new IllegalArgumentException("Evenement complet.");
        }

        String sql = "INSERT INTO participation (event_id, user_id, status) VALUES (?, ?, 'CONFIRMED')";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void annuler(int eventId, int userId) throws SQLException {
        String sql = "DELETE FROM participation WHERE event_id = ? AND user_id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = requireConnection().prepareStatement("DELETE FROM participation WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existeParticipation(int eventId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participation WHERE event_id = ? AND user_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Participation> afficher() throws SQLException {
        String sql = """
                SELECT p.*, e.titre AS event_title, COALESCE(u.username, CONCAT('User #', p.user_id)) AS username
                FROM participation p
                LEFT JOIN evenement e ON e.id_event = p.event_id
                LEFT JOIN users u ON u.id = p.user_id
                ORDER BY p.created_at DESC
                """;
        return query(sql, null);
    }

    public List<Participation> afficherParUtilisateur(int userId) throws SQLException {
        return query("""
                SELECT p.*, e.titre AS event_title, COALESCE(u.username, CONCAT('User #', p.user_id)) AS username
                FROM participation p
                LEFT JOIN evenement e ON e.id_event = p.event_id
                LEFT JOIN users u ON u.id = p.user_id
                WHERE p.user_id = ?
                ORDER BY p.created_at DESC
                """, List.of(userId));
    }

    private List<Participation> query(String sql, List<Object> params) throws SQLException {
        List<Participation> participations = new ArrayList<>();
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Participation participation = new Participation();
                    participation.setId(rs.getInt("id"));
                    participation.setEventId(rs.getInt("event_id"));
                    participation.setUserId(rs.getInt("user_id"));
                    participation.setStatus(rs.getString("status"));
                    Timestamp created = rs.getTimestamp("created_at");
                    participation.setCreatedAt(created == null ? null : created.toLocalDateTime());
                    participation.setEventTitle(rs.getString("event_title"));
                    participation.setUsername(rs.getString("username"));
                    participations.add(participation);
                }
            }
        }
        return participations;
    }

    private void ensureSchema() {
        if (connection == null) {
            System.err.println("[EVENT] Connexion artevia indisponible.");
            return;
        }
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS participation (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        event_id INT NOT NULL,
                        user_id INT NOT NULL,
                        status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_participation_event_user (event_id, user_id)
                    )
                    """);
            addColumnIfMissing("participation", "status", "VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED'");
            addColumnIfMissing("participation", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        } catch (SQLException e) {
            System.err.println("[EVENT] Verification schema participation ignoree : " + e.getMessage());
        }
    }

    private void addColumnIfMissing(String table, String column, String definition) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, table, column)) {
            if (!rs.next()) {
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                }
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Connexion artevia indisponible.");
        }
        return connection;
    }
}
