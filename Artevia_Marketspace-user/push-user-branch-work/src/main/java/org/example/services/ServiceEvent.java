package org.example.services;

import Utils.MyBD;
import org.example.entities.Event;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvent implements IService<Event> {
    private final Connection connection;

    public ServiceEvent() {
        this.connection = MyBD.getInstance().getConn();
        ensureSchema();
    }

    @Override
    public void ajouter(Event event) throws SQLException {
        validate(event);
        Connection conn = requireConnection();
        String sql = """
                INSERT INTO evenement (titre, description, lieu, date_debut, date_fin, capacite, prix, type, statut, organizer_id, latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, event);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    event.setId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Event event) throws SQLException {
        validate(event);
        Connection conn = requireConnection();
        String sql = """
                UPDATE evenement
                SET titre = ?, description = ?, lieu = ?, date_debut = ?, date_fin = ?, capacite = ?, prix = ?,
                    type = ?, statut = ?, organizer_id = ?, latitude = ?, longitude = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id_event = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, event);
            ps.setInt(13, event.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try (PreparedStatement ps = requireConnection().prepareStatement("DELETE FROM evenement WHERE id_event = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Event> afficher() throws SQLException {
        return query(baseSelect() + " ORDER BY date_debut DESC", null);
    }

    public List<Event> rechercher(String keyword, String categorie) throws SQLException {
        StringBuilder sql = new StringBuilder(baseSelect() + " WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(titre) LIKE ? OR LOWER(lieu) LIKE ? OR LOWER(description) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (categorie != null && !categorie.isBlank() && !"Tous".equalsIgnoreCase(categorie)) {
            sql.append(" AND type = ?");
            params.add(categorie);
        }

        sql.append(" ORDER BY date_debut DESC");
        return query(sql.toString(), params);
    }

    public Event findById(int id) throws SQLException {
        List<Object> params = List.of(id);
        List<Event> events = query(baseSelect() + " WHERE id_event = ?", params);
        return events.isEmpty() ? null : events.get(0);
    }

    public int countAvailableSeats(int eventId) throws SQLException {
        Event event = findById(eventId);
        if (event == null) {
            return 0;
        }
        int reserved = countParticipations(eventId);
        return Math.max(0, event.getCapacite() - reserved);
    }

    public int countParticipations(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participation WHERE event_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Event> query(String sql, List<Object> params) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        }
        return events;
    }

    private void fillStatement(PreparedStatement ps, Event event) throws SQLException {
        ps.setString(1, event.getTitre().trim());
        ps.setString(2, event.getDescription() == null ? "" : event.getDescription().trim());
        ps.setString(3, event.getLieu().trim());
        ps.setTimestamp(4, Timestamp.valueOf(event.getDateEvent()));
        ps.setTimestamp(5, Timestamp.valueOf(event.getDateEvent().plusHours(2)));
        ps.setInt(6, event.getCapacite());
        ps.setDouble(7, event.getPrix());
        ps.setString(8, event.getCategorie() == null || event.getCategorie().isBlank() ? "General" : event.getCategorie());
        ps.setString(9, "ACTIVE");
        ps.setInt(10, Math.max(0, event.getOrganizerId()));
        ps.setDouble(11, event.getLatitude());
        ps.setDouble(12, event.getLongitude());
    }

    private Event map(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("id"));
        event.setTitre(rs.getString("titre"));
        event.setDescription(rs.getString("description"));
        event.setLieu(rs.getString("lieu"));
        Timestamp date = rs.getTimestamp("date_event");
        event.setDateEvent(date == null ? null : date.toLocalDateTime());
        event.setCapacite(rs.getInt("capacite"));
        event.setPrix(rs.getDouble("prix"));
        event.setCategorie(rs.getString("categorie"));
        event.setOrganizerId(rs.getInt("organizer_id"));
        event.setLatitude(rs.getDouble("latitude"));
        event.setLongitude(rs.getDouble("longitude"));
        event.setCreatedAt(toDateTime(rs.getTimestamp("created_at")));
        event.setUpdatedAt(toDateTime(rs.getTimestamp("updated_at")));
        return event;
    }

    private LocalDateTime toDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String baseSelect() {
        return """
                SELECT
                    id_event AS id,
                    titre,
                    COALESCE(description, '') AS description,
                    lieu,
                    date_debut AS date_event,
                    capacite,
                    COALESCE(prix, 0) AS prix,
                    type AS categorie,
                    COALESCE(organizer_id, 0) AS organizer_id,
                    COALESCE(latitude, 0) AS latitude,
                    COALESCE(longitude, 0) AS longitude,
                    created_at,
                    updated_at
                FROM evenement
                """;
    }

    private void validate(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Evenement invalide.");
        }
        if (event.getTitre() == null || event.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'evenement est obligatoire.");
        }
        if (event.getLieu() == null || event.getLieu().trim().isEmpty()) {
            throw new IllegalArgumentException("Le lieu de l'evenement est obligatoire.");
        }
        if (event.getDateEvent() == null) {
            throw new IllegalArgumentException("La date de l'evenement est obligatoire.");
        }
        if (event.getCapacite() <= 0) {
            throw new IllegalArgumentException("La capacite doit etre superieure a 0.");
        }
        if (event.getPrix() < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas etre negatif.");
        }
    }

    private void ensureSchema() {
        if (connection == null) {
            System.err.println("[EVENT] Connexion artevia indisponible.");
            return;
        }
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS evenement (
                        id_event INT AUTO_INCREMENT PRIMARY KEY,
                        titre VARCHAR(180) NOT NULL,
                        date_debut DATETIME NOT NULL,
                        date_fin DATETIME NOT NULL,
                        lieu VARCHAR(220) NOT NULL,
                        capacite INT NOT NULL DEFAULT 1,
                        type VARCHAR(80) NOT NULL DEFAULT 'General',
                        statut VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                        description TEXT,
                        prix DECIMAL(10,2) NOT NULL DEFAULT 0,
                        organizer_id INT DEFAULT 0,
                        latitude DOUBLE DEFAULT 0,
                        longitude DOUBLE DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """);
            addColumnIfMissing("evenement", "description", "TEXT");
            addColumnIfMissing("evenement", "prix", "DECIMAL(10,2) NOT NULL DEFAULT 0");
            addColumnIfMissing("evenement", "organizer_id", "INT DEFAULT 0");
            addColumnIfMissing("evenement", "latitude", "DOUBLE DEFAULT 0");
            addColumnIfMissing("evenement", "longitude", "DOUBLE DEFAULT 0");
            addColumnIfMissing("evenement", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            addColumnIfMissing("evenement", "updated_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        } catch (SQLException e) {
            System.err.println("[EVENT] Verification schema event ignoree : " + e.getMessage());
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
