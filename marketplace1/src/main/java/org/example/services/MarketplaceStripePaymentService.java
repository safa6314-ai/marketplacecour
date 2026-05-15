package org.example.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Set;

public class MarketplaceStripePaymentService {

    private static final Set<String> ZERO_DECIMAL_CURRENCIES = Set.of(
            "bif", "clp", "djf", "gnf", "jpy", "kmf", "krw", "mga",
            "pyg", "rwf", "ugx", "vnd", "vuv", "xaf", "xof", "xpf"
    );
    private static final Set<String> THREE_DECIMAL_CURRENCIES = Set.of(
            "bhd", "jod", "kwd", "omr", "tnd"
    );

    public boolean isConfigured() {
        return apiKey() != null;
    }

    public String configuredCurrency() {
        String currency = System.getenv("STRIPE_CURRENCY");
        if (currency == null || currency.trim().isEmpty()) {
            return "usd";
        }
        return currency.trim().toLowerCase(Locale.ROOT);
    }

    public CheckoutSessionResult createCheckoutSession(double amount, String orderName, String paymentRef)
            throws StripeException {
        String key = apiKey();
        if (key == null) {
            throw new IllegalStateException("STRIPE_SECRET_KEY n'est pas configuree.");
        }

        String currency = configuredCurrency();
        long unitAmount = toMinorUnits(amount, currency);
        String successUrl = envOrDefault("STRIPE_SUCCESS_URL", "https://example.com/artevia/stripe/success");
        String cancelUrl = envOrDefault("STRIPE_CANCEL_URL", "https://example.com/artevia/stripe/cancel");

        Stripe.apiKey = key;
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
        String key = apiKey();
        if (key == null) {
            throw new IllegalStateException("STRIPE_SECRET_KEY n'est pas configuree.");
        }
        Stripe.apiKey = key;
        Session session = Session.retrieve(sessionId);
        System.out.println("Stripe Checkout session verifiee: id=" + session.getId()
                + ", status=" + session.getStatus()
                + ", payment_status=" + session.getPaymentStatus());
        return new CheckoutSessionResult(session.getId(), session.getUrl(), session.getStatus(),
                session.getPaymentStatus(), configuredCurrency());
    }

    private String apiKey() {
        String key = System.getenv("STRIPE_SECRET_KEY");
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        return key.trim();
    }

    private String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
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
}
