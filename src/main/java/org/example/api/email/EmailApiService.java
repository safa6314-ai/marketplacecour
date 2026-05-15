package org.example.api.email;

import org.example.api.ApiConfig;
import org.example.api.ApiException;
import org.example.api.ApiHttpClient;
import org.example.api.JsonUtils;

public class EmailApiService {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final ApiHttpClient httpClient;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;

    public EmailApiService() {
        this(new ApiHttpClient(),
                ApiConfig.requiredEnv("BREVO_API_KEY"),
                ApiConfig.requiredEnv("BREVO_SENDER_EMAIL"),
                ApiConfig.optionalEnv("BREVO_SENDER_NAME", "Plateforme Cours"));
    }

    public EmailApiService(ApiHttpClient httpClient, String apiKey, String senderEmail, String senderName) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
    }

    public void sendEmail(EmailMessage message) {
        String body = """
                {
                  "sender": {"name": %s, "email": %s},
                  "to": [{"email": %s, "name": %s}],
                  "subject": %s,
                  "htmlContent": %s
                }
                """.formatted(
                JsonUtils.quote(senderName),
                JsonUtils.quote(senderEmail),
                JsonUtils.quote(message.toEmail()),
                JsonUtils.quote(message.toName()),
                JsonUtils.quote(message.subject()),
                JsonUtils.quote(message.htmlContent())
        );

        try {
            httpClient.postJson(BREVO_URL, body, "api-key", apiKey);
        } catch (ApiException e) {
            if (e.getMessage() != null && e.getMessage().contains("unrecognized IP address")) {
                throw new ApiException(
                        "Brevo a bloque l'appel API car cette adresse IP n'est pas encore autorisee. "
                                + "Ouvre l'email de securite Brevo et clique sur \"Oui, autoriser la nouvelle adresse IP\". "
                                + "Si ce n'etait pas toi, clique sur \"Non, changer les cles API\", cree une nouvelle cle, "
                                + "puis mets a jour BREVO_API_KEY.",
                        e
                );
            }
            throw e;
        }
    }

    public void sendCourseReminder(String studentEmail, String studentName, String courseTitle, String dateText) {
        sendEmail(new EmailMessage(
                studentEmail,
                studentName,
                "Rappel de cours : " + courseTitle,
                "<h2>Rappel de cours</h2><p>Bonjour " + studentName + ",</p>"
                        + "<p>Votre cours <strong>" + courseTitle + "</strong> est prevu : "
                        + dateText + ".</p>"
        ));
    }
}
