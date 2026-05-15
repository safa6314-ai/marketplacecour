package Services;

import Utils.MyBD;

import jakarta.mail.MessagingException;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SecurityAlertService {

    private static final String ALERT_TYPE = "FAILED_LOGIN_3_ATTEMPTS";

    private final Connection conn;
    private final WebcamService webcamService = new WebcamService();
    private final EmailService emailService = new EmailService();

    public SecurityAlertService() {
        conn = MyBD.getInstance().getConn();
        ensureSecuritySchema();
    }

    public void triggerFailedLoginAlert(String email) {
        File imageFile = null;
        String details = "Trois tentatives de connexion echouees pour " + maskEmail(email) + ".";

        try {
            imageFile = webcamService.captureImage(email);
        } catch (RuntimeException e) {
            System.err.println("[SECURITY] Capture ignoree : " + e.getMessage());
        }

        try {
            insertSecurityAlert(email, imageFile, ALERT_TYPE);
            insertAuditLog(email, "FAILED_LOGIN_SECURITY_ALERT", details);
        } catch (SQLException e) {
            System.err.println("[SECURITY] Impossible d'enregistrer l'audit : " + e.getMessage());
        }

        try {
            String adminEmail = getAdminSecurityEmail();
            if (isBlank(adminEmail)) {
                System.err.println("[SECURITY] ADMIN_SECURITY_EMAIL manquant. Email d'alerte ignore.");
                return;
            }

            emailService.sendSecurityAlertWithAttachment(
                    adminEmail,
                    "Alerte securite Artevia",
                    buildSecurityAlertHtml(email, imageFile),
                    imageFile
            );
        } catch (MessagingException e) {
            System.err.println("[SECURITY] Email d'alerte impossible : " + e.getMessage());
        }
    }

    private void ensureSecuritySchema() {
        if (conn == null) {
            System.err.println("[SECURITY] Connexion BD indisponible, schema securite non verifie.");
            return;
        }

        try {
            addColumnIfMissing("users", "failed_attempts", "INT DEFAULT 0");
            addColumnIfMissing("users", "lock_until", "DATETIME NULL");

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS audit_logs ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                        + "user_email VARCHAR(255),"
                        + "action VARCHAR(100),"
                        + "details TEXT,"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS security_alerts ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                        + "user_email VARCHAR(255),"
                        + "image_path VARCHAR(500),"
                        + "alert_type VARCHAR(100),"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")");
            }
        } catch (SQLException e) {
            System.err.println("[SECURITY] Schema securite non initialise : " + e.getMessage());
        }
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            if (rs.next()) {
                return;
            }
        }

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private void insertSecurityAlert(String email, File imageFile, String alertType) throws SQLException {
        String req = "INSERT INTO security_alerts (user_email, image_path, alert_type) VALUES (?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, email);
            pst.setString(2, imageFile == null ? null : imageFile.getAbsolutePath());
            pst.setString(3, alertType);
            pst.executeUpdate();
        }
    }

    private void insertAuditLog(String email, String action, String details) throws SQLException {
        String req = "INSERT INTO audit_logs (user_email, action, details) VALUES (?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, email);
            pst.setString(2, action);
            pst.setString(3, details);
            pst.executeUpdate();
        }
    }

    private String getAdminSecurityEmail() {
        String value = emailService.getConfigValue("ADMIN_SECURITY_EMAIL", "");
        if (!isBlank(value)) {
            return value;
        }

        value = emailService.getConfigValue("SMTP_FROM", "");
        if (!isBlank(value)) {
            return value;
        }

        return emailService.getConfigValue("SMTP_USERNAME", "");
    }

    private String buildSecurityAlertHtml(String email, File imageFile) {
        String imageText = imageFile == null
                ? "Aucune photo n'a pu etre capturee. La webcam est absente ou indisponible."
                : "Une photo de securite est jointe a cet email.";

        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f5f7fb;padding:24px;'>"
                + "<div style='max-width:560px;margin:auto;background:#ffffff;border-radius:14px;padding:24px;"
                + "border:1px solid #e4e7ee;'>"
                + "<h2 style='color:#6C2BD9;margin-top:0;'>Alerte securite Artevia</h2>"
                + "<p>Trois tentatives de connexion echouees ont ete detectees.</p>"
                + "<p><b>Email :</b> " + escapeHtml(maskEmail(email)) + "</p>"
                + "<p>" + imageText + "</p>"
                + "<p style='color:#8A8FA3;font-size:12px;'>Evenement enregistre dans audit_logs et security_alerts.</p>"
                + "</div></body></html>";
    }

    private String maskEmail(String email) {
        if (isBlank(email) || !email.contains("@")) {
            return "unknown";
        }

        int atIndex = email.indexOf('@');
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String visible = name.length() <= 2 ? name : name.substring(0, 2);
        return visible + "***" + domain;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
