package Services;

import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Properties;

public class EmailService {

    private static final String DEFAULT_HOST = "smtp.gmail.com";
    private static final String DEFAULT_PORT = "587";
    private static final String DEFAULT_USERNAME = "bilel3375@gmail.com";
    private static final int OTP_BOUND = 1_000_000;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Properties appProperties = new Properties();
    private final Properties envFileProperties = new Properties();

    public EmailService() {
        loadApplicationProperties();
        loadEnvFile();
    }

    public String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(OTP_BOUND));
    }

    public void sendPasswordResetCode(String toEmail, String code) throws MessagingException {
        SmtpConfig config = loadSmtpConfig();
        logSmtpDebug(config);
        validateConfig(config);

        Session session = createSession(config);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Code de reinitialisation Artevia", "UTF-8");
        message.setContent(buildPasswordResetHtml(code), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    public void sendSecurityAlertWithAttachment(String to, String subject, String body, File attachment)
            throws MessagingException {
        SmtpConfig config = loadSmtpConfig();
        logSmtpDebug(config);
        validateConfig(config);

        if (isBlank(to)) {
            throw new MessagingException("ADMIN_SECURITY_EMAIL manquant.");
        }

        Session session = createSession(config);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");

        BodyPart textPart = new MimeBodyPart();
        textPart.setContent(body, "text/html; charset=UTF-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        if (attachment != null && attachment.exists() && attachment.isFile()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            try {
                attachmentPart.attachFile(attachment);
                multipart.addBodyPart(attachmentPart);
            } catch (IOException e) {
                throw new MessagingException("Impossible d'attacher la photo de securite.", e);
            }
        }

        message.setContent(multipart);
        Transport.send(message);
    }

    private SmtpConfig loadSmtpConfig() {
        String username = getConfigWithAliases(DEFAULT_USERNAME, "SMTP_USERNAME", "SMTP_USER");

        return new SmtpConfig(
                getConfig("SMTP_HOST", DEFAULT_HOST),
                getConfig("SMTP_PORT", DEFAULT_PORT),
                username,
                getConfig("SMTP_PASSWORD", ""),
                getConfigWithAliases(username, "SMTP_FROM", "SMTP_USER", "SMTP_USERNAME")
        );
    }

    private Session createSession(SmtpConfig config) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", config.host);
        props.put("mail.smtp.port", config.port);
        props.put("mail.smtp.ssl.trust", config.host);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.username, config.password);
            }
        });
    }

    private void validateConfig(SmtpConfig config) throws MessagingException {
        StringBuilder missing = new StringBuilder();

        if (isBlank(config.host)) {
            missing.append("SMTP_HOST ");
        }

        if (isBlank(config.port)) {
            missing.append("SMTP_PORT ");
        }

        if (isBlank(config.username)) {
            missing.append("SMTP_USERNAME ");
        }

        if (isBlank(config.password)) {
            missing.append("SMTP_PASSWORD ");
        }

        if (isBlank(config.from)) {
            missing.append("SMTP_FROM ");
        }

        if (missing.length() > 0) {
            throw new MessagingException("Configuration SMTP manquante : " + missing.toString().trim()
                    + ". Utilisez un Gmail App Password, pas le mot de passe normal Gmail.");
        }
    }

    private String buildPasswordResetHtml(String code) {
        return "<!DOCTYPE html>"
                + "<html><body style='margin:0;padding:0;background:#f5f7fb;font-family:Arial,sans-serif;'>"
                + "<div style='max-width:520px;margin:30px auto;background:white;border-radius:14px;"
                + "padding:28px;border:1px solid #e4e7ee;'>"
                + "<h2 style='margin:0;color:#5b2b91;'>Artevia</h2>"
                + "<p style='color:#1f2937;font-size:15px;'>Bonjour,</p>"
                + "<p style='color:#4b5563;font-size:14px;'>Voici votre code de reinitialisation :</p>"
                + "<div style='font-size:34px;font-weight:800;letter-spacing:8px;color:#6C3EF4;"
                + "background:#f2e8ff;padding:18px;border-radius:12px;text-align:center;'>"
                + code
                + "</div>"
                + "<p style='color:#4b5563;font-size:14px;'>Ce code expire dans 10 minutes.</p>"
                + "<p style='color:#8a8fa3;font-size:12px;'>Si vous n'avez pas demande ce changement, ignorez cet email.</p>"
                + "</div></body></html>";
    }

    private String getConfig(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (!isBlank(systemValue)) {
            return systemValue.trim();
        }

        String envValue = System.getenv(key);
        if (!isBlank(envValue)) {
            return envValue.trim();
        }

        String envFileValue = envFileProperties.getProperty(key);
        if (!isBlank(envFileValue)) {
            return envFileValue.trim();
        }

        String propertyValue = appProperties.getProperty(key);
        if (!isBlank(propertyValue)) {
            return propertyValue.trim();
        }

        return defaultValue;
    }

    public String getConfigValue(String key, String defaultValue) {
        return getConfig(key, defaultValue);
    }

    private String getConfigWithAliases(String defaultValue, String... keys) {
        for (String key : keys) {
            String value = getConfig(key, null);
            if (!isBlank(value)) {
                return value.trim();
            }
        }

        return defaultValue;
    }

    private void loadApplicationProperties() {
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            if (inputStream != null) {
                appProperties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("[SMTP DEBUG] Impossible de lire application.properties : " + e.getMessage());
        }
    }

    private void loadEnvFile() {
        Path envPath = Path.of(".env");

        if (!Files.exists(envPath)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(envPath)) {
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }

                String[] parts = trimmed.split("=", 2);
                envFileProperties.setProperty(parts[0].trim(), parts[1].trim());
            }
        } catch (IOException e) {
            System.err.println("[SMTP DEBUG] Impossible de lire le fichier .env : " + e.getMessage());
        }
    }

    private void logSmtpDebug(SmtpConfig config) {
        System.out.println("[SMTP DEBUG] SMTP_HOST = " + config.host);
        System.out.println("[SMTP DEBUG] SMTP_PORT = " + config.port);
        System.out.println("[SMTP DEBUG] SMTP_USERNAME is null/empty = " + isBlank(config.username));
        System.out.println("[SMTP DEBUG] SMTP_PASSWORD is null/empty = " + isBlank(config.password));
        System.out.println("[SMTP DEBUG] SMTP_FROM = " + config.from);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class SmtpConfig {
        private final String host;
        private final String port;
        private final String username;
        private final String password;
        private final String from;

        private SmtpConfig(String host, String port, String username, String password, String from) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.from = from;
        }
    }
}

