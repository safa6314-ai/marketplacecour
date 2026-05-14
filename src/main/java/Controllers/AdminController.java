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

    // ── Sidebar nav buttons ──────────────────────────────────────────
    @FXML private Button btnDashboardNav;
    @FXML private Button btnForumNav;
    @FXML private VBox   forumSubMenu;
    @FXML private Button btnPostsNav, btnCommentairesNav, btnLikesNav;
    @FXML private Button btnAbonnementNav;
    @FXML private VBox   abonnementSubMenu;
    @FXML private Button btnAbonnementListNav, btnSouscriptionNav;
    @FXML private Button btnTopDashboardNav;
    @FXML private Button btnCoursNav, btnMarketplaceNav, btnEventsNav, btnQuizNav;

    // ── Topbar ───────────────────────────────────────────────────────
    @FXML private Label modeTitle;
    @FXML private HBox  switchContainer, filterContainer, crudButtons;
    @FXML private Button btnAbonnement, btnSouscription;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;

    // ── Main views ───────────────────────────────────────────────────
    @FXML private ScrollPane dashboardView;
    @FXML private HBox       gridView;
    @FXML private GridPane   grid;
    @FXML private VBox       formPanel, abonnementForm, souscriptionForm;
    @FXML private VBox       forumView, coursView, marketplaceView, eventsView, quizView, emptyContentView;
    @FXML private ScrollPane forumScrollPane;
    @FXML private ScrollPane quizScrollPane;
    @FXML private Label      forumSubTitle;
    @FXML private Label      quizSubTitle;

    // ── Stat labels ──────────────────────────────────────────────────
    @FXML private Label statTotalPlans, statTotalSubs, statActiveSubs, statRevenue;

    // ── Charts ───────────────────────────────────────────────────────
    @FXML private PieChart                  pieChart;
    @FXML private BarChart<String, Number>  barChart;
    @FXML private LineChart<String, Number> lineChart;

    // ── Form fields ──────────────────────────────────────────────────
    @FXML private TextField  aboNomField, aboPrixField, aboDureeField;
    @FXML private TextArea   aboDescField;
    @FXML private Label      aboNomError, aboPrixError, aboDureeError, aboDescError;
    @FXML private TextField  subClientField, subStatutField;
    @FXML private DatePicker subDateDebut, subDateFin;
    @FXML private ComboBox<Abonnement> subAboComboBox;
    @FXML private Label subClientError, subDateDebutError, subDateFinError, subStatutError, subAboIdError;

    private final AbonnementCRUD  abonnementCRUD  = new AbonnementCRUD();
    private final SouscriptionCRUD souscriptionCRUD = new SouscriptionCRUD();
    private boolean abonnementMode = true;
    private Integer editingAbonnementId    = null;
    private Integer editingSouscriptionId  = null;
    private int editingAbonnementUserId   = ADMIN_ID;
    private int editingSouscriptionUserId = CLIENT_ID;

    private List<Abonnement>  allAbonnements  = new ArrayList<>();
    private List<Souscription> allSouscriptions = new ArrayList<>();

    // track which section is expanded
    private boolean forumExpanded      = false;
    private boolean abonnementExpanded = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        subAboComboBox.setConverter(new javafx.util.StringConverter<Abonnement>() {
            @Override public String toString(Abonnement a)  { return a == null ? "" : a.getNom() + " - " + a.getPrix() + " DT"; }
            @Override public Abonnement fromString(String s) { return null; }
        });
        searchField.textProperty().addListener((obs, old, nv) -> applyFilters());
        filterCombo.valueProperty().addListener((obs, old, nv) -> applyFilters());
        showEmptyContent(null);
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════

    /** Reset all top-level nav buttons to inactive style. */
    private void clearAllNav() {
        for (Button b : new Button[]{btnTopDashboardNav, btnForumNav, btnAbonnementNav,
                                     btnCoursNav, btnMarketplaceNav, btnEventsNav, btnQuizNav}) {
            b.getStyleClass().setAll("admin-nav");
        }
        for (Button b : new Button[]{btnPostsNav, btnCommentairesNav, btnLikesNav,
                                     btnAbonnementListNav, btnSouscriptionNav, btnDashboardNav}) {
            b.getStyleClass().setAll("admin-nav-sub");
        }
    }

    /** Hide every content pane. */
    private void hideAllViews() {
        dashboardView.setVisible(false);  dashboardView.setManaged(false);
        gridView.setVisible(false);       gridView.setManaged(false);
        forumView.setVisible(false);      forumView.setManaged(false);
        coursView.setVisible(false);      coursView.setManaged(false);
        marketplaceView.setVisible(false);marketplaceView.setManaged(false);
        eventsView.setVisible(false);     eventsView.setManaged(false);
        quizView.setVisible(false);       quizView.setManaged(false);
        emptyContentView.setVisible(false); emptyContentView.setManaged(false);
        switchContainer.setVisible(false);switchContainer.setManaged(false);
        filterContainer.setVisible(false);filterContainer.setManaged(false);
        crudButtons.setVisible(false);    crudButtons.setManaged(false);
    }

    // ══════════════════════════════════════════════════════════════════
    //  SIDEBAR TOGGLE SECTIONS
    // ══════════════════════════════════════════════════════════════════

    @FXML
    void toggleForumSection(ActionEvent event) {
        forumExpanded = !forumExpanded;
        forumSubMenu.setVisible(forumExpanded);
        forumSubMenu.setManaged(forumExpanded);
        btnForumNav.setText(forumExpanded ? "💬  Forum  ▴" : "💬  Forum  ▾");
        // collapse abonnement section
        if (forumExpanded && abonnementExpanded) {
            abonnementExpanded = false;
            abonnementSubMenu.setVisible(false);
            abonnementSubMenu.setManaged(false);
            btnAbonnementNav.setText("📦  Abonnements  ▾");
        }
        if (forumExpanded) showPosts(null);
    }

    @FXML
    void toggleAbonnementSection(ActionEvent event) {
        abonnementExpanded = !abonnementExpanded;
        abonnementSubMenu.setVisible(abonnementExpanded);
        abonnementSubMenu.setManaged(abonnementExpanded);
        btnAbonnementNav.setText(abonnementExpanded ? "📦  Abonnements  ▴" : "📦  Abonnements  ▾");
        // collapse forum section
        if (abonnementExpanded && forumExpanded) {
            forumExpanded = false;
            forumSubMenu.setVisible(false);
            forumSubMenu.setManaged(false);
            btnForumNav.setText("💬  Forum  ▾");
        }
        if (abonnementExpanded) showAbonnements(null);
    }

    // ══════════════════════════════════════════════════════════════════
    //  NAVIGATION HANDLERS
    // ══════════════════════════════════════════════════════════════════

    @FXML
    void showDashboard(ActionEvent event) {
        clearAllNav();
        abonnementExpanded = true;
        abonnementSubMenu.setVisible(true);
        abonnementSubMenu.setManaged(true);
        btnAbonnementNav.setText("📦  Abonnements  ▴");
        if (forumExpanded) {
            forumExpanded = false;
            forumSubMenu.setVisible(false);
            forumSubMenu.setManaged(false);
            btnForumNav.setText("💬  Forum  ▾");
        }
        btnAbonnementNav.getStyleClass().setAll("admin-nav-active");
        btnDashboardNav.getStyleClass().setAll("admin-nav-sub-active");
        modeTitle.setText("Statistiques");
        hideAllViews();
        dashboardView.setVisible(true); dashboardView.setManaged(true);
        loadDashboardStats();
    }

    // ── Forum sub-nav ─────────────────────────────────────────────────

    @FXML
    void showPosts(ActionEvent event) {
        clearAllNav();
        btnForumNav.getStyleClass().setAll("admin-nav-active");
        btnPostsNav.getStyleClass().setAll("admin-nav-sub-active");
        modeTitle.setText("Forum — Posts");
        hideAllViews();
        forumView.setVisible(true); forumView.setManaged(true);
        forumSubTitle.setText("Gérer les publications, modérer les commentaires et suivre les réactions.");
        loadForumView("/post/AfficherPostAdmin.fxml");
    }

    @FXML
    void showCommentaires(ActionEvent event) {
        clearAllNav();
        btnForumNav.getStyleClass().setAll("admin-nav-active");
        btnCommentairesNav.getStyleClass().setAll("admin-nav-sub-active");
        modeTitle.setText("Forum — Commentaires");
        hideAllViews();
        forumView.setVisible(true); forumView.setManaged(true);
        forumSubTitle.setText("Modérer les commentaires des publications.");
        loadForumView("/commentaire/AfficherCommentaire.fxml");
    }

    @FXML
    void showLikes(ActionEvent event) {
        clearAllNav();
        btnForumNav.getStyleClass().setAll("admin-nav-active");
        btnLikesNav.getStyleClass().setAll("admin-nav-sub-active");
        modeTitle.setText("Forum — Likes");
        hideAllViews();
        forumView.setVisible(true); forumView.setManaged(true);
        forumSubTitle.setText("Suivre les réactions sur les publications.");
        // Likes has no standalone view — show a placeholder label inside the scroll pane
        VBox placeholder = new VBox(16);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setStyle("-fx-padding: 60;");
        Label icon = new Label("❤"); icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label("Statistiques Likes"); title.getStyleClass().add("heading-title");
        Label sub = new Label("Consultez les posts pour voir le détail des likes par publication."); sub.getStyleClass().add("muted-label");
        placeholder.getChildren().addAll(icon, title, sub);
        forumScrollPane.setContent(placeholder);
    }

    /** Load a FXML into the shared forum ScrollPane. */
    private void loadForumView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            forumScrollPane.setContent(view);
        } catch (IOException e) {
            Label err = new Label("Impossible de charger la vue : " + fxmlPath);
            err.getStyleClass().add("muted-label");
            err.setStyle("-fx-padding: 32;");
            forumScrollPane.setContent(err);
        }
    }

    // ── Abonnement sub-nav ────────────────────────────────────────────

    @FXML
    void showAbonnements(ActionEvent event) {
        clearAllNav();
        btnAbonnementNav.getStyleClass().setAll("admin-nav-active");
        btnAbonnementListNav.getStyleClass().setAll("admin-nav-sub-active");
        modeTitle.setText("Gestion des abonnements");
        hideAllViews();
        switchContainer.setVisible(true); switchContainer.setManaged(true);
        filterContainer.setVisible(true); filterContainer.setManaged(true);
        crudButtons.setVisible(true);     crudButtons.setManaged(true);
        gridView.setVisible(true);        gridView.setManaged(true);
        btnAbonnement.getStyleClass().setAll("admin-switch-btn-active");
        btnSouscription.getStyleClass().setAll("admin-switch-btn");
        filterCombo.getItems().setAll("Tous", "Prix < 50", "Prix > 50", "Longue durée");
        filterCombo.setValue("Tous");
        abonnementMode = true;
        abonnementForm.setVisible(true);  abonnementForm.setManaged(true);
        souscriptionForm.setVisible(false); souscriptionForm.setManaged(false);
        clearFormFields(); clearErrors();
        loadAbonnements();
    }

    @FXML
    void showSouscriptions(ActionEvent event) {
        clearAllNav();
        btnAbonnementNav.getStyleClass().setAll("admin-nav-active");
        btnSouscriptionNav.getStyleClass().setAll("admin-nav-sub-active");
        modeTitle.setText("Gestion des souscriptions");
        hideAllViews();
        switchContainer.setVisible(true); switchContainer.setManaged(true);
        filterContainer.setVisible(true); filterContainer.setManaged(true);
        crudButtons.setVisible(true);     crudButtons.setManaged(true);
        gridView.setVisible(true);        gridView.setManaged(true);
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

    // ── Standalone nav items ──────────────────────────────────────────

    @FXML
    void showCours(ActionEvent event) {
        clearAllNav();
        btnCoursNav.getStyleClass().setAll("admin-nav-active");
        modeTitle.setText("Cours");
        hideAllViews();
        coursView.setVisible(true); coursView.setManaged(true);
    }

    @FXML
    void showMarketplace(ActionEvent event) {
        clearAllNav();
        btnMarketplaceNav.getStyleClass().setAll("admin-nav-active");
        modeTitle.setText("Marketplace");
        hideAllViews();
        marketplaceView.setVisible(true); marketplaceView.setManaged(true);
    }

    @FXML
    void showEvents(ActionEvent event) {
        clearAllNav();
        btnEventsNav.getStyleClass().setAll("admin-nav-active");
        modeTitle.setText("Events");
        hideAllViews();
        eventsView.setVisible(true); eventsView.setManaged(true);
    }

    @FXML
    void showQuiz(ActionEvent event) {
        showQuizQuestions(event);
    }

    @FXML
    void showQuizQuestions(ActionEvent event) {
        clearAllNav();
        btnQuizNav.getStyleClass().setAll("admin-nav-active");
        modeTitle.setText("Quiz — Questions");
        hideAllViews();
        quizView.setVisible(true);
        quizView.setManaged(true);
        quizSubTitle.setText("Creer et gerer les questions du quiz Artevia.");
        loadQuizView("/Question.fxml");
    }

    @FXML
    void showQuizReponses(ActionEvent event) {
        clearAllNav();
        btnQuizNav.getStyleClass().setAll("admin-nav-active");
        modeTitle.setText("Quiz — Reponses");
        hideAllViews();
        quizView.setVisible(true);
        quizView.setManaged(true);
        quizSubTitle.setText("Associer les reponses et indiquer les bonnes reponses.");
        loadQuizView("/Reponse.fxml");
    }

    @FXML
    void showQuizUserPreview(ActionEvent event) {
        clearAllNav();
        btnQuizNav.getStyleClass().setAll("admin-nav-active");
        modeTitle.setText("Quiz — Apercu utilisateur");
        hideAllViews();
        quizView.setVisible(true);
        quizView.setManaged(true);
        quizSubTitle.setText("Previsualiser le parcours tel que vu par un participant.");
        loadQuizView("/QuizUtilisateur.fxml");
    }

    private void loadQuizView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            NavigationUtil.stripEmbeddedQuizSidebar(view);
            quizScrollPane.setContent(view);
        } catch (IOException e) {
            Label err = new Label("Impossible de charger la vue : " + fxmlPath);
            err.getStyleClass().add("muted-label");
            err.setStyle("-fx-padding: 32;");
            quizScrollPane.setContent(err);
        }
    }

    @FXML
    void showEmptyContent(ActionEvent event) {
        clearAllNav();
        if (abonnementExpanded) {
            abonnementExpanded = false;
            abonnementSubMenu.setVisible(false);
            abonnementSubMenu.setManaged(false);
            btnAbonnementNav.setText("📦  Abonnements  ▾");
        }
        btnTopDashboardNav.getStyleClass().setAll("admin-nav-active");
        modeTitle.setText("Dashboard");
        hideAllViews();
        emptyContentView.setVisible(true);
        emptyContentView.setManaged(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  STREAM FILTERING
    // ══════════════════════════════════════════════════════════════════

    private void applyFilters() {
        String query  = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String filter = filterCombo.getValue() == null ? "Tous" : filterCombo.getValue();

        if (abonnementMode) {
            List<Abonnement> filtered = allAbonnements.stream()
                .filter(a -> a.getNom().toLowerCase().contains(query) || a.getDescription().toLowerCase().contains(query))
                .filter(a -> {
                    if (filter.equals("Prix < 50"))     return a.getPrix() < 50;
                    if (filter.equals("Prix > 50"))     return a.getPrix() >= 50;
                    if (filter.equals("Longue durée"))  return a.getDureeMois() >= 12;
                    return true;
                }).toList();
            displayAbonnements(filtered);
        } else {
            List<Souscription> filtered = allSouscriptions.stream()
                .filter(s -> s.getNomClient().toLowerCase().contains(query) || String.valueOf(s.getIdAbonnement()).contains(query))
                .filter(s -> {
                    if (filter.equals("Active"))     return "active".equalsIgnoreCase(s.getStatut());
                    if (filter.equals("Inactive"))   return "inactive".equalsIgnoreCase(s.getStatut());
                    if (filter.equals("Fin proche")) return s.getDateFin().toLocalDate().isBefore(LocalDate.now().plusMonths(1));
                    return true;
                }).toList();
            displaySouscriptions(filtered);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DASHBOARD STATS
    // ══════════════════════════════════════════════════════════════════

    private void loadDashboardStats() {
        try {
            allAbonnements   = abonnementCRUD.afficher();
            allSouscriptions = souscriptionCRUD.afficher();

            statTotalPlans.setText(String.valueOf(allAbonnements.size()));
            statTotalSubs.setText(String.valueOf(allSouscriptions.size()));
            long active = allSouscriptions.stream().filter(s -> "active".equalsIgnoreCase(s.getStatut())).count();
            statActiveSubs.setText(String.valueOf(active));

            Map<Integer, Abonnement> aboMap = new HashMap<>();
            for (Abonnement a : allAbonnements) aboMap.put(a.getIdAbonnement(), a);
            double revenue = 0;
            for (Souscription s : allSouscriptions) { Abonnement a = aboMap.get(s.getIdAbonnement()); if (a != null) revenue += a.getPrix(); }
            statRevenue.setText(String.format("%.0f DT", revenue));

            pieChart.getData().clear();
            Map<String, Integer> planCounts = new LinkedHashMap<>();
            for (Souscription s : allSouscriptions) { Abonnement a = aboMap.get(s.getIdAbonnement()); planCounts.merge(a != null ? a.getNom() : "Inconnu", 1, Integer::sum); }
            if (planCounts.isEmpty()) pieChart.getData().add(new PieChart.Data("Aucune donnée", 1));
            else planCounts.forEach((k, v) -> pieChart.getData().add(new PieChart.Data(k + " (" + v + ")", v)));

            barChart.getData().clear();
            XYChart.Series<String, Number> ps = new XYChart.Series<>(); ps.setName("Prix");
            for (Abonnement a : allAbonnements) ps.getData().add(new XYChart.Data<>(a.getNom(), a.getPrix()));
            barChart.getData().add(ps);

            lineChart.getData().clear();
            XYChart.Series<String, Number> ms = new XYChart.Series<>(); ms.setName("Souscriptions");
            LocalDate now = LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                LocalDate m = now.minusMonths(i);
                String lbl = m.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH) + " " + m.getYear();
                long cnt = allSouscriptions.stream().filter(s -> { if (s.getDateDebut()==null) return false; LocalDate d=s.getDateDebut().toLocalDate(); return d.getMonth()==m.getMonth()&&d.getYear()==m.getYear(); }).count();
                ms.getData().add(new XYChart.Data<>(lbl, cnt));
            }
            lineChart.getData().add(ms);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════
    //  GRID LOADING
    // ══════════════════════════════════════════════════════════════════

    private void loadAbonnements() {
        try { allAbonnements = abonnementCRUD.afficher(); displayAbonnements(allAbonnements); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void displayAbonnements(List<Abonnement> list) {
        grid.getChildren().clear();
        try {
            int col = 0, row = 1;
            for (Abonnement a : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/AbonnementCard.fxml"));
                VBox card = loader.load();
                AbonnementCardController ctrl = loader.getController();
                ctrl.setData(a); ctrl.setOnEdit(this::prepareAbonnementEdit); ctrl.setOnDelete(this::deleteAbonnement);
                if (col == 3) { col = 0; row++; }
                grid.add(card, col++, row);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadSouscriptions() {
        try { allSouscriptions = souscriptionCRUD.afficher(); displaySouscriptions(allSouscriptions); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void displaySouscriptions(List<Souscription> list) {
        grid.getChildren().clear();
        try {
            int col = 0, row = 1;
            for (Souscription s : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/SouscriptionCard.fxml"));
                VBox card = loader.load();
                SouscriptionCardController ctrl = loader.getController();
                ctrl.setData(s); ctrl.setOnEdit(this::prepareSouscriptionEdit); ctrl.setOnDelete(this::deleteSouscription);
                if (col == 3) { col = 0; row++; }
                grid.add(card, col++, row);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════
    //  FORM HANDLING
    // ══════════════════════════════════════════════════════════════════

    @FXML void handleAdd(ActionEvent event)   { if (abonnementMode) openAbonnementPopup(null); else openSouscriptionPopup(null); }
    @FXML void handleSave(ActionEvent event)  { handleAdd(event); }
    @FXML void handleCloseForm(ActionEvent event) { clearFormFields(); clearErrors(); formPanel.setVisible(false); formPanel.setManaged(false); }

    @FXML void handleExportCsv(ActionEvent event) {
        List<Abonnement> abos; List<Souscription> subs;
        try { abos = abonnementCRUD.afficher(); subs = souscriptionCRUD.afficher(); }
        catch (SQLException e) { e.printStackTrace(); NotificationHelper.error("Export CSV", "Impossible de lire les donnees."); return; }
        Stage stage = (Stage) modeTitle.getScene().getWindow();
        FileChooser fc = new FileChooser(); fc.setTitle("Exporter CSV"); fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv")); fc.setInitialFileName("artevia_admin_" + LocalDate.now() + ".csv");
        File file = fc.showSaveDialog(stage); if (file == null) return;
        File out = file.getName().toLowerCase(Locale.ROOT).endsWith(".csv") ? file : new File(file.getAbsolutePath() + ".csv");
        try { CsvExportService.exportAdminData(out, abos, subs); NotificationHelper.success("Export CSV", "Fichier enregistre :\n" + out.getAbsolutePath()); }
        catch (IOException ex) { ex.printStackTrace(); NotificationHelper.error("Export CSV", "Ecriture impossible."); }
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
        Stage stage = new Stage(); stage.setTitle(existing == null ? "Nouveau Abonnement" : "Modifier Abonnement");
        VBox root = new VBox(10); root.getStyleClass().add("admin-root"); root.setStyle("-fx-padding: 16;");
        Label title = new Label(existing == null ? "Ajouter un abonnement" : "Modifier l'abonnement"); title.getStyleClass().add("admin-section-title");
        TextField nomF=new TextField(); nomF.setPromptText("Nom"); TextField prixF=new TextField(); prixF.setPromptText("Prix");
        TextField dureeF=new TextField(); dureeF.setPromptText("Duree (mois)"); TextArea descF=new TextArea(); descF.setPromptText("Description"); descF.setPrefRowCount(3);
        Label nomE=new Label(),prixE=new Label(),dureeE=new Label(),descE=new Label(),dbE=new Label();
        for (Label l : new Label[]{nomE,prixE,dureeE,descE,dbE}) l.getStyleClass().add("error-label");
        if (existing != null) { nomF.setText(existing.getNom()); prixF.setText(String.valueOf(existing.getPrix())); dureeF.setText(String.valueOf(existing.getDureeMois())); descF.setText(existing.getDescription()); }
        Button cancel=new Button("Annuler"); Button save=new Button(existing==null?"Ajouter":"Sauvegarder");
        cancel.getStyleClass().add("admin-btn-secondary"); save.getStyleClass().add("admin-btn-primary");
        cancel.setOnAction(e -> stage.close());
        save.setOnAction(e -> {
            nomE.setText(""); prixE.setText(""); dureeE.setText(""); descE.setText(""); dbE.setText("");
            String nom=nomF.getText()==null?"":nomF.getText().trim(), pt=prixF.getText()==null?"":prixF.getText().trim(), dt=dureeF.getText()==null?"":dureeF.getText().trim(), desc=descF.getText()==null?"":descF.getText().trim();
            if (nom.isEmpty()) nomE.setText("Nom obligatoire."); if (pt.isEmpty()) prixE.setText("Prix obligatoire."); if (dt.isEmpty()) dureeE.setText("Duree obligatoire."); if (desc.isEmpty()) descE.setText("Description obligatoire.");
            double prix; int duree;
            try { prix=Double.parseDouble(pt); duree=Integer.parseInt(dt); } catch (NumberFormatException ex) { if (!pt.isEmpty()) prixE.setText("Prix invalide."); if (!dt.isEmpty()) dureeE.setText("Duree invalide."); return; }
            if (!nomE.getText().isEmpty()||!prixE.getText().isEmpty()||!dureeE.getText().isEmpty()||!descE.getText().isEmpty()) return;
            try {
                int uid=existing==null?ADMIN_ID:existing.getIdUser();
                Abonnement a=existing==null?new Abonnement(uid,nom,prix,duree,desc):new Abonnement(existing.getIdAbonnement(),uid,nom,prix,duree,desc);
                if (existing==null){abonnementCRUD.ajouter(a);NotificationHelper.success("✅ Abonnement ajouté","Le plan '"+nom+"' a été créé.");}
                else{abonnementCRUD.modifier(a);NotificationHelper.success("✏ Abonnement modifié","Le plan '"+nom+"' a été mis à jour.");}
                loadAbonnements(); stage.close();
            } catch (SQLException ex) { dbE.setText("Erreur DB: "+ex.getMessage()); NotificationHelper.error("❌ Erreur",ex.getMessage()); }
        });
        Label nL=new Label("Nom"),pL=new Label("Prix"),dL=new Label("Duree (mois)"),dsL=new Label("Description");
        for (Label l : new Label[]{nL,pL,dL,dsL}) l.getStyleClass().add("muted-label");
        root.getChildren().addAll(title,nL,nomF,nomE,pL,prixF,prixE,dL,dureeF,dureeE,dsL,descF,descE,dbE,new HBox(10,cancel,save));
        Scene scene=new Scene(root,460,520); scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); stage.setScene(scene); stage.show();
    }

    private void openSouscriptionPopup(Souscription existing) {
        Stage stage=new Stage(); stage.setTitle(existing==null?"Nouvelle Souscription":"Modifier Souscription");
        VBox root=new VBox(10); root.getStyleClass().add("admin-root"); root.setStyle("-fx-padding: 16;");
        Label title=new Label(existing==null?"Ajouter une souscription":"Modifier la souscription"); title.getStyleClass().add("admin-section-title");
        TextField clientF=new TextField(); clientF.setPromptText("Nom client"); DatePicker debutP=new DatePicker(),finP=new DatePicker();
        TextField statutF=new TextField(); statutF.setPromptText("active / inactive"); ComboBox<Abonnement> aboCombo=new ComboBox<>(); aboCombo.setMaxWidth(Double.MAX_VALUE);
        aboCombo.setConverter(new javafx.util.StringConverter<Abonnement>(){@Override public String toString(Abonnement a){return a==null?"":a.getNom()+" - "+a.getPrix()+" DT";}@Override public Abonnement fromString(String s){return null;}});
        Label cE=new Label(),dE=new Label(),fE=new Label(),sE=new Label(),aE=new Label(),dbE=new Label();
        for (Label l:new Label[]{cE,dE,fE,sE,aE,dbE}) l.getStyleClass().add("error-label");
        try{aboCombo.getItems().setAll(abonnementCRUD.afficher());}catch(SQLException ex){dbE.setText("Impossible de charger.");}
        if (existing!=null){clientF.setText(existing.getNomClient());debutP.setValue(existing.getDateDebut().toLocalDate());finP.setValue(existing.getDateFin().toLocalDate());statutF.setText(existing.getStatut());for(Abonnement a:aboCombo.getItems()) if(a.getIdAbonnement()==existing.getIdAbonnement()){aboCombo.setValue(a);break;}}
        Button cancel=new Button("Annuler"); Button save=new Button(existing==null?"Ajouter":"Sauvegarder");
        cancel.getStyleClass().add("admin-btn-secondary"); save.getStyleClass().add("admin-btn-primary"); cancel.setOnAction(e->stage.close());
        save.setOnAction(e->{
            cE.setText("");dE.setText("");fE.setText("");sE.setText("");aE.setText("");dbE.setText("");
            String client=clientF.getText()==null?"":clientF.getText().trim(); LocalDate debut=debutP.getValue(),fin=finP.getValue(); String statut=statutF.getText()==null?"":statutF.getText().trim(); Abonnement sel=aboCombo.getValue();
            if(client.isEmpty())cE.setText("Nom client obligatoire."); if(debut==null)dE.setText("Date debut obligatoire."); if(fin==null)fE.setText("Date fin obligatoire."); if(statut.isEmpty())sE.setText("Statut obligatoire."); if(sel==null)aE.setText("Abonnement obligatoire.");
            if(debut!=null&&fin!=null&&fin.isBefore(debut)){fE.setText("Date fin >= date debut.");return;}
            if(!cE.getText().isEmpty()||!dE.getText().isEmpty()||!fE.getText().isEmpty()||!sE.getText().isEmpty()||!aE.getText().isEmpty()) return;
            try{
                int uid=existing==null?CLIENT_ID:existing.getIdUser();
                Souscription s=existing==null?new Souscription(uid,client,Date.valueOf(debut),Date.valueOf(fin),statut,sel.getIdAbonnement()):new Souscription(existing.getIdSouscription(),uid,client,Date.valueOf(debut),Date.valueOf(fin),statut,sel.getIdAbonnement());
                if(existing==null){souscriptionCRUD.ajouter(s);NotificationHelper.success("✅ Souscription ajoutée","Souscription pour '"+client+"' créée.");}
                else{souscriptionCRUD.modifier(s);NotificationHelper.success("✏ Souscription modifiée","Souscription de '"+client+"' mise à jour.");}
                loadSouscriptions(); stage.close();
            }catch(SQLException ex){dbE.setText("Erreur DB: "+ex.getMessage());NotificationHelper.error("❌ Erreur",ex.getMessage());}
        });
        Label cL=new Label("Nom client"),dL=new Label("Date debut"),fL=new Label("Date fin"),sL=new Label("Statut"),aL=new Label("Abonnement");
        for(Label l:new Label[]{cL,dL,fL,sL,aL}) l.getStyleClass().add("muted-label");
        root.getChildren().addAll(title,cL,clientF,cE,dL,debutP,dE,fL,finP,fE,sL,statutF,sE,aL,aboCombo,aE,dbE,new HBox(10,cancel,save));
        Scene scene=new Scene(root,460,620); scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); stage.setScene(scene); stage.show();
    }

    // ══════════════════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════════════════

    private void deleteAbonnement(Abonnement a) {
        if (!confirmAction("Confirmation","Voulez-vous supprimer cet abonnement ?")) return;
        try { abonnementCRUD.supprimer(a.getIdAbonnement()); loadAbonnements(); NotificationHelper.warning("🗑 Abonnement supprimé","Le plan '"+a.getNom()+"' a été supprimé."); }
        catch (SQLException e) { showError("Suppression impossible",e.getMessage()); NotificationHelper.error("❌ Erreur",e.getMessage()); }
    }
    private void deleteSouscription(Souscription s) {
        if (!confirmAction("Confirmation","Voulez-vous supprimer cette souscription ?")) return;
        try { souscriptionCRUD.supprimer(s.getIdSouscription()); loadSouscriptions(); NotificationHelper.warning("🗑 Souscription supprimée","La souscription de '"+s.getNomClient()+"' a été supprimée."); }
        catch (SQLException e) { showError("Suppression impossible",e.getMessage()); NotificationHelper.error("❌ Erreur",e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════════════════
    //  CSS HELPERS
    // ══════════════════════════════════════════════════════════════════

    private void clearFormFields() {
        editingAbonnementId=null; editingSouscriptionId=null; editingAbonnementUserId=ADMIN_ID; editingSouscriptionUserId=CLIENT_ID;
        aboNomField.clear(); aboPrixField.clear(); aboDureeField.clear(); aboDescField.clear();
        subClientField.clear(); subDateDebut.setValue(null); subDateFin.setValue(null); subStatutField.clear(); subAboComboBox.setValue(null);
    }
    private void clearErrors() {
        for (Label l:new Label[]{aboNomError,aboPrixError,aboDureeError,aboDescError,subClientError,subDateDebutError,subDateFinError,subStatutError,subAboIdError}) l.setText("");
    }
    private void showError(String t, String m) {
        Alert a=new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
    private boolean confirmAction(String t, String m) {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION); c.setTitle(t); c.setHeaderText(null); c.setContentText(m);
        return c.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK;
    }

    @FXML void switchToClientView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GUI/ClientDashboard.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setTitle("Artevia - Client Dashboard"); stage.setScene(new Scene(root)); stage.show();
        } catch (IOException e) { showError("Navigation erreur", e.getMessage()); }
    }
}
