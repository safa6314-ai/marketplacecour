package Utils;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import Entities.Abonnement;

public class StripeService {
    private static final String SECRET_KEY = "sk_test_XXXXXXXXXXXXXXXX";

    static {
        Stripe.apiKey = SECRET_KEY;
    }

    public static String getPaymentUrl(Abonnement abonnement) throws Exception {
        return createSession(
            "Abonnement Artevia: " + abonnement.getNom(), 
            "Accès pendant " + abonnement.getDureeMois() + " mois au plan " + abonnement.getNom(), 
            abonnement.getPrix()
        );
    }

    private static String createSession(String name, String description, Double amount) throws Exception {
        if (amount == null || amount <= 0) return null;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://success.example.com")
                .setCancelUrl("https://cancel.example.com")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd") // Switched back to USD for test account compatibility
                                .setUnitAmount((long) (amount * 100))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(name)
                                        .setDescription(description)
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
