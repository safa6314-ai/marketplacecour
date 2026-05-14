package org.example.services;

import org.example.entities.MarketplaceVente;
import org.example.utils.MarketplaceMyDataBase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceServiceVente {

    private final Connection connection;

    public MarketplaceServiceVente() {
        connection = MarketplaceMyDataBase.getInstance().getConnection();
        ensureVenteColumns();
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            String details = MarketplaceMyDataBase.getLastError() == null ? "" : " Cause: " + MarketplaceMyDataBase.getLastError();
            throw new SQLException("Connexion MySQL indisponible. Verifiez XAMPP et la dependance mysql-connector-j." + details);
        }
        return connection;
    }

    private void ensureVenteColumns() {
        if (connection == null) {
            return;
        }
        try {
            if (!columnExists("vente", "quantite")) {
                Statement st = connection.createStatement();
                st.executeUpdate("ALTER TABLE vente ADD COLUMN quantite INT NOT NULL DEFAULT 1");
            }
            if (!columnExists("vente", "image_path")) {
                Statement st = connection.createStatement();
                st.executeUpdate("ALTER TABLE vente ADD COLUMN image_path VARCHAR(500) NULL");
            }
        } catch (SQLException ignored) {
            // Le CRUD affichera l'erreur SQL exacte si la colonne reste indisponible.
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                return true;
            }
        }
        try (ResultSet columns = metaData.getColumns(null, null, tableName.toUpperCase(), columnName)) {
            return columns.next();
        }
    }

    public void ajouter(MarketplaceVente v) throws SQLException {
        String req = "INSERT INTO vente (titre, description, prix, categorie, nom_artiste, quantite, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, v.getTitre());
        ps.setString(2, v.getDescription());
        ps.setDouble(3, v.getPrix());
        ps.setString(4, v.getCategorie());
        ps.setString(5, v.getNomArtiste());
        ps.setInt(6, v.getQuantite());
        ps.setString(7, v.getImagePath());
        ps.executeUpdate();
    }

    public void ajouterOeuvresDemoSiAbsentes() throws SQLException {
        MarketplaceVente[] oeuvres = {
                new MarketplaceVente(
                        "Medina de Tunis",
                        "Peinture lumineuse inspiree des ruelles anciennes et des portes bleues de la Medina.",
                        420.0,
                        "Peinture",
                        "Nour Ben Salem",
                        4,
                        null
                ),
                new MarketplaceVente(
                        "Sidi Bou Said Bleu",
                        "Illustration decorative aux tons bleus et blancs, ideale pour une collection moderne.",
                        280.0,
                        "Illustration",
                        "Yassine Trabelsi",
                        6,
                        null
                ),
                new MarketplaceVente(
                        "Desert au Coucher",
                        "Photographie artistique du desert tunisien avec lumiere chaude et contraste doux.",
                        350.0,
                        "Photo",
                        "Maya Gharbi",
                        3,
                        null
                ),
                new MarketplaceVente(
                        "Calligraphie Jasmin",
                        "Oeuvre calligraphique contemporaine avec details floraux et composition elegante.",
                        190.0,
                        "Calligraphie",
                        "Ali Mansour",
                        5,
                        null
                ),
                new MarketplaceVente(
                        "Mosaic Heritage",
                        "Composition inspiree des mosaiques antiques, avec formes geometriques et couleurs riches.",
                        510.0,
                        "Mosaique",
                        "Rim Haddad",
                        2,
                        null
                ),
                new MarketplaceVente(
                        "Ceramique Artisanale",
                        "Piece decorative artisanale aux motifs traditionnels, vendue en serie limitee.",
                        160.0,
                        "Ceramique",
                        "Sarra Mejri",
                        0,
                        null
                )
        };

        for (MarketplaceVente oeuvre : oeuvres) {
            if (!existeParTitre(oeuvre.getTitre())) {
                ajouter(oeuvre);
            }
        }
    }

    private boolean existeParTitre(String titre) throws SQLException {
        String req = "SELECT id FROM vente WHERE titre = ? LIMIT 1";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, titre);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public void modifier(MarketplaceVente v) throws SQLException {
        String req = "UPDATE vente SET titre = ?, description = ?, prix = ?, categorie = ?, nom_artiste = ?, quantite = ?, image_path = ? WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setString(1, v.getTitre());
        ps.setString(2, v.getDescription());
        ps.setDouble(3, v.getPrix());
        ps.setString(4, v.getCategorie());
        ps.setString(5, v.getNomArtiste());
        ps.setInt(6, v.getQuantite());
        ps.setString(7, v.getImagePath());
        ps.setInt(8, v.getId());
        ps.executeUpdate();
    }

    public void diminuerQuantite(int id) throws SQLException {
        String req = "UPDATE vente SET quantite = quantite - 1 WHERE id = ? AND quantite > 0";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setInt(1, id);
        if (ps.executeUpdate() == 0) {
            throw new SQLException("Article epuise.");
        }
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM vente WHERE id = ?";
        PreparedStatement ps = requireConnection().prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<MarketplaceVente> afficherAll() throws SQLException {
        List<MarketplaceVente> list = new ArrayList<>();
        String req = "SELECT * FROM vente";
        Statement st = requireConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            MarketplaceVente v = new MarketplaceVente();
            v.setId(rs.getInt("id"));
            v.setTitre(rs.getString("titre"));
            v.setDescription(rs.getString("description"));
            v.setPrix(rs.getDouble("prix"));
            v.setCategorie(rs.getString("categorie"));
            v.setNomArtiste(rs.getString("nom_artiste"));
            v.setQuantite(rs.getInt("quantite"));
            v.setImagePath(rs.getString("image_path"));
            list.add(v);
        }
        return list;
    }
}
