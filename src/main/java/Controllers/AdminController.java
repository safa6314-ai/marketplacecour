package Controllers;

import Entities.Abonnement;
import Entities.Souscription;
import Services.AbonnementCRUD;
import Utils.CsvExportService;
import Services.SouscriptionCRUD;
import Utils.NotificationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class AdminController implements Initializable {
    private static final int ADMIN_ID = 1;
    private static final int CLIENT_ID = 2;

    // Sidebar nav
    @FXML private Button btnDashboardNav, btnAbonnementNav, btnSouscriptionNav;
    // Topbar
    @FXML private Label modeTitle;
    @FXML private HBox switchContainer, filterContainer, crudButtons;
    @FXML private Button btnAbonnement, btnSouscription;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    // Views
    @FXML private ScrollPane dashboardView;
    @FXML private HBox gridView;
    @FXML private GridPane grid;
    @FXML private VBox formPanel, abonnementForm, souscriptionForm;
    // Stat labels
    @FXML private Label statTotalPlans, statTotalSubs, statActiveSubs, statRevenue;
    // Charts
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private LineChart<String, Number> lineChart;
    // Form fields
    @FXML private TextField aboNomField, aboPrixField, aboDureeField;
    @FXML private TextArea aboDescField;
    @FXML private Label aboNomError, aboPrixError, aboDureeError, aboDescError;
    @FXML private TextField subClientField, subStatutField;
    @FXML private DatePicker subDateDebut, subDateFin;
    @FXML private ComboBox<Abonnement> subAboComboBox;
    @FXML private Label subClientError, subDateDebutError, subDateFinError, subStatutError, subAboIdError;

    private final AbonnementCRUD abonnementCRUD = new AbonnementCRUD();
    private final SouscriptionCRUD souscriptionCRUD = new SouscriptionCRUD();
    private boolean abonnementMode = true;
    private Integer editingAbonnementId = null;
    private Integer editingSouscriptionId = null;
    private int editingAbonnementUserId = ADMIN_ID;
    private int editingSouscriptionUserId = CLIENT_ID;

    // Master lists for streaming
    private List<Abonnement> allAbonnements = new ArrayList<>();
    private List<Souscription> allSouscriptions = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formPanel.setVisible(false);
        formPanel.setManaged(false);

        // ✅ Renommer le bouton Dashboard en Statistiques
        btnDashboardNav.setText("Statistiques");

        subAboComboBox.setConverter(new javafx.util.StringConverter<Abonnement>() {
            @Override public String toString(Abonnement a) { return a == null ? "" : a.getNom() + " - " + a.getPrix() + " DT"; }
            @Override public Abonnement fromString(String s) { return null; }
        });

        // Search listeners
        searchField.textProperty().addListener((obs, old, newValue) -> applyFilters());
        filterCombo.valueProperty().addListener((obs, old, newValue) -> applyFilters());

        // ✅ On démarre sur la vue Statistiques (anciennement Dashboard)
        showStatistiques(null);
    }

    // ==================== VIEW SWITCHING ====================

    private void setActiveNav(Button active) {
        btnDashboardNav.getStyleClass().setAll("admin-nav");
        btnAbonnementNav.getStyleClass().setAll("admin-nav");
        btnSouscriptionNav.getStyleClass().setAll("admin-nav");
        active.getStyleClass().setAll("admin-nav-active");
    }

    // ✅ Méthode renommée showStatistiques (appelée par btnDashboardNav en FXML)
    @FXML
    public void showStatistiques(ActionEvent event) {
        setActiveNav(btnDashboardNav);
        modeTitle.setText("Statistiques"); // ✅ Titre changé
        switchContainer.setVisible(false); switchContainer.setManaged(false);
        filterContainer.setVisible(false); filterContainer.setManaged(false);
        crudButtons.setVisible(false); crudButtons.setManaged(false);
        dashboardView.setVisible(true); dashboardView.setManaged(true);
        gridView.setVisible(false); gridView.setManaged(false);
        loadDashboardStats();
    }

    // ✅ Alias gardé pour compatibilité si utilisé ailleurs
    @FXML
    public void showDashboard(ActionEvent event) {
        showStatistiques(event);
    }

    @FXML
    void handleExportCsv(ActionEvent event) {
        List<Abonnement> abos;
        List<Souscription> subs;
        try {
            abos = abonnementCRUD.afficher();
            subs = souscriptionCRUD.afficher();
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationHelper.error("Export CSV", "Impossible de lire les donnees.");
            return;
        }

        Stage stage = (Stage) modeTitle.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter CSV — plans et souscriptions");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("artevia_admin_" + LocalDate.now() + ".csv");
        File file = fc.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        File out = file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")
                ? file
                : new File(file.getAbsolutePath() + ".csv");
        try {
            CsvExportService.exportAdminData(out, abos, subs);
            NotificationHelper.success("Export CSV", "Fichier enregistre :\n" + out.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            NotificationHelper.error("Export CSV", "Ecriture du fichier impossible.");
        }
    }

    @FXML
    public void showAbonnements(ActionEvent event) {
        setActiveNav(btnAbonnementNav);
        modeTitle.setText("Gestion des abonnements");
        switchContainer.setVisible(true); switchContainer.setManaged(true);
        filterContainer.setVisible(true); filterContainer.setManaged(true);
        crudButtons.setVisible(true); crudButtons.setManaged(true);
        dashboardView.setVisible(false); dashboardView.setManaged(false);
        gridView.setVisible(true); gridView.setManaged(true);
        btnAbonnement.getStyleClass().setAll("admin-switch-btn-active");
        btnSouscription.getStyleClass().setAll("admin-switch-btn");

        filterCombo.getItems().setAll("Tous", "Prix < 50", "Prix > 50", "Longue durée");
        filterCombo.setValue("Tous");

        abonnementMode = true;
        abonnementForm.setVisible(true); abonnementForm.setManaged(true);
        souscriptionForm.setVisible(false); souscriptionForm.setManaged(false);
        clearFormFields(); clearErrors();
        loadAbonnements();
    }

    @FXML
    public void showSouscriptions(ActionEvent event) {
        setActiveNav(btnSouscriptionNav);
        modeTitle.setText("Gestion des souscriptions");
        switchContainer.setVisible(true); switchContainer.setManaged(true);
        filterContainer.setVisible(true); filterContainer.setManaged(true);
        crudButtons.setVisible(true); crudButtons.setManaged(true);
        dashboardView.setVisible(false); dashboardView.setManaged(false);
        gridView.setVisible(true); gridView.setManaged(true);
        btnAbonnement.getStyleClass().setAll("admin-switch-btn");
        btnSouscription.getStyleClass().setAll("admin-switch-btn-active");

        filterCombo.getItems().setAll("Tous", "Active", "Inactive", "Fin proche");
        filterCombo.setValue("Tous");

        abonnementMode = false;
        abonnementForm.setVisible(false); abonnementForm.setManaged(false);
        souscriptionForm.setVisible(true); souscriptionForm.setManaged(true);
        clearFormFields(); clearErrors();
        try { subAboComboBox.getItems().setAll(abonnementCRUD.afficher()); } catch (SQLException e) { e.printStackTrace(); }
        loadSouscriptions();
    }

    // ==================== STREAM FILTERING ====================

    private void applyFilters() {
        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String filter = filterCombo.getValue() == null ? "Tous" : filterCombo.getValue();

        if (abonnementMode) {
            List<Abonnement> filtered = allAbonnements.stream()
                    .filter(a -> a.getNom().toLowerCase().contains(query) || a.getDescription().toLowerCase().contains(query))
                    .filter(a -> {
                        if (filter.equals("Prix < 50")) return a.getPrix() < 50;
                        if (filter.equals("Prix > 50")) return a.getPrix() >= 50;
                        if (filter.equals("Longue durée")) return a.getDureeMois() >= 12;
                        return true;
                    })
                    .toList();
            displayAbonnements(filtered);
        } else {
            List<Souscription> filtered = allSouscriptions.stream()
                    .filter(s -> s.getNomClient().toLowerCase().contains(query) || String.valueOf(s.getIdAbonnement()).contains(query))
                    .filter(s -> {
                        if (filter.equals("Active")) return "active".equalsIgnoreCase(s.getStatut());
                        if (filter.equals("Inactive")) return "inactive".equalsIgnoreCase(s.getStatut());
                        if (filter.equals("Fin proche")) return s.getDateFin().toLocalDate().isBefore(LocalDate.now().plusMonths(1));
                        return true;
                    })
                    .toList();
            displaySouscriptions(filtered);
        }
    }

    // ==================== DASHBOARD STATS ====================

    private void loadDashboardStats() {
        try {
            allAbonnements = abonnementCRUD.afficher();
            allSouscriptions = souscriptionCRUD.afficher();

            // Stat cards
            statTotalPlans.setText(String.valueOf(allAbonnements.size()));
            statTotalSubs.setText(String.valueOf(allSouscriptions.size()));
            long active = allSouscriptions.stream().filter(s -> "active".equalsIgnoreCase(s.getStatut())).count();
            statActiveSubs.setText(String.valueOf(active));

            // Revenue: sum prix of each souscription's linked abonnement
            Map<Integer, Abonnement> aboMap = new HashMap<>();
            for (Abonnement a : allAbonnements) aboMap.put(a.getIdAbonnement(), a);
            double revenue = 0;
            for (Souscription s : allSouscriptions) {
                Abonnement a = aboMap.get(s.getIdAbonnement());
                if (a != null) revenue += a.getPrix();
            }
            statRevenue.setText(String.format("%.0f DT", revenue));

            // Pie Chart — subscriptions per plan
            pieChart.getData().clear();
            Map<String, Integer> planCounts = new LinkedHashMap<>();
            for (Souscription s : allSouscriptions) {
                Abonnement a = aboMap.get(s.getIdAbonnement());
                String name = a != null ? a.getNom() : "Inconnu";
                planCounts.merge(name, 1, Integer::sum);
            }
            if (planCounts.isEmpty()) {
                pieChart.getData().add(new PieChart.Data("Aucune donnée", 1));
            } else {
                for (Map.Entry<String, Integer> e : planCounts.entrySet())
                    pieChart.getData().add(new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()));
            }

            // Bar Chart — price per plan
            barChart.getData().clear();
            XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
            priceSeries.setName("Prix");
            for (Abonnement a : allAbonnements)
                priceSeries.getData().add(new XYChart.Data<>(a.getNom(), a.getPrix()));
            barChart.getData().add(priceSeries);

            // Line Chart — subscriptions by month (last 6 months)
            lineChart.getData().clear();
            XYChart.Series<String, Number> monthSeries = new XYChart.Series<>();
            monthSeries.setName("Souscriptions");
            LocalDate now = LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH) + " " + month.getYear();
                long count = allSouscriptions.stream().filter(s -> {
                    if (s.getDateDebut() == null) return false;
                    LocalDate d = s.getDateDebut().toLocalDate();
                    return d.getMonth() == month.getMonth() && d.getYear() == month.getYear();
                }).count();
                monthSeries.getData().add(new XYChart.Data<>(label, count));
            }
            lineChart.getData().add(monthSeries);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== GRID LOADING ====================

    private void loadAbonnements() {
        try {
            allAbonnements = abonnementCRUD.afficher();
            displayAbonnements(allAbonnements);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void displayAbonnements(List<Abonnement> list) {
        grid.getChildren().clear();
        try {
            int col = 0, row = 1;
            for (Abonnement a : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/AbonnementCard.fxml"));
                VBox card = loader.load();
                AbonnementCardController ctrl = loader.getController();
                ctrl.setData(a);
                ctrl.setOnEdit(this::prepareAbonnementEdit);
                ctrl.setOnDelete(this::deleteAbonnement);
                if (col == 3) { col = 0; row++; }
                grid.add(card, col++, row);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadSouscriptions() {
        try {
            allSouscriptions = souscriptionCRUD.afficher();
            displaySouscriptions(allSouscriptions);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void displaySouscriptions(List<Souscription> list) {
        grid.getChildren().clear();
        try {
            int col = 0, row = 1;
            for (Souscription s : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/SouscriptionCard.fxml"));
                VBox card = loader.load();
                SouscriptionCardController ctrl = loader.getController();
                ctrl.setData(s);
                ctrl.setOnEdit(this::prepareSouscriptionEdit);
                ctrl.setOnDelete(this::deleteSouscription);
                if (col == 3) { col = 0; row++; }
                grid.add(card, col++, row);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ==================== FORM HANDLING ====================

    @FXML void handleAdd(ActionEvent event) {
        if (abonnementMode) openAbonnementPopup(null);
        else openSouscriptionPopup(null);
    }
    @FXML void handleSave(ActionEvent event) { handleAdd(event); }

    @FXML void handleCloseForm(ActionEvent event) {
        clearFormFields(); clearErrors();
        formPanel.setVisible(false); formPanel.setManaged(false);
    }

    private void prepareAbonnementEdit(Abonnement a) {
        if (!confirmAction("Confirmation", "Voulez-vous modifier cet abonnement ?")) return;
        openAbonnementPopup(a);
    }
    private void prepareSouscriptionEdit(Souscription s) {
        if (!confirmAction("Confirmation", "Voulez-vous modifier cette souscription ?")) return;
        openSouscriptionPopup(s);
    }

    private void openAbonnementPopup(Abonnement existing) {
        Stage stage = new Stage();
        stage.setTitle(existing == null ? "Nouveau Abonnement" : "Modifier Abonnement");
        VBox root = new VBox(10); root.getStyleClass().add("admin-root"); root.setStyle("-fx-padding: 16;");
        Label title = new Label(existing == null ? "Ajouter un abonnement" : "Modifier l'abonnement");
        title.getStyleClass().add("admin-section-title");

        TextField nomF = new TextField(); nomF.setPromptText("Nom");
        TextField prixF = new TextField(); prixF.setPromptText("Prix");
        TextField dureeF = new TextField(); dureeF.setPromptText("Duree (mois)");
        TextArea descF = new TextArea(); descF.setPromptText("Description"); descF.setPrefRowCount(3);
        Label nomE = new Label(), prixE = new Label(), dureeE = new Label(), descE = new Label(), dbE = new Label();
        for (Label l : new Label[]{nomE, prixE, dureeE, descE, dbE}) l.getStyleClass().add("error-label");

        if (existing != null) {
            nomF.setText(existing.getNom()); prixF.setText(String.valueOf(existing.getPrix()));
            dureeF.setText(String.valueOf(existing.getDureeMois())); descF.setText(existing.getDescription());
        }

        Button cancel = new Button("Annuler"); Button save = new Button(existing == null ? "Ajouter" : "Sauvegarder");
        cancel.getStyleClass().add("admin-btn-secondary"); save.getStyleClass().add("admin-btn-primary");
        cancel.setOnAction(e -> stage.close());
        save.setOnAction(e -> {
            nomE.setText(""); prixE.setText(""); dureeE.setText(""); descE.setText(""); dbE.setText("");
            String nom = nomF.getText() == null ? "" : nomF.getText().trim();
            String pt = prixF.getText() == null ? "" : prixF.getText().trim();
            String dt = dureeF.getText() == null ? "" : dureeF.getText().trim();
            String desc = descF.getText() == null ? "" : descF.getText().trim();
            if (nom.isEmpty()) nomE.setText("Nom obligatoire.");
            if (pt.isEmpty()) prixE.setText("Prix obligatoire.");
            if (dt.isEmpty()) dureeE.setText("Duree obligatoire.");
            if (desc.isEmpty()) descE.setText("Description obligatoire.");
            double prix; int duree;
            try { prix = Double.parseDouble(pt); duree = Integer.parseInt(dt); }
            catch (NumberFormatException ex) {
                if (!pt.isEmpty()) prixE.setText("Prix invalide.");
                if (!dt.isEmpty()) dureeE.setText("Duree invalide."); return;
            }
            if (!nomE.getText().isEmpty()||!prixE.getText().isEmpty()||!dureeE.getText().isEmpty()||!descE.getText().isEmpty()) return;
            try {
                int uid = existing == null ? ADMIN_ID : existing.getIdUser();
                Abonnement a = existing == null ? new Abonnement(uid, nom, prix, duree, desc) : new Abonnement(existing.getIdAbonnement(), uid, nom, prix, duree, desc);
                if (existing == null) { abonnementCRUD.ajouter(a); NotificationHelper.success("✅ Abonnement ajouté", "Le plan '" + nom + "' a été créé avec succès."); }
                else { abonnementCRUD.modifier(a); NotificationHelper.success("✏ Abonnement modifié", "Le plan '" + nom + "' a été mis à jour."); }
                loadAbonnements(); stage.close();
            } catch (SQLException ex) { dbE.setText("Erreur DB: " + ex.getMessage()); NotificationHelper.error("❌ Erreur", ex.getMessage()); }
        });

        Label nL=new Label("Nom"),pL=new Label("Prix"),dL=new Label("Duree (mois)"),dsL=new Label("Description");
        for (Label l : new Label[]{nL,pL,dL,dsL}) l.getStyleClass().add("muted-label");
        root.getChildren().addAll(title, nL, nomF, nomE, pL, prixF, prixE, dL, dureeF, dureeE, dsL, descF, descE, dbE, new HBox(10, cancel, save));
        Scene scene = new Scene(root, 460, 520);
        scene.getStylesheets().add(getClass().getResource("/CSS/style.css").toExternalForm());
        stage.setScene(scene); stage.show();
    }

    private void openSouscriptionPopup(Souscription existing) {
        Stage stage = new Stage();
        stage.setTitle(existing == null ? "Nouvelle Souscription" : "Modifier Souscription");
        VBox root = new VBox(10); root.getStyleClass().add("admin-root"); root.setStyle("-fx-padding: 16;");
        Label title = new Label(existing == null ? "Ajouter une souscription" : "Modifier la souscription");
        title.getStyleClass().add("admin-section-title");

        TextField clientF = new TextField(); clientF.setPromptText("Nom client");
        DatePicker debutP = new DatePicker(), finP = new DatePicker();
        TextField statutF = new TextField(); statutF.setPromptText("active / inactive");
        ComboBox<Abonnement> aboCombo = new ComboBox<>(); aboCombo.setMaxWidth(Double.MAX_VALUE);
        aboCombo.setConverter(new javafx.util.StringConverter<Abonnement>() {
            @Override public String toString(Abonnement a) { return a == null ? "" : a.getNom() + " - " + a.getPrix() + " DT"; }
            @Override public Abonnement fromString(String s) { return null; }
        });

        Label cE=new Label(),dE=new Label(),fE=new Label(),sE=new Label(),aE=new Label(),dbE=new Label();
        for (Label l : new Label[]{cE,dE,fE,sE,aE,dbE}) l.getStyleClass().add("error-label");
        try { aboCombo.getItems().setAll(abonnementCRUD.afficher()); } catch (SQLException ex) { dbE.setText("Impossible de charger."); }

        if (existing != null) {
            clientF.setText(existing.getNomClient());
            debutP.setValue(existing.getDateDebut().toLocalDate()); finP.setValue(existing.getDateFin().toLocalDate());
            statutF.setText(existing.getStatut());
            for (Abonnement a : aboCombo.getItems()) if (a.getIdAbonnement() == existing.getIdAbonnement()) { aboCombo.setValue(a); break; }
        }

        Button cancel = new Button("Annuler"); Button save = new Button(existing == null ? "Ajouter" : "Sauvegarder");
        cancel.getStyleClass().add("admin-btn-secondary"); save.getStyleClass().add("admin-btn-primary");
        cancel.setOnAction(e -> stage.close());
        save.setOnAction(e -> {
            cE.setText(""); dE.setText(""); fE.setText(""); sE.setText(""); aE.setText(""); dbE.setText("");
            String client = clientF.getText() == null ? "" : clientF.getText().trim();
            LocalDate debut = debutP.getValue(), fin = finP.getValue();
            String statut = statutF.getText() == null ? "" : statutF.getText().trim();
            Abonnement sel = aboCombo.getValue();
            if (client.isEmpty()) cE.setText("Nom client obligatoire.");
            if (debut == null) dE.setText("Date debut obligatoire.");
            if (fin == null) fE.setText("Date fin obligatoire.");
            if (statut.isEmpty()) sE.setText("Statut obligatoire.");
            if (sel == null) aE.setText("Abonnement obligatoire.");
            if (debut != null && fin != null && fin.isBefore(debut)) { fE.setText("Date fin >= date debut."); return; }
            if (!cE.getText().isEmpty()||!dE.getText().isEmpty()||!fE.getText().isEmpty()||!sE.getText().isEmpty()||!aE.getText().isEmpty()) return;
            try {
                int uid = existing == null ? CLIENT_ID : existing.getIdUser();
                Souscription s = existing == null
                        ? new Souscription(uid, client, Date.valueOf(debut), Date.valueOf(fin), statut, sel.getIdAbonnement())
                        : new Souscription(existing.getIdSouscription(), uid, client, Date.valueOf(debut), Date.valueOf(fin), statut, sel.getIdAbonnement());
                if (existing == null) { souscriptionCRUD.ajouter(s); NotificationHelper.success("✅ Souscription ajoutée", "Souscription pour '" + client + "' créée."); }
                else { souscriptionCRUD.modifier(s); NotificationHelper.success("✏ Souscription modifiée", "Souscription de '" + client + "' mise à jour."); }
                loadSouscriptions(); stage.close();
            } catch (SQLException ex) { dbE.setText("Erreur DB: " + ex.getMessage()); NotificationHelper.error("❌ Erreur", ex.getMessage()); }
        });

        Label cL=new Label("Nom client"),dL=new Label("Date debut"),fL=new Label("Date fin"),sL=new Label("Statut"),aL=new Label("Abonnement");
        for (Label l : new Label[]{cL,dL,fL,sL,aL}) l.getStyleClass().add("muted-label");
        root.getChildren().addAll(title, cL,clientF,cE, dL,debutP,dE, fL,finP,fE, sL,statutF,sE, aL,aboCombo,aE, dbE, new HBox(10,cancel,save));
        Scene scene = new Scene(root, 460, 620);
        scene.getStylesheets().add(getClass().getResource("/CSS/style.css").toExternalForm());
        stage.setScene(scene); stage.show();
    }

    // ==================== DELETE ====================

    private void deleteAbonnement(Abonnement a) {
        if (!confirmAction("Confirmation", "Voulez-vous supprimer cet abonnement ?")) return;
        try { abonnementCRUD.supprimer(a.getIdAbonnement()); loadAbonnements(); NotificationHelper.warning("🗑 Abonnement supprimé", "Le plan '" + a.getNom() + "' a été supprimé."); }
        catch (SQLException e) { showError("Suppression impossible", e.getMessage()); NotificationHelper.error("❌ Erreur", e.getMessage()); }
    }
    private void deleteSouscription(Souscription s) {
        if (!confirmAction("Confirmation", "Voulez-vous supprimer cette souscription ?")) return;
        try { souscriptionCRUD.supprimer(s.getIdSouscription()); loadSouscriptions(); NotificationHelper.warning("🗑 Souscription supprimée", "La souscription de '" + s.getNomClient() + "' a été supprimée."); }
        catch (SQLException e) { showError("Suppression impossible", e.getMessage()); NotificationHelper.error("❌ Erreur", e.getMessage()); }
    }

    // ==================== UTILS ====================

    private void clearFormFields() {
        editingAbonnementId = null; editingSouscriptionId = null;
        editingAbonnementUserId = ADMIN_ID; editingSouscriptionUserId = CLIENT_ID;
        aboNomField.clear(); aboPrixField.clear(); aboDureeField.clear(); aboDescField.clear();
        subClientField.clear(); subDateDebut.setValue(null); subDateFin.setValue(null);
        subStatutField.clear(); subAboComboBox.setValue(null);
    }
    private void clearErrors() {
        for (Label l : new Label[]{aboNomError,aboPrixError,aboDureeError,aboDescError,subClientError,subDateDebutError,subDateFinError,subStatutError,subAboIdError})
            l.setText("");
    }
    private void showError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
    private boolean confirmAction(String t, String m) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION); c.setTitle(t); c.setHeaderText(null); c.setContentText(m);
        return c.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    @FXML void switchToClientView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GUI/ClientDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Artevia - Client Dashboard"); stage.setScene(new Scene(root)); stage.show();
        } catch (IOException e) { showError("Navigation erreur", e.getMessage()); }
    }
}
