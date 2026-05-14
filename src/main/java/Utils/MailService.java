package Utils;

import Entities.Abonnement;
import Entities.Souscription;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Sends purchase confirmations via Gmail SMTP.
 * <p>
 * Set {@code GMAIL_SMTP_PASSWORD} (Gmail App Password) and optionally {@code GMAIL_SMTP_USER}
 * (defaults to the from-address below). Do not commit app passwords to the repository.
 */
public final class MailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    /** Notifications go here (per product request). */
    private static final String TO_ADDRESS = "hassanjebri@gmail.com";

    private static final String DEFAULT_FROM = "jebrihassan66@gmail.com";

    private MailService() {}

    public static boolean isConfigured() {
        return !smtpPassword().isBlank();
    }

    private static String smtpUser() {
        String u = firstNonBlank(System.getenv("GMAIL_SMTP_USER"), System.getProperty("gmail.smtp.user"));
        return u != null && !u.isBlank() ? u : DEFAULT_FROM;
    }

    private static String smtpPassword() {
        return firstNonBlank(System.getenv("GMAIL_SMTP_PASSWORD"), System.getProperty("gmail.smtp.password"));
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return "";
    }

    /**
     * Fire-and-forget email on a background thread. Swallows errors after logging.
     */
    public static void sendPurchaseConfirmationAsync(Abonnement plan, Souscription sub, String clientName) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(sub, "sub");
        Thread t = new Thread(() -> {
            try {
                sendPurchaseConfirmation(plan, sub, clientName);
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                    NotificationHelper.warning(
                        "E-mail",
                        "L'envoi du courriel de confirmation a échoué. Vérifiez GMAIL_SMTP_PASSWORD."
                    ));
            }
        }, "mail-purchase-confirm");
        t.setDaemon(true);
        t.start();
    }

    public static void sendPurchaseConfirmation(Abonnement plan, Souscription sub, String clientName) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("Gmail SMTP password not set (GMAIL_SMTP_PASSWORD).");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        String user = smtpUser();
        String pass = smtpPassword();

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user, "Artevia", "UTF-8"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(TO_ADDRESS));
        message.setSubject("Confirmation d'abonnement — " + escapePlain(plan.getNom()), "UTF-8");

        String html = buildHtmlBody(plan, sub, clientName);
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=UTF-8");

        Multipart mp = new MimeMultipart("alternative");
        mp.addBodyPart(htmlPart);
        message.setContent(mp);
        Transport.send(message);
    }

    private static String escapePlain(String s) {
        if (s == null) return "";
        return s.replace("\r", " ").replace("\n", " ");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String buildHtmlBody(Abonnement plan, Souscription sub, String clientName) {
        String when = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.FRENCH).format(LocalDateTime.now());
        String price = String.format(Locale.US, "%.2f USD", plan.getPrix());
        String nomClient = escapeHtml(clientName);
        String nomPlan = escapeHtml(plan.getNom());
        String desc = escapeHtml(plan.getDescription() != null ? plan.getDescription() : "");

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;background:#0f1117;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;">
              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#0f1117;padding:32px 12px;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="max-width:600px;background:#161b26;border-radius:16px;overflow:hidden;border:1px solid #252d3d;">
                      <tr>
                        <td style="background:linear-gradient(135deg,#6366f1 0%%,#8b5cf6 50%%,#a855f7 100%%);padding:28px 32px;">
                          <div style="font-size:22px;font-weight:700;color:#ffffff;letter-spacing:0.02em;">Artevia</div>
                          <div style="font-size:14px;color:rgba(255,255,255,0.88);margin-top:6px;">Nouvel abonnement confirmé</div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px;color:#e5e7eb;font-size:15px;line-height:1.6;">
                          <p style="margin:0 0 16px;">Bonjour,</p>
                          <p style="margin:0 0 24px;">Un paiement vient d'être finalisé et l'abonnement est <strong style="color:#a5b4fc;">actif</strong>.</p>
                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#1f2937;border-radius:12px;border:1px solid #374151;">
                            <tr><td style="padding:18px 20px;">
                              <div style="font-size:12px;text-transform:uppercase;letter-spacing:0.08em;color:#9ca3af;margin-bottom:12px;">Récapitulatif</div>
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="8" style="font-size:14px;color:#f3f4f6;">
                                <tr><td style="color:#9ca3af;width:42%%;">Client</td><td style="font-weight:600;">%s</td></tr>
                                <tr><td style="color:#9ca3af;">Plan</td><td style="font-weight:600;">%s</td></tr>
                                <tr><td style="color:#9ca3af;">Montant</td><td style="font-weight:600;color:#86efac;">%s</td></tr>
                                <tr><td style="color:#9ca3af;">Durée</td><td>%d mois</td></tr>
                                <tr><td style="color:#9ca3af;">Début</td><td>%s</td></tr>
                                <tr><td style="color:#9ca3af;">Fin</td><td>%s</td></tr>
                                <tr><td style="color:#9ca3af;">N° souscription</td><td>#%d</td></tr>
                                <tr><td style="color:#9ca3af;">Statut</td><td><span style="display:inline-block;background:#312e81;color:#c7d2fe;padding:2px 10px;border-radius:999px;font-size:12px;">%s</span></td></tr>
                              </table>
                            </td></tr>
                          </table>
                          %s
                          <p style="margin:24px 0 0;font-size:13px;color:#9ca3af;">Reçu le %s — Merci d'utiliser Artevia.</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:16px 32px 28px;font-size:12px;color:#6b7280;text-align:center;border-top:1px solid #252d3d;">
                          Ce message est généré automatiquement. En cas de question, répondez à ce fil ou contactez le support.
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                nomClient,
                nomPlan,
                price,
                plan.getDureeMois(),
                escapeHtml(sub.getDateDebut().toString()),
                escapeHtml(sub.getDateFin().toString()),
                sub.getIdSouscription(),
                escapeHtml(sub.getStatut()),
                desc.isBlank()
                    ? ""
                    : "<p style=\"margin:20px 0 0;font-size:14px;color:#9ca3af;\">" + desc + "</p>",
                escapeHtml(when)
        );
    }
}
