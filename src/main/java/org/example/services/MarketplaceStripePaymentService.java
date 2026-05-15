package org.example.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Balance;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public class MarketplaceStripePaymentService {

    private static final Set<String> ZERO_DECIMAL_CURRENCIES = Set.of(
            "bif", "clp", "djf", "gnf", "jpy", "kmf", "krw", "mga",
            "pyg", "rwf", "ugx", "vnd", "vuv", "xaf", "xof", "xpf"
    );
    private static final Set<String> THREE_DECIMAL_CURRENCIES = Set.of(
            "bhd", "jod", "kwd", "omr", "tnd"
    );

    private final Properties appProperties = new Properties();
    private final Properties envFileProperties = new Properties();

    public MarketplaceStripePaymentService() {
        loadApplicationProperties();
        loadEnvFile();
        logStripeConfiguration();
    }

    public boolean isConfigured() {
        return loadStripeConfig().isValidSecretKey();
    }

    public String configuredCurrency() {
        String currency = getConfig("STRIPE_CURRENCY", "usd");
        if (currency == null || currency.trim().isEmpty()) {
            return "usd";
        }
        return currency.trim().toLowerCase(Locale.ROOT);
    }

    public StripeConfigurationResult verifyStripeConfiguration() {
        StripeConfig config = loadStripeConfig();
        logStripeConfiguration();

        if (config.isBlank()) {
            return StripeConfigurationResult.failure("Configuration paiement manquante. Mode mock active.");
        }

        if (!config.isValidSecretKey()) {
            return StripeConfigurationResult.failure("Cle Stripe invalide. Utilisez une cle secrete sk_test_..., pas pk_test_...");
        }

        try {
            Stripe.apiKey = config.secretKey;
            Balance.retrieve();
            return StripeConfigurationResult.success("Stripe configure correctement.");
        } catch (StripeException e) {
            return StripeConfigurationResult.failure("Cle Stripe invalide ou connexion Stripe impossible : " + e.getMessage());
        } catch (Exception e) {
            return StripeConfigurationResult.failure("Connexion Stripe impossible. Mode mock active : " + e.getMessage());
        }
    }

    public CheckoutSessionResult createCheckoutSession(double amount, String orderName, String paymentRef)
            throws StripeException {
        StripeConfig config = loadStripeConfig();
        logStripeConfiguration();

        if (config.isBlank()) {
            throw new IllegalStateException("Configuration paiement manquante. Mode mock active.");
        }

        if (!config.isValidSecretKey()) {
            throw new IllegalStateException("Cle Stripe invalide. Utilisez une cle secrete sk_test_..., pas pk_test_...");
        }

        String currency = configuredCurrency();
        long unitAmount = toMinorUnits(amount, currency);
        String successUrl = getConfig("STRIPE_SUCCESS_URL", "https://example.com/artevia/stripe/success");
        String cancelUrl = getConfig("STRIPE_CANCEL_URL", "https://example.com/artevia/stripe/cancel");

        Stripe.apiKey = config.secretKey;
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(unitAmount)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(orderName)
                                        .build())
                                .build())
                        .build())
                .putMetadata("payment_ref", paymentRef)
                .putMetadata("source", "Artevia Marketplace JavaFX")
                .build();

        Session session = Session.create(params);
        System.out.println("Stripe Checkout session creee: id=" + session.getId()
                + ", status=" + session.getStatus()
                + ", payment_status=" + session.getPaymentStatus()
                + ", url=" + session.getUrl());
        return new CheckoutSessionResult(session.getId(), session.getUrl(), session.getStatus(),
                session.getPaymentStatus(), currency);
    }

    public CheckoutSessionResult retrieveCheckoutSession(String sessionId) throws StripeException {
        StripeConfig config = loadStripeConfig();
        logStripeConfiguration();

        if (config.isBlank()) {
            throw new IllegalStateException("Configuration paiement manquante. Mode mock active.");
        }

        if (!config.isValidSecretKey()) {
            throw new IllegalStateException("Cle Stripe invalide. Utilisez une cle secrete sk_test_..., pas pk_test_...");
        }

        Stripe.apiKey = config.secretKey;
        Session session = Session.retrieve(sessionId);
        System.out.println("Stripe Checkout session verifiee: id=" + session.getId()
                + ", status=" + session.getStatus()
                + ", payment_status=" + session.getPaymentStatus());
        return new CheckoutSessionResult(session.getId(), session.getUrl(), session.getStatus(),
                session.getPaymentStatus(), configuredCurrency());
    }

    private StripeConfig loadStripeConfig() {
        return new StripeConfig(getConfig("STRIPE_SECRET_KEY", ""));
    }

    private void logStripeConfiguration() {
        StripeConfig config = loadStripeConfig();
        System.out.println("[STRIPE DEBUG] STRIPE_SECRET_KEY is null/empty = " + config.isBlank());
        System.out.println("[STRIPE DEBUG] STRIPE_SECRET_KEY starts with sk_test_ = " + config.startsWithTestSecretPrefix());
        System.out.println("[STRIPE DEBUG] STRIPE_SECRET_KEY starts with pk_test_ = " + config.startsWithPublishablePrefix());
        System.out.println("[STRIPE DEBUG] STRIPE_CURRENCY = " + configuredCurrency());
        System.out.println("[STRIPE DEBUG] Mode mock active = " + !config.isValidSecretKey());
    }

    private String getConfig(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (!isBlank(systemValue)) {
            return cleanValue(systemValue);
        }

        String envValue = System.getenv(key);
        if (!isBlank(envValue)) {
            return cleanValue(envValue);
        }

        String envFileValue = envFileProperties.getProperty(key);
        if (!isBlank(envFileValue)) {
            return cleanValue(envFileValue);
        }

        String propertyValue = appProperties.getProperty(key);
        if (!isBlank(propertyValue)) {
            return cleanValue(propertyValue);
        }

        return defaultValue;
    }

    private void loadApplicationProperties() {
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            if (inputStream != null) {
                appProperties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("[STRIPE DEBUG] Impossible de lire application.properties : " + e.getMessage());
        }
    }

    private void loadEnvFile() {
        Path[] candidates = {
                Path.of(".env"),
                Path.of(System.getProperty("user.dir"), ".env")
        };

        for (Path envPath : candidates) {
            if (Files.exists(envPath)) {
                readEnvFile(envPath);
                return;
            }
        }
    }

    private void readEnvFile(Path envPath) {
        try {
            for (String line : Files.readAllLines(envPath)) {
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }

                String[] parts = trimmed.split("=", 2);
                envFileProperties.setProperty(parts[0].trim(), cleanValue(parts[1]));
            }
            System.out.println("[STRIPE DEBUG] .env charge : " + envPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[STRIPE DEBUG] Impossible de lire .env : " + e.getMessage());
        }
    }

    private String cleanValue(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\""))
                || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private long toMinorUnits(double amount, String currency) {
        int scale = minorUnitScale(currency);
        BigDecimal multiplier = BigDecimal.TEN.pow(scale);
        return BigDecimal.valueOf(amount).multiply(multiplier).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private int minorUnitScale(String currency) {
        String normalized = currency.toLowerCase(Locale.ROOT);
        if (ZERO_DECIMAL_CURRENCIES.contains(normalized)) {
            return 0;
        }
        if (THREE_DECIMAL_CURRENCIES.contains(normalized)) {
            return 3;
        }
        return 2;
    }

    public static class CheckoutSessionResult {
        private final String sessionId;
        private final String checkoutUrl;
        private final String status;
        private final String paymentStatus;
        private final String currency;

        public CheckoutSessionResult(String sessionId, String checkoutUrl, String status,
                                     String paymentStatus, String currency) {
            this.sessionId = sessionId;
            this.checkoutUrl = checkoutUrl;
            this.status = status;
            this.paymentStatus = paymentStatus;
            this.currency = currency;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getCheckoutUrl() {
            return checkoutUrl;
        }

        public String getStatus() {
            return status;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public String getCurrency() {
            return currency;
        }
    }

    public static class StripeConfigurationResult {
        private final boolean valid;
        private final String message;

        private StripeConfigurationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static StripeConfigurationResult success(String message) {
            return new StripeConfigurationResult(true, message);
        }

        public static StripeConfigurationResult failure(String message) {
            return new StripeConfigurationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class StripeConfig {
        private final String secretKey;

        private StripeConfig(String secretKey) {
            this.secretKey = secretKey == null ? "" : secretKey.trim();
        }

        private boolean isBlank() {
            return secretKey.isEmpty();
        }

        private boolean startsWithTestSecretPrefix() {
            return secretKey.startsWith("sk_test_");
        }

        private boolean startsWithPublishablePrefix() {
            return secretKey.startsWith("pk_test_");
        }

        private boolean isValidSecretKey() {
            return startsWithTestSecretPrefix();
        }
    }
}
