package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.example.entities.Achat;
import org.example.entities.Vente;
import org.example.services.ServiceAchat;
import org.example.services.ServiceVente;

import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MarketplaceController {

    private static final String MODE_ADMIN = "Admin";
    private static final String MODE_CLIENT = "Client";

    private final ServiceVente serviceVente = new ServiceVente();
    private final ServiceAchat serviceAchat = new ServiceAchat();
    private final ObservableList<Vente> ventesData = FXCollections.observableArrayList();
    private final ObservableList<Achat> achatsData = FXCollections.observableArrayList();
    private final ObservableList<Vente> cartData = FXCollections.observableArrayList();
    private final FilteredList<Vente> filteredVentesData = new FilteredList<>(ventesData, vente -> true);
    private final FilteredList<Achat> filteredAchatsData = new FilteredList<>(achatsData, achat -> true);

    @FXML private ComboBox<String> modeComboBox;
    @FXML private VBox adminView;
    @FXML private VBox adminArticlesView;
    @FXML private VBox adminAchatsView;
    @FXML private VBox clientView;
    @FXML private VBox clientCatalogView;
    @FXML private VBox clientCartView;
    @FXML private Button adminArticlesButton;
    @FXML private Button adminAchatsButton;
    @FXML private Button clientCatalogButton;
    @FXML private Button clientCartButton;
    @FXML private ListView<Vente> ventesListView;
    @FXML private ListView<Achat> achatsListView;
    @FXML private ListView<Vente> cartListView;
    @FXML private TilePane clientGrid;
    @FXML private Label cartTotalLabel;
    @FXML private TextField adminArticleSearchField;
    @FXML private ComboBox<String> adminStockFilterComboBox;
    @FXML private TextField achatSearchField;
    @FXML private ComboBox<String> achatStatusFilterComboBox;
    @FXML private TextField clientSearchField;
    @FXML private ComboBox<String> clientCategoryFilterComboBox;

    @FXML
    public void initialize() {
        modeComboBox.setItems(FXCollections.observableArrayList(MODE_ADMIN, MODE_CLIENT));
        modeComboBox.getSelectionModel().select(MODE_ADMIN);
        modeComboBox.valueProperty().addListener((obs, oldMode, newMode) -> updateMode(newMode));

        ventesListView.setItems(filteredVentesData);
        achatsListView.setItems(filteredAchatsData);
        cartListView.setItems(cartData);
        ventesListView.setCellFactory(list -> new VenteAdminCell());
        achatsListView.setCellFactory(list -> new AchatAdminCell());
        cartListView.setCellFactory(list -> new CartCell());
        cartData.addListener((javafx.collections.ListChangeListener<Vente>) change -> updateCartTotal());
        setupFilters();

        seedDemoData();
        refreshVentes();
        refreshAchats();
        showAdminArticles();
        showClientCatalog();
        updateMode(MODE_ADMIN);
    }

    @FXML
    private void onAddVente() {
        openVenteDialog(null).ifPresent(vente -> {
            try {
                serviceVente.ajouter(vente);
                refreshVentes();
                showSuccess("Article ajoute", "L'article a ete ajoute avec succes.");
            } catch (Exception ex) {
                showError("Erreur ajout article", ex.getMessage());
            }
        });
    }

    @FXML
    private void onEditVente() {
        Vente selected = ventesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un article a modifier.");
            return;
        }
        openVenteDialog(selected).ifPresent(vente -> {
            try {
                serviceVente.modifier(vente);
                refreshVentes();
                showSuccess("Article modifie", "L'article a ete modifie avec succes.");
            } catch (Exception ex) {
                showError("Erreur modification article", ex.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteVente() {
        Vente selected = ventesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un article a supprimer.");
            return;
        }
        if (confirmAction("Confirmation", "Supprimer l'article", "Confirmer la suppression de: " + selected.getTitre() + " ?")) {
            try {
                serviceVente.supprimer(selected.getId());
                refreshVentes();
                showSuccess("Article supprime", "L'article a ete supprime avec succes.");
            } catch (Exception ex) {
                showError("Erreur suppression article", ex.getMessage());
            }
        }
    }

    @FXML
    private void onAddAchat() {
        openAchatDialog(null).ifPresent(achat -> {
            try {
                serviceAchat.ajouter(achat);
                refreshAchats();
                showSuccess("Achat ajoute", "L'achat a ete ajoute avec succes.");
            } catch (Exception ex) {
                showError("Erreur ajout achat", ex.getMessage());
            }
        });
    }

    @FXML
    private void onEditAchat() {
        Achat selected = achatsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un achat a modifier.");
            return;
        }
        openAchatDialog(selected).ifPresent(achat -> {
            try {
                serviceAchat.modifier(achat);
                refreshAchats();
                showSuccess("Achat modifie", "L'achat a ete modifie avec succes.");
            } catch (Exception ex) {
                showError("Erreur modification achat", ex.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteAchat() {
        Achat selected = achatsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un achat a supprimer.");
            return;
        }
        if (confirmAction("Confirmation", "Supprimer l'achat", "Confirmer la suppression de: " + selected.getNomOeuvre() + " ?")) {
            try {
                serviceAchat.supprimer(selected.getId());
                refreshAchats();
                showSuccess("Achat supprime", "L'achat a ete supprime avec succes.");
            } catch (Exception ex) {
                showError("Erreur suppression achat", ex.getMessage());
            }
        }
    }

    @FXML
    private void onConfirmAchat() {
        Achat selected = achatsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un achat a confirmer.");
            return;
        }
        if (!confirmAction("Confirmation", "Confirmer l'achat", "Voulez-vous accepter l'achat de: " + selected.getNomOeuvre() + " ?")) {
            return;
        }
        try {
            serviceAchat.confirmer(selected.getId());
            refreshAchats();
            showSuccess("Achat accepte", "L'achat a ete confirme avec succes.");
        } catch (Exception ex) {
            showError("Erreur confirmation achat", ex.getMessage());
        }
    }

    @FXML
    private void onRefuseAchat() {
        Achat selected = achatsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un achat a refuser.");
            return;
        }
        if (!confirmAction("Confirmation", "Refuser l'achat", "Voulez-vous refuser l'achat de: " + selected.getNomOeuvre() + " ?")) {
            return;
        }
        try {
            serviceAchat.refuser(selected.getId());
            refreshAchats();
            showSuccess("Achat refuse", "L'achat a ete refuse avec succes.");
        } catch (Exception ex) {
            showError("Erreur refus achat", ex.getMessage());
        }
    }

    @FXML
    private void onShowAdminArticles() {
        refreshVentes();
        showAdminArticles();
    }

    @FXML
    private void onShowAdminAchats() {
        refreshAchats();
        showAdminAchats();
    }

    @FXML
    private void onShowClientCatalog() {
        refreshVentes();
        showClientCatalog();
    }

    @FXML
    private void onShowClientCart() {
        updateCartTotal();
        showClientCart();
    }

    @FXML
    private void onRemoveFromCart() {
        Vente selected = cartListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un article a retirer du panier.");
            return;
        }
        cartData.remove(selected);
    }

    @FXML
    private void onClearCart() {
        if (cartData.isEmpty()) {
            showError("Panier vide", "Votre panier est deja vide.");
            return;
        }
        if (confirmAction("Confirmation", "Vider le panier", "Voulez-vous retirer tous les articles du panier ?")) {
            cartData.clear();
        }
    }

    @FXML
    private void onValidateCart() {
        if (cartData.isEmpty()) {
            showError("Panier vide", "Ajoutez au moins un article avant de valider la commande.");
            return;
        }
        refreshVentes();
        String stockError = validateCartStock();
        if (stockError != null) {
            showError("Stock insuffisant", stockError);
            return;
        }
        if (!confirmAction("Confirmation", "Valider la commande", "Voulez-vous envoyer le panier a l'admin pour confirmation ?")) {
            return;
        }
        try {
            for (Vente vente : new ArrayList<>(cartData)) {
                serviceVente.diminuerQuantite(vente.getId());
                serviceAchat.ajouter(new Achat(
                        safeText(vente.getTitre(), "Article"),
                        "Client test",
                        vente.getPrix(),
                        Date.valueOf(LocalDate.now()),
                        "En attente"
                ));
            }
            cartData.clear();
            refreshVentes();
            refreshAchats();
            showSuccess("Commande en attente", "Votre panier a ete envoye a l'admin pour confirmation.");
            showClientCatalog();
        } catch (Exception ex) {
            showError("Commande impossible", ex.getMessage());
        }
    }

    private void updateMode(String mode) {
        boolean admin = MODE_ADMIN.equals(mode);
        adminView.setVisible(admin);
        adminView.setManaged(admin);
        clientView.setVisible(!admin);
        clientView.setManaged(!admin);
        if (admin) {
            refreshVentes();
            refreshAchats();
        } else {
            refreshVentes();
        }
    }

    private void seedDemoData() {
        try {
            serviceVente.ajouterOeuvresDemoSiAbsentes();
        } catch (Exception ex) {
            showError("Donnees de validation", "Impossible d'ajouter les oeuvres de demonstration: " + ex.getMessage());
        }
    }

    private void showAdminArticles() {
        setAdminSection(adminArticlesView, true);
        setAdminSection(adminAchatsView, false);
        setActiveNavButton(adminArticlesButton, true);
        setActiveNavButton(adminAchatsButton, false);
    }

    private void showAdminAchats() {
        setAdminSection(adminArticlesView, false);
        setAdminSection(adminAchatsView, true);
        setActiveNavButton(adminArticlesButton, false);
        setActiveNavButton(adminAchatsButton, true);
    }

    private void showClientCatalog() {
        setClientSection(clientCatalogView, true);
        setClientSection(clientCartView, false);
        setActiveNavButton(clientCatalogButton, true);
        setActiveNavButton(clientCartButton, false);
    }

    private void showClientCart() {
        setClientSection(clientCatalogView, false);
        setClientSection(clientCartView, true);
        setActiveNavButton(clientCatalogButton, false);
        setActiveNavButton(clientCartButton, true);
    }

    private void setAdminSection(VBox section, boolean visible) {
        section.setVisible(visible);
        section.setManaged(visible);
    }

    private void setClientSection(VBox section, boolean visible) {
        section.setVisible(visible);
        section.setManaged(visible);
    }

    private void setActiveNavButton(Button button, boolean active) {
        button.getStyleClass().removeAll("nav-btn", "nav-btn-active");
        button.getStyleClass().add(active ? "nav-btn-active" : "nav-btn");
    }

    private void refreshVentes() {
        try {
            ventesData.setAll(serviceVente.afficherAll());
            updateClientCategoryFilter();
            applyAdminArticleFilters();
        } catch (Exception e) {
            ventesData.clear();
            showError("Chargement articles impossible", e.getMessage());
        }
        renderClientGrid();
    }

    private void refreshAchats() {
        try {
            achatsData.setAll(serviceAchat.afficher());
            applyAchatFilters();
        } catch (Exception e) {
            achatsData.clear();
            showError("Chargement achats impossible", e.getMessage());
        }
    }

    private void setupFilters() {
        adminStockFilterComboBox.setItems(FXCollections.observableArrayList("Tous les articles", "Disponibles", "Epuises"));
        adminStockFilterComboBox.getSelectionModel().select("Tous les articles");
        adminArticleSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyAdminArticleFilters());
        adminStockFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applyAdminArticleFilters());

        achatStatusFilterComboBox.setItems(FXCollections.observableArrayList("Tous les statuts", "En attente", "Confirme", "Refuse"));
        achatStatusFilterComboBox.getSelectionModel().select("Tous les statuts");
        achatSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyAchatFilters());
        achatStatusFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applyAchatFilters());

        clientCategoryFilterComboBox.setItems(FXCollections.observableArrayList("Toutes les categories"));
        clientCategoryFilterComboBox.getSelectionModel().select("Toutes les categories");
        clientSearchField.textProperty().addListener((obs, oldValue, newValue) -> renderClientGrid());
        clientCategoryFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> renderClientGrid());
    }

    private void applyAdminArticleFilters() {
        String query = normalized(adminArticleSearchField == null ? "" : adminArticleSearchField.getText());
        String stockFilter = adminStockFilterComboBox == null ? "Tous les articles" : adminStockFilterComboBox.getValue();
        filteredVentesData.setPredicate(vente -> {
            boolean textMatches = query.isEmpty()
                    || normalized(vente.getTitre()).contains(query)
                    || normalized(vente.getCategorie()).contains(query)
                    || normalized(vente.getNomArtiste()).contains(query)
                    || normalized(vente.getDescription()).contains(query);
            boolean stockMatches = "Epuises".equals(stockFilter)
                    ? vente.getQuantite() <= 0
                    : !"Disponibles".equals(stockFilter) || vente.getQuantite() > 0;
            return textMatches && stockMatches;
        });
    }

    private void applyAchatFilters() {
        String query = normalized(achatSearchField == null ? "" : achatSearchField.getText());
        String statusFilter = achatStatusFilterComboBox == null ? "Tous les statuts" : achatStatusFilterComboBox.getValue();
        filteredAchatsData.setPredicate(achat -> {
            boolean textMatches = query.isEmpty()
                    || normalized(achat.getNomOeuvre()).contains(query)
                    || normalized(achat.getNomAcheteur()).contains(query);
            boolean statusMatches = statusFilter == null
                    || "Tous les statuts".equals(statusFilter)
                    || statusFilter.equals(achat.getStatut());
            return textMatches && statusMatches;
        });
    }

    private void updateClientCategoryFilter() {
        if (clientCategoryFilterComboBox == null) {
            return;
        }
        String selected = clientCategoryFilterComboBox.getValue();
        ObservableList<String> categories = FXCollections.observableArrayList("Toutes les categories");
        for (Vente vente : ventesData) {
            String category = safeText(vente.getCategorie(), "");
            if (!category.isEmpty() && !categories.contains(category)) {
                categories.add(category);
            }
        }
        clientCategoryFilterComboBox.setItems(categories);
        if (selected != null && categories.contains(selected)) {
            clientCategoryFilterComboBox.getSelectionModel().select(selected);
        } else {
            clientCategoryFilterComboBox.getSelectionModel().select("Toutes les categories");
        }
    }

    private void renderClientGrid() {
        if (clientGrid == null) {
            return;
        }
        clientGrid.getChildren().clear();
        List<Vente> visibleArticles = filteredClientArticles();
        if (visibleArticles.isEmpty()) {
            Label empty = new Label("Aucun article ne correspond a votre recherche.");
            empty.getStyleClass().add("empty-label");
            clientGrid.getChildren().add(empty);
            return;
        }
        for (Vente vente : visibleArticles) {
            clientGrid.getChildren().add(createArticleCard(vente));
        }
    }

    private List<Vente> filteredClientArticles() {
        String query = normalized(clientSearchField == null ? "" : clientSearchField.getText());
        String categoryFilter = clientCategoryFilterComboBox == null ? "Toutes les categories" : clientCategoryFilterComboBox.getValue();
        List<Vente> results = new ArrayList<>();
        for (Vente vente : ventesData) {
            boolean textMatches = query.isEmpty()
                    || normalized(vente.getTitre()).contains(query)
                    || normalized(vente.getCategorie()).contains(query)
                    || normalized(vente.getNomArtiste()).contains(query)
                    || normalized(vente.getDescription()).contains(query);
            boolean categoryMatches = categoryFilter == null
                    || "Toutes les categories".equals(categoryFilter)
                    || categoryFilter.equals(vente.getCategorie());
            if (textMatches && categoryMatches) {
                results.add(vente);
            }
        }
        return results;
    }

    private Node createArticleCard(Vente vente) {
        VBox card = new VBox(10);
        card.getStyleClass().add("article-card");
        card.setPrefWidth(280);

        Node photo = createArtworkPreview(vente, 256, 150, "article-photo");

        Label category = new Label(safeText(vente.getCategorie(), "Article"));
        category.getStyleClass().add("category-pill");

        Label title = new Label(safeText(vente.getTitre(), "Sans titre"));
        title.getStyleClass().add("article-title");
        title.setWrapText(true);

        Label artist = new Label("Par " + safeText(vente.getNomArtiste(), "Artiste inconnu"));
        artist.getStyleClass().add("muted-label");

        Label description = new Label(safeText(vente.getDescription(), "Aucune description."));
        description.getStyleClass().add("article-description");
        description.setWrapText(true);

        Label stock = new Label(stockText(vente));
        stock.getStyleClass().add(vente.getQuantite() <= 0 ? "stock-empty" : "stock-label");

        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label(formatPrice(vente.getPrix()));
        price.getStyleClass().add("price-label");
        Button buyButton = new Button(vente.getQuantite() <= 0 ? "Article epuise" : "Ajouter au panier");
        buyButton.getStyleClass().add("primary-btn");
        buyButton.setDisable(vente.getQuantite() <= 0);
        buyButton.setOnAction(event -> addToCart(vente));
        footer.getChildren().addAll(price, buyButton);
        HBox.setHgrow(price, javafx.scene.layout.Priority.ALWAYS);

        if (vente.getQuantite() <= 0) {
            card.getStyleClass().add("article-card-empty");
        }
        card.getChildren().addAll(photo, category, title, artist, description, stock, footer);
        return card;
    }

    private void addToCart(Vente vente) {
        if (vente.getQuantite() <= 0) {
            showError("Article epuise", "Impossible d'ajouter au panier: l'article \"" + vente.getTitre() + "\" est epuise.");
            return;
        }
        if (countCartItems(vente.getId()) >= vente.getQuantite()) {
            showError("Stock insuffisant", "Toute la quantite disponible de cet article est deja dans le panier.");
            return;
        }
        cartData.add(vente);
        showSuccess("Ajoute au panier", "\"" + vente.getTitre() + "\" a ete ajoute au panier.");
    }

    private int countCartItems(int venteId) {
        int count = 0;
        for (Vente cartItem : cartData) {
            if (cartItem.getId() == venteId) {
                count++;
            }
        }
        return count;
    }

    private String validateCartStock() {
        Map<Integer, Integer> requestedQuantities = new HashMap<>();
        for (Vente cartItem : cartData) {
            requestedQuantities.put(cartItem.getId(), requestedQuantities.getOrDefault(cartItem.getId(), 0) + 1);
        }

        for (Map.Entry<Integer, Integer> entry : requestedQuantities.entrySet()) {
            Vente current = findVenteById(entry.getKey());
            if (current == null) {
                return "Un article du panier n'est plus disponible.";
            }
            if (current.getQuantite() < entry.getValue()) {
                return "Stock insuffisant pour: " + current.getTitre() + ". Disponible: " + current.getQuantite() + ", dans panier: " + entry.getValue() + ".";
            }
        }
        return null;
    }

    private Vente findVenteById(int id) {
        for (Vente vente : ventesData) {
            if (vente.getId() == id) {
                return vente;
            }
        }
        return null;
    }

    private void updateCartTotal() {
        double total = 0;
        for (Vente vente : cartData) {
            total += vente.getPrix();
        }
        if (cartTotalLabel != null) {
            cartTotalLabel.setText(formatPrice(total));
        }
        if (clientCartButton != null) {
            clientCartButton.setText("Panier (" + cartData.size() + ")");
        }
    }

    private Optional<Vente> openVenteDialog(Vente existing) {
        Dialog<Vente> dialog = new Dialog<>();
        styleDialog(dialog.getDialogPane());
        dialog.setTitle(existing == null ? "Vendre un article" : "Modifier l'article");
        dialog.setHeaderText(existing == null ? "Nouvel article a vendre" : "Mise a jour de l'article");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField titreField = new TextField();
        TextArea descriptionArea = new TextArea();
        TextField prixField = new TextField();
        TextField categorieField = new TextField();
        TextField artisteField = new TextField();
        TextField quantiteField = new TextField();
        TextField imagePathField = new TextField();
        imagePathField.setEditable(false);
        titreField.setPromptText("Ex: Tableau abstrait");
        descriptionArea.setPromptText("Decrivez l'article pour le client");
        prixField.setPromptText("Ex: 250");
        categorieField.setPromptText("Ex: Peinture, Sculpture, Photo");
        artisteField.setPromptText("Nom du vendeur / artiste");
        quantiteField.setPromptText("Ex: 5");
        imagePathField.setPromptText("Aucune image selectionnee");
        descriptionArea.setPrefRowCount(4);

        if (existing != null) {
            titreField.setText(existing.getTitre());
            descriptionArea.setText(existing.getDescription());
            prixField.setText(String.valueOf(existing.getPrix()));
            categorieField.setText(existing.getCategorie());
            artisteField.setText(existing.getNomArtiste());
            quantiteField.setText(String.valueOf(existing.getQuantite()));
            imagePathField.setText(safeText(existing.getImagePath(), ""));
        }

        String[] selectedImagePath = {imagePathField.getText()};
        VBox previewBox = new VBox(10);
        previewBox.getStyleClass().addAll("form-card", "form-preview");
        Label previewTitle = new Label("Apercu article");
        previewTitle.getStyleClass().add("form-section-title");
        ImageView previewImage = new ImageView(createPreviewImage(selectedImagePath[0], titreField.getText(), categorieField.getText(), 250, 170));
        previewImage.setFitWidth(250);
        previewImage.setFitHeight(170);
        previewImage.setPreserveRatio(false);
        Label previewHint = new Label("Choisissez l'image de l'article a vendre.");
        previewHint.getStyleClass().add("muted-label");
        Button chooseImageButton = new Button("Choisir image");
        chooseImageButton.getStyleClass().add("secondary-btn");
        chooseImageButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir l'image de l'article");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
            );
            File file = chooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedImagePath[0] = file.getAbsolutePath();
                imagePathField.setText(selectedImagePath[0]);
                previewImage.setImage(createPreviewImage(selectedImagePath[0], titreField.getText(), categorieField.getText(), 250, 170));
            }
        });
        previewBox.getChildren().addAll(previewTitle, previewImage, chooseImageButton, previewHint);

        Runnable updatePreview = () -> previewImage.setImage(createPreviewImage(selectedImagePath[0], titreField.getText(), categorieField.getText(), 250, 170));
        titreField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview.run());
        categorieField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview.run());

        GridPane form = createFormGrid();
        form.getStyleClass().add("product-form-grid");
        addRow(form, 0, "Titre de l'article", titreField);
        addRow(form, 1, "Description", descriptionArea);
        addRow(form, 2, "Prix", prixField);
        addRow(form, 3, "Categorie", categorieField);
        addRow(form, 4, "Artiste / vendeur", artisteField);
        addRow(form, 5, "Quantite", quantiteField);
        addRow(form, 6, "Image", imagePathField);

        Label formTitle = new Label("Informations de vente");
        formTitle.getStyleClass().add("form-section-title");
        Label formHint = new Label("Ces informations seront visibles dans le catalogue client.");
        formHint.getStyleClass().add("muted-label");
        VBox formBox = new VBox(10, formTitle, formHint, form);
        formBox.getStyleClass().add("form-card");

        HBox content = new HBox(16, previewBox, formBox);
        content.getStyleClass().add("product-dialog-content");
        content.setAlignment(Pos.TOP_LEFT);
        dialog.getDialogPane().setContent(content);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateVenteForm(titreField, descriptionArea, prixField, categorieField, artisteField, quantiteField, imagePathField)) {
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                double prix = parseDouble(prixField.getText());
                int quantite = parseInt(quantiteField.getText());
                String imagePath = safeText(selectedImagePath[0], null);
                if (existing == null) {
                    return new Vente(titreField.getText(), descriptionArea.getText(), prix, categorieField.getText(), artisteField.getText(), quantite, imagePath);
                }
                return new Vente(existing.getId(), titreField.getText(), descriptionArea.getText(), prix, categorieField.getText(), artisteField.getText(), quantite, imagePath);
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private Optional<Achat> openAchatDialog(Achat existing) {
        Dialog<Achat> dialog = new Dialog<>();
        styleDialog(dialog.getDialogPane());
        dialog.setTitle(existing == null ? "Ajouter Achat" : "Modifier Achat");
        dialog.setHeaderText(existing == null ? "Nouvel achat" : "Edition de l'achat");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField oeuvreField = new TextField();
        TextField acheteurField = new TextField();
        TextField prixField = new TextField();
        DatePicker datePicker = new DatePicker();
        ComboBox<String> statutComboBox = new ComboBox<>();
        statutComboBox.setItems(FXCollections.observableArrayList("En attente", "Confirme", "Refuse"));

        if (existing != null) {
            oeuvreField.setText(existing.getNomOeuvre());
            acheteurField.setText(existing.getNomAcheteur());
            prixField.setText(String.valueOf(existing.getPrix()));
            statutComboBox.getSelectionModel().select(safeText(existing.getStatut(), "En attente"));
            if (existing.getDateAchat() != null) {
                datePicker.setValue(existing.getDateAchat().toLocalDate());
            }
        } else {
            statutComboBox.getSelectionModel().select("En attente");
        }

        GridPane form = createFormGrid();
        addRow(form, 0, "Nom Oeuvre", oeuvreField);
        addRow(form, 1, "Nom Acheteur", acheteurField);
        addRow(form, 2, "Prix", prixField);
        addRow(form, 3, "Date Achat", datePicker);
        addRow(form, 4, "Statut", statutComboBox);
        dialog.getDialogPane().setContent(form);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAchatForm(oeuvreField, acheteurField, prixField, datePicker, statutComboBox)) {
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                double prix = parseDouble(prixField.getText());
                Date date = parseDate(datePicker.getValue());
                String statut = safeText(statutComboBox.getValue(), "En attente");
                if (existing == null) {
                    return new Achat(oeuvreField.getText(), acheteurField.getText(), prix, date, statut);
                }
                return new Achat(existing.getId(), oeuvreField.getText(), acheteurField.getText(), prix, date, statut);
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(8));
        return grid;
    }

    private void addRow(GridPane grid, int row, String label, Node node) {
        Label l = new Label(label);
        l.getStyleClass().add("input-label");
        grid.add(l, 0, row);
        grid.add(node, 1, row);
        if (node instanceof TextArea) {
            ((TextArea) node).setPrefRowCount(4);
        }
        if (node instanceof TextField) {
            ((TextField) node).setPrefWidth(340);
        }
    }

    private double parseDouble(String value) {
        return Double.parseDouble(value.trim());
    }

    private int parseInt(String value) {
        return Integer.parseInt(value.trim());
    }

    private Date parseDate(LocalDate localDate) {
        if (localDate == null) {
            throw new IllegalArgumentException("Date obligatoire");
        }
        return Date.valueOf(localDate);
    }

    private boolean validateVenteForm(TextField titreField, TextArea descriptionArea, TextField prixField,
                                      TextField categorieField, TextField artisteField, TextField quantiteField,
                                      TextField imagePathField) {
        clearValidation(titreField, descriptionArea, prixField, categorieField, artisteField, quantiteField, imagePathField);
        List<String> errors = new ArrayList<>();

        requireText(titreField, "Le titre de l'article est obligatoire.", errors);
        requireText(descriptionArea, "La description est obligatoire.", errors);
        requireText(categorieField, "La categorie est obligatoire.", errors);
        requireText(artisteField, "Le nom artiste / vendeur est obligatoire.", errors);
        validatePositiveDouble(prixField, "Le prix doit etre un nombre strictement positif.", errors);
        validateNonNegativeInt(quantiteField, "La quantite doit etre un nombre entier positif ou zero.", errors);
        validateImagePath(imagePathField, "L'image de l'article est obligatoire.", errors);

        if (!errors.isEmpty()) {
            showValidationAlert(errors);
            return false;
        }
        return true;
    }

    private boolean validateAchatForm(TextField oeuvreField, TextField acheteurField, TextField prixField,
                                      DatePicker datePicker, ComboBox<String> statutComboBox) {
        clearValidation(oeuvreField, acheteurField, prixField, datePicker, statutComboBox);
        List<String> errors = new ArrayList<>();

        requireText(oeuvreField, "Le nom de l'oeuvre est obligatoire.", errors);
        requireText(acheteurField, "Le nom de l'acheteur est obligatoire.", errors);
        validatePositiveDouble(prixField, "Le prix doit etre un nombre strictement positif.", errors);
        if (datePicker.getValue() == null) {
            markInvalid(datePicker);
            errors.add("La date d'achat est obligatoire.");
        }
        if (statutComboBox.getValue() == null || statutComboBox.getValue().trim().isEmpty()) {
            markInvalid(statutComboBox);
            errors.add("Le statut est obligatoire.");
        }

        if (!errors.isEmpty()) {
            showValidationAlert(errors);
            return false;
        }
        return true;
    }

    private void requireText(TextField field, String message, List<String> errors) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            markInvalid(field);
            errors.add(message);
        }
    }

    private void requireText(TextArea field, String message, List<String> errors) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            markInvalid(field);
            errors.add(message);
        }
    }

    private void validatePositiveDouble(TextField field, String message, List<String> errors) {
        try {
            if (Double.parseDouble(field.getText().trim()) <= 0) {
                markInvalid(field);
                errors.add(message);
            }
        } catch (Exception ex) {
            markInvalid(field);
            errors.add(message);
        }
    }

    private void validateNonNegativeInt(TextField field, String message, List<String> errors) {
        try {
            if (Integer.parseInt(field.getText().trim()) < 0) {
                markInvalid(field);
                errors.add(message);
            }
        } catch (Exception ex) {
            markInvalid(field);
            errors.add(message);
        }
    }

    private void validateImagePath(TextField field, String message, List<String> errors) {
        String value = field.getText();
        if (value == null || value.trim().isEmpty()) {
            markInvalid(field);
            errors.add(message);
            return;
        }
        File file = new File(value.trim());
        if (!file.exists() || !file.isFile()) {
            markInvalid(field);
            errors.add("Le fichier image selectionne est introuvable.");
        }
    }

    private void clearValidation(Node... nodes) {
        for (Node node : nodes) {
            node.getStyleClass().remove("field-error");
        }
    }

    private void markInvalid(Node node) {
        if (!node.getStyleClass().contains("field-error")) {
            node.getStyleClass().add("field-error");
        }
    }

    private void showValidationAlert(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        styleDialog(alert.getDialogPane());
        alert.getDialogPane().getStyleClass().add("validation-dialog");
        alert.setTitle("Controle de saisie");
        alert.setHeaderText("Veuillez corriger les champs suivants");
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }

    private boolean confirmAction(String title, String header, String content) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        styleDialog(confirm.getDialogPane());
        confirm.getDialogPane().getStyleClass().add("confirm-dialog");
        confirm.setTitle(title);
        confirm.setHeaderText(header);
        confirm.setContentText(content);
        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        styleDialog(alert.getDialogPane());
        alert.getDialogPane().getStyleClass().add("success-dialog");
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        styleDialog(alert.getDialogPane());
        alert.getDialogPane().getStyleClass().add("error-dialog");
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content == null || content.trim().isEmpty() ? "Une erreur est survenue." : content);
        alert.showAndWait();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String formatPrice(double price) {
        return String.format("%.2f DT", price);
    }

    private String stockText(Vente vente) {
        if (vente.getQuantite() <= 0) {
            return "Article epuise";
        }
        return "Quantite disponible: " + vente.getQuantite();
    }

    private void styleDialog(DialogPane pane) {
        pane.getStyleClass().add("dialog-theme");
        pane.getStylesheets().add(getClass().getResource("/styles/scene-builder.css").toExternalForm());
    }

    private Node createArtworkPreview(Vente vente, double width, double height, String styleClass) {
        ImageView imageView = new ImageView(createPreviewImage(vente.getImagePath(), vente.getTitre(), vente.getCategorie(), (int) width, (int) height));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        Label title = new Label(safeText(vente.getCategorie(), "Article"));
        title.getStyleClass().add("image-caption");

        StackPane preview = new StackPane(imageView, title);
        preview.getStyleClass().add(styleClass);
        preview.setPrefSize(width, height);
        StackPane.setAlignment(title, Pos.BOTTOM_LEFT);
        StackPane.setMargin(title, new Insets(0, 0, 12, 12));
        return preview;
    }

    private Image createPreviewImage(String imagePath, String title, String category, int width, int height) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File file = new File(imagePath.trim());
            if (file.exists() && file.isFile()) {
                return new Image(file.toURI().toString(), width, height, false, true);
            }
        }
        return createArtworkImage(safeText(title, "Article"), safeText(category, "Catalogue"), width, height);
    }

    private WritableImage createArtworkImage(String title, String category, int width, int height) {
        WritableImage image = new WritableImage(width, height);
        javafx.scene.image.PixelWriter writer = image.getPixelWriter();
        Color[] palette = paletteFor(category);

        for (int y = 0; y < height; y++) {
            double vertical = (double) y / Math.max(1, height - 1);
            for (int x = 0; x < width; x++) {
                double horizontal = (double) x / Math.max(1, width - 1);
                Color base = palette[0].interpolate(palette[1], (vertical + horizontal) / 2.0);
                double wave = (Math.sin((x + title.length() * 11) * 0.045) + Math.cos((y + category.length() * 17) * 0.05)) * 0.08;
                Color accent = base.interpolate(palette[2], Math.max(0, Math.min(0.38, wave + 0.18)));
                writer.setColor(x, y, accent);
            }
        }

        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(width, height);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(image, 0, 0);
        gc.setGlobalAlpha(0.28);
        gc.setFill(Color.WHITE);
        gc.fillOval(width * 0.58, height * -0.08, width * 0.48, height * 0.62);
        gc.setFill(palette[2]);
        gc.fillOval(width * -0.12, height * 0.50, width * 0.42, height * 0.62);
        gc.setGlobalAlpha(1);
        gc.setStroke(Color.rgb(255, 255, 255, 0.58));
        gc.setLineWidth(3);
        gc.strokeRoundRect(14, 14, width - 28, height - 28, 18, 18);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    private Color[] paletteFor(String category) {
        int bucket = Math.abs(safeText(category, "Catalogue").hashCode()) % 5;
        if (bucket == 0) {
            return new Color[]{Color.web("#2f6f9f"), Color.web("#b9d8ef"), Color.web("#f7b267")};
        }
        if (bucket == 1) {
            return new Color[]{Color.web("#13795b"), Color.web("#a7e0ce"), Color.web("#f3d36b")};
        }
        if (bucket == 2) {
            return new Color[]{Color.web("#b94634"), Color.web("#f8c0aa"), Color.web("#5f7dbb")};
        }
        if (bucket == 3) {
            return new Color[]{Color.web("#5a50a8"), Color.web("#c8c0f4"), Color.web("#ffb86b")};
        }
        return new Color[]{Color.web("#334155"), Color.web("#d3e3f3"), Color.web("#e85d48")};
    }

    private class VenteAdminCell extends ListCell<Vente> {
        @Override
        protected void updateItem(Vente vente, boolean empty) {
            super.updateItem(vente, empty);
            if (empty || vente == null) {
                setGraphic(null);
                return;
            }

            Node thumb = createArtworkPreview(vente, 72, 62, "admin-thumb");

            Label title = new Label(safeText(vente.getTitre(), "Sans titre"));
            title.getStyleClass().add("list-title");

            Label meta = new Label(safeText(vente.getCategorie(), "Article") + " | " + safeText(vente.getNomArtiste(), "Artiste inconnu"));
            meta.getStyleClass().add("muted-label");

            Label description = new Label(safeText(vente.getDescription(), "Aucune description."));
            description.getStyleClass().add("list-description");
            description.setWrapText(true);

            Label price = new Label(formatPrice(vente.getPrix()));
            price.getStyleClass().add("price-label");

            Label stock = new Label(stockText(vente));
            stock.getStyleClass().add(vente.getQuantite() <= 0 ? "stock-empty" : "stock-label");

            VBox text = new VBox(4, title, meta, description, stock);
            HBox row = new HBox(12, thumb, text, price);
            row.setPadding(new Insets(8));
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            HBox.setHgrow(text, javafx.scene.layout.Priority.ALWAYS);
            setGraphic(row);
        }
    }

    private class CartCell extends ListCell<Vente> {
        @Override
        protected void updateItem(Vente vente, boolean empty) {
            super.updateItem(vente, empty);
            if (empty || vente == null) {
                setGraphic(null);
                return;
            }

            Node thumb = createArtworkPreview(vente, 78, 66, "admin-thumb");

            Label title = new Label(safeText(vente.getTitre(), "Article"));
            title.getStyleClass().add("list-title");

            Label meta = new Label(safeText(vente.getCategorie(), "Categorie") + " | " + safeText(vente.getNomArtiste(), "Artiste inconnu"));
            meta.getStyleClass().add("muted-label");

            Label pending = new Label("Sera envoye en statut: En attente");
            pending.getStyleClass().add("status-pending");

            Label price = new Label(formatPrice(vente.getPrix()));
            price.getStyleClass().add("price-label");

            VBox text = new VBox(5, title, meta, pending);
            HBox row = new HBox(12, thumb, text, price);
            row.setPadding(new Insets(8));
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(text, javafx.scene.layout.Priority.ALWAYS);
            setGraphic(row);
        }
    }

    private class AchatAdminCell extends ListCell<Achat> {
        @Override
        protected void updateItem(Achat achat, boolean empty) {
            super.updateItem(achat, empty);
            if (empty || achat == null) {
                setGraphic(null);
                return;
            }

            Label title = new Label(safeText(achat.getNomOeuvre(), "Oeuvre"));
            title.getStyleClass().add("list-title");

            Label buyer = new Label("Acheteur: " + safeText(achat.getNomAcheteur(), "Client inconnu"));
            buyer.getStyleClass().add("muted-label");

            Label date = new Label(achat.getDateAchat() == null ? "Date non definie" : achat.getDateAchat().toString());
            date.getStyleClass().add("list-description");

            Label status = new Label("Statut: " + safeText(achat.getStatut(), "En attente"));
            status.getStyleClass().add(statusStyleFor(achat.getStatut()));

            Label price = new Label(formatPrice(achat.getPrix()));
            price.getStyleClass().add("price-label");

            VBox text = new VBox(4, title, buyer, date, status);
            HBox row = new HBox(12, text, price);
            row.setPadding(new Insets(8));
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            HBox.setHgrow(text, javafx.scene.layout.Priority.ALWAYS);
            setGraphic(row);
        }
    }

    private String statusStyleFor(String statut) {
        if ("Confirme".equals(statut)) {
            return "status-confirmed";
        }
        if ("Refuse".equals(statut)) {
            return "status-refused";
        }
        return "status-pending";
    }
}
