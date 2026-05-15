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
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import org.example.entities.MarketplaceAchat;
import org.example.entities.MarketplaceVente;
import org.example.services.MarketplaceCurrencyApiService;
import org.example.services.MarketplaceInvoicePdfService;
import org.example.services.MarketplaceMetMuseumApiService;
import org.example.services.MarketplaceQrCodeApiService;
import org.example.services.MarketplaceRatingService;
import org.example.services.MarketplaceServiceAchat;
import org.example.services.MarketplaceServiceVente;
import org.example.services.MarketplaceStripePaymentService;
import org.example.utils.MarketplaceMyDataBase;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArteviaMarketplaceController {

    private static final String MODE_ADMIN = "Admin";
    private static final String MODE_CLIENT = "Client";
    private static final String CURRENT_CUSTOMER_ID = "Client test";

    private final MarketplaceServiceVente serviceVente = new MarketplaceServiceVente();
    private final MarketplaceServiceAchat serviceAchat = new MarketplaceServiceAchat();
    private final MarketplaceRatingService ratingService = new MarketplaceRatingService();
    private final MarketplaceMetMuseumApiService metMuseumApiService = new MarketplaceMetMuseumApiService();
    private final MarketplaceQrCodeApiService qrCodeApiService = new MarketplaceQrCodeApiService();
    private final MarketplaceCurrencyApiService currencyApiService = new MarketplaceCurrencyApiService();
    private final MarketplaceInvoicePdfService invoicePdfService = new MarketplaceInvoicePdfService();
    private final MarketplaceStripePaymentService stripePaymentService = new MarketplaceStripePaymentService();
    private final HttpClient talonHttpClient = HttpClient.newHttpClient();
    private final ObservableList<MarketplaceVente> ventesData = FXCollections.observableArrayList();
    private final ObservableList<MarketplaceAchat> achatsData = FXCollections.observableArrayList();
    private final ObservableList<MarketplaceVente> cartData = FXCollections.observableArrayList();
    private final FilteredList<MarketplaceVente> filteredVentesData = new FilteredList<>(ventesData, vente -> true);
    private final FilteredList<MarketplaceAchat> filteredAchatsData = new FilteredList<>(achatsData, achat -> true);
    private String appliedCouponCode = "";
    private double appliedDiscountRate;
    private MarketplaceStripePaymentService.CheckoutSessionResult pendingStripeSession;
    private String pendingStripePaymentRef;
    private double pendingStripeAmount;

    @FXML private ComboBox<String> modeComboBox;
    @FXML private VBox adminView;
    @FXML private VBox adminArticlesView;
    @FXML private VBox adminAchatsView;
    @FXML private VBox clientView;
    @FXML private VBox clientCatalogView;
    @FXML private VBox clientCartView;
    @FXML private Button venteNavButton;
    @FXML private Button achatNavButton;
    @FXML private Button adminArticlesButton;
    @FXML private Button adminAchatsButton;
    @FXML private Button clientCatalogButton;
    @FXML private Button clientCartButton;
    @FXML private ListView<MarketplaceVente> ventesListView;
    @FXML private ListView<MarketplaceAchat> achatsListView;
    @FXML private ListView<MarketplaceVente> cartListView;
    @FXML private TilePane clientGrid;
    @FXML private Label cartTotalLabel;
    @FXML private TextField couponCodeField;
    @FXML private Label cartSubtotalLabel;
    @FXML private Label cartDiscountLabel;
    @FXML private Label couponStatusLabel;
    @FXML private Label stripePaymentStatusLabel;
    @FXML private Label currencyConversionLabel;
    @FXML private Label clientLoyaltyPointsLabel;
    @FXML private Label clientVipLevelLabel;
    @FXML private Label loyaltyAdminSummaryLabel;
    @FXML private ListView<String> loyaltyHistoryListView;
    @FXML private TextField referralFriendField;
    @FXML private TextField adminBonusCustomerField;
    @FXML private TextField adminBonusPointsField;
    @FXML private TextField adminArticleSearchField;
    @FXML private ComboBox<String> adminStockFilterComboBox;
    @FXML private TextField achatSearchField;
    @FXML private ComboBox<String> achatStatusFilterComboBox;
    @FXML private TextField clientSearchField;
    @FXML private ComboBox<String> clientCategoryFilterComboBox;

    @FXML
    public void initialize() {
        if (modeComboBox != null) {
            modeComboBox.setItems(FXCollections.observableArrayList(MODE_ADMIN, MODE_CLIENT));
            modeComboBox.getSelectionModel().select(MODE_ADMIN);
            modeComboBox.valueProperty().addListener((obs, oldMode, newMode) -> updateMode(newMode));
        }

        ventesListView.setItems(filteredVentesData);
        achatsListView.setItems(filteredAchatsData);
        cartListView.setItems(cartData);
        ventesListView.setCellFactory(list -> new VenteAdminCell());
        achatsListView.setCellFactory(list -> new AchatAdminCell());
        cartListView.setCellFactory(list -> new CartCell());
        cartData.addListener((javafx.collections.ListChangeListener<MarketplaceVente>) change -> updateCartTotal());
        setupFilters();
        ensureSimpleLoyaltyTable();

        seedDemoData();
        refreshVentes();
        refreshAchats();
        refreshLoyaltyUi();
        showAdminArticles();
        showClientCatalog();
        updateMode(MODE_ADMIN);
    }

    public void showVenteFromMainSidebar() {
        updateMode(MODE_ADMIN);
    }

    public void showAchatFromMainSidebar() {
        updateMode(MODE_CLIENT);
    }

    @FXML
    private void onShowVenteSpace() {
        updateMode(MODE_ADMIN);
    }

    @FXML
    private void onShowAchatSpace() {
        updateMode(MODE_CLIENT);
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
        MarketplaceVente selected = ventesListView.getSelectionModel().getSelectedItem();
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
        MarketplaceVente selected = ventesListView.getSelectionModel().getSelectedItem();
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
    private void onImportMetMuseumArticle() {
        TextInputDialog dialog = new TextInputDialog("painting");
        styleDialog(dialog.getDialogPane());
        dialog.setTitle("Import MET Museum");
        dialog.setHeaderText("Importer une oeuvre depuis l'API MET Museum");
        dialog.setContentText("Mot-cle de recherche");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        try {
            MarketplaceVente imported = metMuseumApiService.importFirstArtwork(result.get());
            serviceVente.ajouter(imported);
            refreshVentes();
            showSuccess("Import MET Museum", "Oeuvre importee: " + imported.getTitre());
        } catch (Exception ex) {
            showError("Import MET Museum", ex.getMessage());
        }
    }

    @FXML
    private void onAddAchat() {
        openAchatDialog(null).ifPresent(achat -> {
            try {
                serviceAchat.ajouter(achat);
                refreshAchats();
                showSuccess("MarketplaceAchat ajoute", "L'achat a ete ajoute avec succes.");
            } catch (Exception ex) {
                showError("Erreur ajout achat", ex.getMessage());
            }
        });
    }

    @FXML
    private void onDownloadInvoicePdf() {
        MarketplaceAchat selected = achatsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un achat pour generer sa facture PDF.");
            return;
        }
        try {
            java.nio.file.Path path = invoicePdfService.generateInvoice(selected);
            showSuccess("Facture PDF", "Facture generee avec succes:\n" + path);
        } catch (Exception ex) {
            showError("Facture PDF", ex.getMessage());
        }
    }

    @FXML
    private void onEditAchat() {
        MarketplaceAchat selected = achatsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un achat a modifier.");
            return;
        }
        openAchatDialog(selected).ifPresent(achat -> {
            try {
                serviceAchat.modifier(achat);
                refreshAchats();
                showSuccess("MarketplaceAchat modifie", "L'achat a ete modifie avec succes.");
            } catch (Exception ex) {
                showError("Erreur modification achat", ex.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteAchat() {
        MarketplaceAchat selected = achatsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selection obligatoire", "Selectionnez un achat a supprimer.");
            return;
        }
        if (confirmAction("Confirmation", "Supprimer l'achat", "Confirmer la suppression de: " + selected.getNomOeuvre() + " ?")) {
            try {
                serviceAchat.supprimer(selected.getId());
                refreshAchats();
                showSuccess("MarketplaceAchat supprime", "L'achat a ete supprime avec succes.");
            } catch (Exception ex) {
                showError("Erreur suppression achat", ex.getMessage());
            }
        }
    }

    @FXML
    private void onConfirmAchat() {
        MarketplaceAchat selected = achatsListView.getSelectionModel().getSelectedItem();
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
            showSuccess("MarketplaceAchat accepte", "L'achat a ete confirme avec succes.");
        } catch (Exception ex) {
            showError("Erreur confirmation achat", ex.getMessage());
        }
    }

    @FXML
    private void onRefuseAchat() {
        MarketplaceAchat selected = achatsListView.getSelectionModel().getSelectedItem();
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
            showSuccess("MarketplaceAchat refuse", "L'achat a ete refuse avec succes.");
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
        refreshLoyaltyUi();
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
        refreshLoyaltyUi();
        showClientCart();
    }

    @FXML
    private void onAddReferralBonus() {
        String friendId = referralFriendField == null ? "" : referralFriendField.getText().trim();
        if (friendId.isEmpty()) {
            showError("Parrainage", "Saisissez l'identifiant de l'ami a parrainer.");
            return;
        }
        if (CURRENT_CUSTOMER_ID.equalsIgnoreCase(friendId)) {
            showError("Parrainage", "Un client ne peut pas se parrainer lui-meme.");
            return;
        }
        try {
            String externalRef = "REF-" + CURRENT_CUSTOMER_ID + "-" + friendId;
            addLoyaltyPoints(CURRENT_CUSTOMER_ID, 150, "REFERRAL", "Bonus parrainage de " + friendId, externalRef);
            addLoyaltyPoints(friendId, 75, "REFERRAL", "Bonus filleul par " + CURRENT_CUSTOMER_ID, externalRef + "-FRIEND");
            callTalonOneMock(CURRENT_CUSTOMER_ID, "referral", 0, "");
            refreshLoyaltyUi();
            showSuccess("Parrainage valide", "Bonus attribue: +150 pts parrain, +75 pts filleul.");
        } catch (Exception ex) {
            showError("Parrainage", ex.getMessage());
        }
    }

    @FXML
    private void onAddManualLoyaltyBonus() {
        String customerId = adminBonusCustomerField == null ? "" : adminBonusCustomerField.getText().trim();
        String pointsText = adminBonusPointsField == null ? "" : adminBonusPointsField.getText().trim();
        if (customerId.isEmpty()) {
            customerId = CURRENT_CUSTOMER_ID;
        }
        try {
            int points = Integer.parseInt(pointsText);
            if (points <= 0) {
                throw new IllegalArgumentException("Le bonus doit etre strictement positif.");
            }
            addLoyaltyPoints(customerId, points, "ADMIN_BONUS", "Bonus manuel admin", null);
            callTalonOneMock(customerId, "manual_bonus", 0, "");
            refreshLoyaltyUi();
            showSuccess("Bonus fidelite", points + " points ajoutes a " + customerId + ".");
        } catch (Exception ex) {
            showError("Bonus fidelite", ex.getMessage());
        }
    }

    @FXML
    private void onRemoveFromCart() {
        MarketplaceVente selected = cartListView.getSelectionModel().getSelectedItem();
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
            clearCoupon();
        }
    }

    @FXML
    private void onApplyCoupon() {
        if (cartData.isEmpty()) {
            showError("Panier vide", "Ajoutez au moins un article avant d'appliquer un coupon.");
            return;
        }
        String code = couponCodeField == null ? "" : couponCodeField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            clearCoupon();
            showSuccess("Coupon retire", "Aucun coupon n'est applique au panier.");
            return;
        }
        Double rate = couponRateFor(code);
        if (rate == null) {
            appliedCouponCode = "";
            appliedDiscountRate = 0;
            updateCartTotal();
            showError("Coupon invalide", "Codes disponibles pour le test: ARTEVIA10, VIP20, WELCOME5.");
            return;
        }
        appliedCouponCode = code;
        appliedDiscountRate = rate;
        updateCartTotal();
        showSuccess("Coupon applique", code + " applique: -" + Math.round(rate * 100) + "%.");
    }

    @FXML
    private void onConvertCartCurrency() {
        double total = cartFinalTotal();
        if (total <= 0) {
            showError("Conversion devise", "Le total du panier doit etre superieur a 0.");
            return;
        }
        MarketplaceCurrencyApiService.CurrencyResult result = currencyApiService.convertFromTnd(total);
        String suffix = result.isFallback() ? " (mode secours)" : " (API live)";
        if (currencyConversionLabel != null) {
            currencyConversionLabel.setText(String.format("EUR %.2f | USD %.2f%s", result.getEur(), result.getUsd(), suffix));
        }
    }

    @FXML
    private void onCheckStripeConfiguration() {
        MarketplaceStripePaymentService.StripeConfigurationResult result =
                stripePaymentService.verifyStripeConfiguration();

        updateStripePaymentStatus(result.getMessage());

        if (result.isValid()) {
            showSuccess("Stripe", result.getMessage());
        } else {
            showError("Stripe", result.getMessage());
        }
    }

    @FXML
    private void onPayWithStripe() {
        if (cartData.isEmpty()) {
            showError("Panier vide", "Ajoutez au moins un article avant de payer avec Stripe.");
            return;
        }
        refreshVentes();
        String stockError = validateCartStock();
        if (stockError != null) {
            showError("Stock insuffisant", stockError);
            return;
        }
        double finalTotal = cartFinalTotal();
        if (finalTotal <= 0) {
            showError("Montant invalide", "Le montant du paiement doit etre superieur a 0 DT.");
            return;
        }

        if (!stripePaymentService.isConfigured()) {
            updateStripePaymentStatus("Mode mock actif");
            showError("Configuration paiement manquante", "Configuration paiement manquante. Mode mock active.");
            return;
        }

        try {
            String paymentRef = "STRIPE-" + System.currentTimeMillis();
            if (!showStripeCheckoutConfirmation(finalTotal, paymentRef)) {
                updateStripePaymentStatus("Stripe ANNULE");
                return;
            }
            MarketplaceStripePaymentService.CheckoutSessionResult session = stripePaymentService.createCheckoutSession(
                    finalTotal,
                    "Artevia Marketplace - Panier",
                    paymentRef
            );
            if (session.getCheckoutUrl() == null || session.getCheckoutUrl().trim().isEmpty()) {
                updateStripePaymentStatus("Stripe URL vide | ECHOUE");
                showError("Paiement Stripe", "Stripe n'a retourne aucune URL Checkout.");
                return;
            }

            pendingStripeSession = session;
            pendingStripePaymentRef = paymentRef;
            pendingStripeAmount = finalTotal;
            saveStripePayment(paymentRef, session, finalTotal, "EN_ATTENTE");
            updateStripePaymentStatus("Stripe EN_ATTENTE | " + session.getSessionId());
            openExternalBrowser(session.getCheckoutUrl());
            showSuccess("Stripe Checkout", "La page Stripe Checkout est ouverte dans le navigateur.\nApres le paiement, cliquez sur Verifier Stripe.");
        } catch (Exception ex) {
            updateStripePaymentStatus("Stripe ECHOUE");
            showError("Paiement Stripe impossible", ex.getMessage());
        }
    }

    private boolean showStripeCheckoutConfirmation(double amount, String paymentRef) {
        Dialog<Boolean> dialog = new Dialog<>();
        styleDialog(dialog.getDialogPane());
        dialog.setTitle("Paiement Stripe");
        dialog.setHeaderText("Confirmation du paiement Stripe");

        Label subtitle = new Label("Securise par Stripe");
        subtitle.getStyleClass().add("muted-label");

        Label summaryTitle = new Label("Recapitulatif");
        summaryTitle.getStyleClass().add("panel-title");

        Label amountLabel = new Label(formatPrice(amount));
        amountLabel.getStyleClass().add("cart-total");

        Label orderLabel = new Label("#" + paymentRef.replace("STRIPE-", ""));
        orderLabel.getStyleClass().add("summary-value");

        GridPane summary = new GridPane();
        summary.setHgap(18);
        summary.setVgap(12);
        summary.setPadding(new Insets(16));
        summary.getStyleClass().add("cart-summary");
        summary.add(new Label("Montant total"), 0, 0);
        summary.add(amountLabel, 1, 0);
        summary.add(new Label("Commande N°"), 0, 1);
        summary.add(orderLabel, 1, 1);

        VBox content = new VBox(14, subtitle, summaryTitle, summary);
        content.setPadding(new Insets(8, 4, 4, 4));
        dialog.getDialogPane().setContent(content);

        ButtonType payType = new ButtonType("Payer avec Stripe", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payType, ButtonType.CANCEL);
        Button payButton = (Button) dialog.getDialogPane().lookupButton(payType);
        payButton.getStyleClass().add("primary-btn");

        dialog.setResultConverter(button -> button == payType);
        return dialog.showAndWait().orElse(false);
    }

    @FXML
    private void onVerifyStripePayment() {
        if (pendingStripeSession == null || pendingStripeSession.getSessionId() == null) {
            showError("Stripe", "Aucune session Stripe en attente. Lancez d'abord un paiement Stripe.");
            return;
        }

        try {
            MarketplaceStripePaymentService.CheckoutSessionResult session =
                    stripePaymentService.retrieveCheckoutSession(pendingStripeSession.getSessionId());
            String appStatus = stripeAppStatus(session);
            updateStripePayment(pendingStripePaymentRef, appStatus);
            updateStripePaymentStatus("Stripe " + appStatus + " | " + session.getPaymentStatus());

            if ("PAYE".equals(appStatus)) {
                processPaidCart(pendingStripePaymentRef, pendingStripeAmount);
                pendingStripeSession = null;
                pendingStripePaymentRef = null;
                pendingStripeAmount = 0;
                showSuccess("Paiement confirme", "Stripe confirme le paiement. La commande est marquee PAYE.");
            } else if ("ANNULE".equals(appStatus)) {
                showError("Paiement annule", "La session Stripe est expiree ou annulee.");
            } else {
                showError("Paiement en attente", "Stripe n'a pas encore confirme le paiement. Statut: " + session.getPaymentStatus());
            }
        } catch (Exception ex) {
            if (pendingStripePaymentRef != null) {
                try {
                    updateStripePayment(pendingStripePaymentRef, "ECHOUE");
                } catch (SQLException sqlException) {
                    System.out.println("Impossible de mettre a jour le statut Stripe: " + sqlException.getMessage());
                }
            }
            updateStripePaymentStatus("Stripe ECHOUE");
            showError("Verification Stripe impossible", ex.getMessage());
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
            double finalTotal = cartFinalTotal();
            String loyaltySessionRef = "ORDER-" + System.currentTimeMillis();
            for (MarketplaceVente vente : new ArrayList<>(cartData)) {
                serviceVente.diminuerQuantite(vente.getId());
                serviceAchat.ajouter(new MarketplaceAchat(
                        safeText(vente.getTitre(), "Article"),
                        CURRENT_CUSTOMER_ID,
                        vente.getPrix(),
                        Date.valueOf(LocalDate.now()),
                        "En attente"
                ));
            }
            int earnedPoints = calculateCashbackPoints(finalTotal);
            addLoyaltyPoints(CURRENT_CUSTOMER_ID, earnedPoints, "PURCHASE", "Cashback automatique sur achat", loyaltySessionRef);
            callTalonOneMock(CURRENT_CUSTOMER_ID, "purchase", finalTotal, appliedCouponCode);
            String couponMessage = appliedCouponCode == null || appliedCouponCode.isEmpty()
                    ? ""
                    : "\nCoupon applique: " + appliedCouponCode + " | Total final: " + formatPrice(cartFinalTotal());
            cartData.clear();
            clearCoupon();
            refreshVentes();
            refreshAchats();
            refreshLoyaltyUi();
            showSuccess("Commande en attente", "Votre panier a ete envoye a l'admin pour confirmation.\nPoints fidelite gagnes: +" + earnedPoints + couponMessage);
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
        setSidebarNavButton(venteNavButton, admin);
        setSidebarNavButton(achatNavButton, !admin);
        if (modeComboBox != null && mode != null && !mode.equals(modeComboBox.getValue())) {
            modeComboBox.getSelectionModel().select(mode);
        }
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

    private void setSidebarNavButton(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.getStyleClass().removeAll("side-nav-btn", "side-nav-btn-active");
        button.getStyleClass().add(active ? "side-nav-btn-active" : "side-nav-btn");
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
        for (MarketplaceVente vente : ventesData) {
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
        List<MarketplaceVente> visibleArticles = filteredClientArticles();
        if (visibleArticles.isEmpty()) {
            Label empty = new Label("Aucun article ne correspond a votre recherche.");
            empty.getStyleClass().add("empty-label");
            clientGrid.getChildren().add(empty);
            return;
        }
        for (MarketplaceVente vente : visibleArticles) {
            clientGrid.getChildren().add(createArticleCard(vente));
        }
    }

    private List<MarketplaceVente> filteredClientArticles() {
        String query = normalized(clientSearchField == null ? "" : clientSearchField.getText());
        String categoryFilter = clientCategoryFilterComboBox == null ? "Toutes les categories" : clientCategoryFilterComboBox.getValue();
        List<MarketplaceVente> results = new ArrayList<>();
        for (MarketplaceVente vente : ventesData) {
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

    private Node createArticleCard(MarketplaceVente vente) {
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

        Label rating = new Label(ratingText(vente));
        rating.getStyleClass().add("rating-label");

        HBox ratingButtons = new HBox(3);
        ratingButtons.setAlignment(Pos.CENTER_LEFT);
        int currentRating = currentUserRating(vente.getId());
        for (int note = 1; note <= 5; note++) {
            final int selectedNote = note;
            Button star = new Button();
            star.getStyleClass().add("star-btn");
            star.setGraphic(createStarIcon(note <= currentRating));
            star.setOnAction(event -> rateArticle(vente, selectedNote));
            ratingButtons.getChildren().add(star);
        }

        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label(formatPrice(vente.getPrix()));
        price.getStyleClass().add("price-label");
        Button buyButton = new Button(vente.getQuantite() <= 0 ? "Article epuise" : "Ajouter au panier");
        buyButton.getStyleClass().add("primary-btn");
        buyButton.setDisable(vente.getQuantite() <= 0);
        buyButton.setOnAction(event -> addToCart(vente));
        Button qrButton = new Button("QR");
        qrButton.getStyleClass().add("secondary-btn");
        qrButton.setOnAction(event -> showArticleQrCode(vente));
        footer.getChildren().addAll(price, qrButton, buyButton);
        HBox.setHgrow(price, javafx.scene.layout.Priority.ALWAYS);

        if (vente.getQuantite() <= 0) {
            card.getStyleClass().add("article-card-empty");
        }
        card.getChildren().addAll(photo, category, title, artist, description, stock, rating, ratingButtons, footer);
        return card;
    }

    private Polygon createStarIcon(boolean filled) {
        Polygon star = new Polygon();
        double centerX = 8;
        double centerY = 8;
        double outerRadius = 8;
        double innerRadius = 3.5;
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(-90 + i * 36);
            double radius = i % 2 == 0 ? outerRadius : innerRadius;
            star.getPoints().addAll(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius);
        }
        star.getStyleClass().add(filled ? "star-icon-filled" : "star-icon-empty");
        return star;
    }

    private int currentUserRating(int venteId) {
        try {
            return ratingService.noteUtilisateur(venteId, CURRENT_CUSTOMER_ID);
        } catch (Exception ex) {
            return 0;
        }
    }

    private String ratingText(MarketplaceVente vente) {
        try {
            double moyenne = ratingService.moyenne(vente.getId());
            int count = ratingService.nombreNotes(vente.getId());
            return count == 0 ? "Pas encore note" : String.format("Note %.1f/5 (%d avis)", moyenne, count);
        } catch (Exception ex) {
            return "Note indisponible";
        }
    }

    private void rateArticle(MarketplaceVente vente, int note) {
        try {
            ratingService.ajouterOuModifier(vente.getId(), CURRENT_CUSTOMER_ID, note);
            renderClientGrid();
        } catch (Exception ex) {
            showError("Notation", ex.getMessage());
        }
    }

    private void showArticleQrCode(MarketplaceVente vente) {
        Dialog<Void> dialog = new Dialog<>();
        styleDialog(dialog.getDialogPane());
        dialog.setTitle("QR Code article");
        dialog.setHeaderText("QR Code - " + safeText(vente.getTitre(), "Article"));
        ImageView qrView = new ImageView(new Image(qrCodeApiService.createQrCodeUrl(articleQrData(vente)), true));
        qrView.setFitWidth(240);
        qrView.setFitHeight(240);
        qrView.setPreserveRatio(true);
        VBox content = new VBox(12, qrView, new Label("Scannez pour voir les infos de l'article."));
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(360, 380);
        dialog.showAndWait();
    }

    private String articleQrData(MarketplaceVente vente) {
        return safeText(vente.getTitre(), "Article") + " | " +
                safeText(vente.getNomArtiste(), "Artiste inconnu") + " | " +
                formatPrice(vente.getPrix());
    }

    private void addToCart(MarketplaceVente vente) {
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
        for (MarketplaceVente cartItem : cartData) {
            if (cartItem.getId() == venteId) {
                count++;
            }
        }
        return count;
    }

    private String validateCartStock() {
        Map<Integer, Integer> requestedQuantities = new HashMap<>();
        for (MarketplaceVente cartItem : cartData) {
            requestedQuantities.put(cartItem.getId(), requestedQuantities.getOrDefault(cartItem.getId(), 0) + 1);
        }

        for (Map.Entry<Integer, Integer> entry : requestedQuantities.entrySet()) {
            MarketplaceVente current = findVenteById(entry.getKey());
            if (current == null) {
                return "Un article du panier n'est plus disponible.";
            }
            if (current.getQuantite() < entry.getValue()) {
                return "Stock insuffisant pour: " + current.getTitre() + ". Disponible: " + current.getQuantite() + ", dans panier: " + entry.getValue() + ".";
            }
        }
        return null;
    }

    private MarketplaceVente findVenteById(int id) {
        for (MarketplaceVente vente : ventesData) {
            if (vente.getId() == id) {
                return vente;
            }
        }
        return null;
    }

    private void processPaidCart(String paymentRef, double finalTotal) throws SQLException {
        refreshVentes();
        String stockError = validateCartStock();
        if (stockError != null) {
            throw new SQLException(stockError);
        }

        String loyaltySessionRef = "ORDER-" + System.currentTimeMillis();
        for (MarketplaceVente vente : new ArrayList<>(cartData)) {
            serviceVente.diminuerQuantite(vente.getId());
            serviceAchat.ajouter(new MarketplaceAchat(
                    safeText(vente.getTitre(), "Article"),
                    CURRENT_CUSTOMER_ID,
                    vente.getPrix(),
                    Date.valueOf(LocalDate.now()),
                    "PAYE"
            ));
        }

        int earnedPoints = calculateCashbackPoints(finalTotal);
        addLoyaltyPoints(CURRENT_CUSTOMER_ID, earnedPoints, "PURCHASE", "Paiement Stripe confirme", loyaltySessionRef);
        callTalonOneMock(CURRENT_CUSTOMER_ID, "stripe_payment", finalTotal, appliedCouponCode);
        cartData.clear();
        clearCoupon();
        refreshVentes();
        refreshAchats();
        refreshLoyaltyUi();
        showClientCatalog();
    }

    private void saveStripePayment(String paymentRef, MarketplaceStripePaymentService.CheckoutSessionResult session,
                                   double amount, String status) throws SQLException {
        Connection connection = loyaltyConnection();
        try (java.sql.Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS stripe_checkout_payments (" +
                            "id_payment INT AUTO_INCREMENT PRIMARY KEY, " +
                            "payment_ref VARCHAR(120) NOT NULL UNIQUE, " +
                            "stripe_session_id VARCHAR(255) NOT NULL UNIQUE, " +
                            "customer_id VARCHAR(100) NOT NULL, " +
                            "amount DOUBLE NOT NULL, " +
                            "currency VARCHAR(10) NOT NULL, " +
                            "status VARCHAR(20) NOT NULL, " +
                            "checkout_url TEXT, " +
                            "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)"
            );
        }
        String sql = "INSERT INTO stripe_checkout_payments " +
                "(payment_ref, stripe_session_id, customer_id, amount, currency, status, checkout_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentRef);
            ps.setString(2, session.getSessionId());
            ps.setString(3, CURRENT_CUSTOMER_ID);
            ps.setDouble(4, amount);
            ps.setString(5, session.getCurrency());
            ps.setString(6, status);
            ps.setString(7, session.getCheckoutUrl());
            ps.executeUpdate();
        }
    }

    private void updateStripePayment(String paymentRef, String status) throws SQLException {
        Connection connection = loyaltyConnection();
        String sql = "UPDATE stripe_checkout_payments SET status = ? WHERE payment_ref = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, paymentRef);
            ps.executeUpdate();
        }
    }

    private String stripeAppStatus(MarketplaceStripePaymentService.CheckoutSessionResult session) {
        if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
            return "PAYE";
        }
        if ("expired".equalsIgnoreCase(session.getStatus())) {
            return "ANNULE";
        }
        return "EN_ATTENTE";
    }

    private void openExternalBrowser(String url) throws IOException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Impossible d'ouvrir le navigateur automatiquement. URL Stripe: " + url);
        }
        Desktop.getDesktop().browse(URI.create(url));
    }

    private void updateStripePaymentStatus(String status) {
        if (stripePaymentStatusLabel != null) {
            stripePaymentStatusLabel.setText(status);
        }
    }

    private void updateCartTotal() {
        double subtotal = cartSubtotal();
        double discount = cartDiscount();
        double total = cartFinalTotal();
        if (cartSubtotalLabel != null) {
            cartSubtotalLabel.setText(formatPrice(subtotal));
        }
        if (cartDiscountLabel != null) {
            cartDiscountLabel.setText("-" + formatPrice(discount));
        }
        if (cartTotalLabel != null) {
            cartTotalLabel.setText(formatPrice(total));
        }
        if (couponStatusLabel != null) {
            if (appliedCouponCode == null || appliedCouponCode.isEmpty()) {
                couponStatusLabel.setText("Aucun coupon applique");
            } else {
                couponStatusLabel.setText("Coupon " + appliedCouponCode + " applique: -" + Math.round(appliedDiscountRate * 100) + "%");
            }
        }
        if (clientCartButton != null) {
            clientCartButton.setText("Panier (" + cartData.size() + ")");
        }
    }

    private double cartSubtotal() {
        double subtotal = 0;
        for (MarketplaceVente vente : cartData) {
            subtotal += vente.getPrix();
        }
        return subtotal;
    }

    private double cartDiscount() {
        return cartSubtotal() * appliedDiscountRate;
    }

    private double cartFinalTotal() {
        return Math.max(0, cartSubtotal() - cartDiscount());
    }

    private void clearCoupon() {
        appliedCouponCode = "";
        appliedDiscountRate = 0;
        if (couponCodeField != null) {
            couponCodeField.clear();
        }
        updateCartTotal();
    }

    private Double couponRateFor(String code) {
        if ("ARTEVIA10".equals(code)) {
            return 0.10;
        }
        if ("VIP20".equals(code) && cartSubtotal() >= 500) {
            return 0.20;
        }
        if ("WELCOME5".equals(code)) {
            return 0.05;
        }
        if ("POINTS1000".equals(code) && safeLoyaltyPoints(CURRENT_CUSTOMER_ID) >= 1000) {
            return 0.12;
        }
        if ("VIPAUTO".equals(code)) {
            String level = vipLevelForPoints(safeLoyaltyPoints(CURRENT_CUSTOMER_ID));
            if ("VIP".equals(level)) {
                return 0.25;
            }
            if ("Gold".equals(level)) {
                return 0.18;
            }
            if ("Silver".equals(level)) {
                return 0.10;
            }
        }
        return null;
    }

    private void ensureSimpleLoyaltyTable() {
        try (java.sql.Statement st = loyaltyConnection().createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS loyalty_transactions (" +
                            "id_transaction INT AUTO_INCREMENT PRIMARY KEY, " +
                            "customer_id VARCHAR(100) NOT NULL, " +
                            "points INT NOT NULL, " +
                            "type VARCHAR(40) NOT NULL, " +
                            "reason VARCHAR(255), " +
                            "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "external_ref VARCHAR(120) UNIQUE)"
            );
        } catch (Exception ex) {
            showError("Fidelite", "Initialisation fidelite impossible: " + ex.getMessage());
        }
    }

    private void addLoyaltyPoints(String customerId, int points, String type, String reason, String externalRef) throws SQLException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new SQLException("Client fidelite obligatoire.");
        }
        if (points == 0) {
            throw new SQLException("Le nombre de points ne peut pas etre zero.");
        }
        if (externalRef != null && loyaltyExternalRefExists(externalRef)) {
            throw new SQLException("Cette recompense fidelite a deja ete appliquee.");
        }
        String sql = "INSERT INTO loyalty_transactions (customer_id, points, type, reason, created_at, external_ref) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = loyaltyConnection().prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            ps.setInt(2, points);
            ps.setString(3, type);
            ps.setString(4, reason);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(6, externalRef);
            ps.executeUpdate();
        }
    }

    private boolean loyaltyExternalRefExists(String externalRef) throws SQLException {
        String sql = "SELECT id_transaction FROM loyalty_transactions WHERE external_ref = ? LIMIT 1";
        try (PreparedStatement ps = loyaltyConnection().prepareStatement(sql)) {
            ps.setString(1, externalRef);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void refreshLoyaltyUi() {
        int points = safeLoyaltyPoints(CURRENT_CUSTOMER_ID);
        String vip = vipLevelForPoints(points);
        if (clientLoyaltyPointsLabel != null) {
            clientLoyaltyPointsLabel.setText(points + " pts");
        }
        if (clientVipLevelLabel != null) {
            clientVipLevelLabel.setText(vip);
        }
        if (loyaltyAdminSummaryLabel != null) {
            loyaltyAdminSummaryLabel.setText(totalDistributedPoints() + " pts distribues");
        }
        if (loyaltyHistoryListView != null) {
            loyaltyHistoryListView.setItems(FXCollections.observableArrayList(simpleLoyaltyHistory()));
        }
    }

    private int safeLoyaltyPoints(String customerId) {
        try {
            return loyaltyPoints(customerId);
        } catch (Exception ex) {
            return 0;
        }
    }

    private int loyaltyPoints(String customerId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(points), 0) AS total FROM loyalty_transactions WHERE customer_id = ?";
        try (PreparedStatement ps = loyaltyConnection().prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Math.max(0, rs.getInt("total")) : 0;
            }
        }
    }

    private int totalDistributedPoints() {
        String sql = "SELECT COALESCE(SUM(CASE WHEN points > 0 THEN points ELSE 0 END), 0) AS total FROM loyalty_transactions";
        try (PreparedStatement ps = loyaltyConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        } catch (Exception ex) {
            return 0;
        }
    }

    private List<String> simpleLoyaltyHistory() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT customer_id, points, type, reason, created_at FROM loyalty_transactions ORDER BY created_at DESC, id_transaction DESC LIMIT 12";
        try (PreparedStatement ps = loyaltyConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                history.add(rs.getTimestamp("created_at") + " | " + rs.getString("customer_id") + " | " +
                        rs.getInt("points") + " pts | " + rs.getString("type") + " | " + safeText(rs.getString("reason"), ""));
            }
        } catch (Exception ex) {
            history.add("Historique fidelite indisponible: " + ex.getMessage());
        }
        return history;
    }

    private int calculateCashbackPoints(double amount) {
        String vip = vipLevelForPoints(safeLoyaltyPoints(CURRENT_CUSTOMER_ID));
        double rate = "VIP".equals(vip) ? 0.10 : "Gold".equals(vip) ? 0.07 : "Silver".equals(vip) ? 0.05 : 0.03;
        return Math.max(1, (int) Math.round(amount * rate));
    }

    private String vipLevelForPoints(int points) {
        if (points >= 7000) {
            return "VIP";
        }
        if (points >= 3000) {
            return "Gold";
        }
        if (points >= 1000) {
            return "Silver";
        }
        return "Bronze";
    }

    private Connection loyaltyConnection() throws SQLException {
        Connection connection = MarketplaceMyDataBase.getInstance().getConnection();
        if (connection == null) {
            String details = MarketplaceMyDataBase.getLastError() == null ? "" : " Cause: " + MarketplaceMyDataBase.getLastError();
            throw new SQLException("Connexion MySQL indisponible." + details);
        }
        return connection;
    }

    private void callTalonOneMock(String customerId, String eventType, double amount, String couponCode) {
        String baseUrl = System.getenv("TALON_ONE_BASE_URL");
        String apiKey = System.getenv("TALON_ONE_API_KEY");
        if (baseUrl == null || baseUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            return;
        }
        try {
            String sessionId = "marketplace-" + System.currentTimeMillis();
            String body = "{\"customerSession\":{\"profileId\":\"" + json(customerId) + "\",\"state\":\"closed\",\"attributes\":{" +
                    "\"eventType\":\"" + json(eventType) + "\",\"amount\":" + amount + ",\"coupon\":\"" + json(couponCode) + "\"}}}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trimTrailingSlash(baseUrl) + "/v2/customer_sessions/" + sessionId))
                    .header("Authorization", "ApiKey-v1 " + apiKey)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = talonHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("Talon.One API erreur HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (IOException | InterruptedException ex) {
            System.out.println("Talon.One indisponible, mode local conserve: " + ex.getMessage());
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String json(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Optional<MarketplaceVente> openVenteDialog(MarketplaceVente existing) {
        Dialog<MarketplaceVente> dialog = new Dialog<>();
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
                    return new MarketplaceVente(titreField.getText(), descriptionArea.getText(), prix, categorieField.getText(), artisteField.getText(), quantite, imagePath);
                }
                return new MarketplaceVente(existing.getId(), titreField.getText(), descriptionArea.getText(), prix, categorieField.getText(), artisteField.getText(), quantite, imagePath);
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private Optional<MarketplaceAchat> openAchatDialog(MarketplaceAchat existing) {
        Dialog<MarketplaceAchat> dialog = new Dialog<>();
        styleDialog(dialog.getDialogPane());
        dialog.setTitle(existing == null ? "Ajouter MarketplaceAchat" : "Modifier MarketplaceAchat");
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
        addRow(form, 3, "Date MarketplaceAchat", datePicker);
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
                    return new MarketplaceAchat(oeuvreField.getText(), acheteurField.getText(), prix, date, statut);
                }
                return new MarketplaceAchat(existing.getId(), oeuvreField.getText(), acheteurField.getText(), prix, date, statut);
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
        if (isWebImage(value)) {
            return;
        }
        File file = new File(value.trim());
        if (!file.exists() || !file.isFile()) {
            markInvalid(field);
            errors.add("Le fichier image selectionne est introuvable ou l'URL image est invalide.");
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

    private String stockText(MarketplaceVente vente) {
        if (vente.getQuantite() <= 0) {
            return "Article epuise";
        }
        return "Quantite disponible: " + vente.getQuantite();
    }

    private void styleDialog(DialogPane pane) {
        pane.getStyleClass().add("dialog-theme");
        pane.getStylesheets().add(getClass().getResource("/styles/artevia-marketplace.css").toExternalForm());
    }

    private Node createArtworkPreview(MarketplaceVente vente, double width, double height, String styleClass) {
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
            if (isWebImage(imagePath)) {
                return new Image(imagePath.trim(), width, height, false, true, true);
            }
            File file = new File(imagePath.trim());
            if (file.exists() && file.isFile()) {
                return new Image(file.toURI().toString(), width, height, false, true);
            }
        }
        return createArtworkImage(safeText(title, "Article"), safeText(category, "Catalogue"), width, height);
    }

    private boolean isWebImage(String value) {
        String lower = value == null ? "" : value.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
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

    private class VenteAdminCell extends ListCell<MarketplaceVente> {
        @Override
        protected void updateItem(MarketplaceVente vente, boolean empty) {
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

    private class CartCell extends ListCell<MarketplaceVente> {
        @Override
        protected void updateItem(MarketplaceVente vente, boolean empty) {
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

    private class AchatAdminCell extends ListCell<MarketplaceAchat> {
        @Override
        protected void updateItem(MarketplaceAchat achat, boolean empty) {
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
