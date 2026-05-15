package Services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Properties;

public class SmsVerificationService {

    private static final String TWILIO_VERIFY_BASE_URL = "https://verify.twilio.com/v2/Services/";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Properties appProperties = new Properties();
    private final Properties envFileProperties = new Properties();

    public SmsVerificationService() {
        loadApplicationProperties();
        loadEnvFile();
    }

    public boolean sendVerificationCode(String phoneNumber) throws IOException, InterruptedException {
        if (isVerificationDisabled()) {
            System.out.println("[TWILIO DEBUG] Verification SMS desactivee. Code local pour " + phoneNumber + " : 000000");
            return true;
        }

        TwilioConfig config = loadConfig();
        validateConfig(config);

        String body = formEncode("To", phoneNumber) + "&" + formEncode("Channel", "sms");
        HttpRequest request = baseRequest(config, "/Verifications")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (isSuccess(response)) {
            return true;
        }

        throw new IOException(buildTwilioError("envoi OTP", response));
    }

    public boolean checkVerificationCode(String phoneNumber, String code) throws IOException, InterruptedException {
        if (isVerificationDisabled()) {
            return "000000".equals(code);
        }

        TwilioConfig config = loadConfig();
        validateConfig(config);

        String body = formEncode("To", phoneNumber) + "&" + formEncode("Code", code);
        HttpRequest request = baseRequest(config, "/VerificationCheck")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body() == null ? "" : response.body().replace(" ", "");
        if (isSuccess(response)) {
            return responseBody.contains("\"status\":\"approved\"");
        }

        throw new IOException(buildTwilioError("verification OTP", response));
    }

    private boolean isSuccess(HttpResponse<String> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private String buildTwilioError(String action, HttpResponse<String> response) {
        String body = response.body() == null ? "" : response.body();
        String message = extractJsonValue(body, "message");
        String code = extractJsonValue(body, "code");

        StringBuilder error = new StringBuilder("Erreur Twilio pendant ")
                .append(action)
                .append(" (HTTP ")
                .append(response.statusCode())
                .append(")");

        if (!isBlank(code)) {
            error.append(" - code ").append(code);
        }

        if (!isBlank(message)) {
            error.append(" : ").append(message);
        } else if (!isBlank(body)) {
            error.append(" : ").append(body);
        }

        return error.toString();
    }

    private String extractJsonValue(String json, String key) {
        if (isBlank(json)) {
            return "";
        }

        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return "";
        }

        int colonIndex = json.indexOf(':', keyIndex + pattern.length());
        if (colonIndex < 0) {
            return "";
        }

        int firstQuote = json.indexOf('"', colonIndex + 1);
        if (firstQuote < 0) {
            return "";
        }

        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) {
            return "";
        }

        return json.substring(firstQuote + 1, secondQuote);
    }

    private HttpRequest.Builder baseRequest(TwilioConfig config, String path) {
        String credentials = config.accountSid + ":" + config.authToken;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder()
                .uri(URI.create(TWILIO_VERIFY_BASE_URL + config.verifyServiceSid + path))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded");
    }

    private TwilioConfig loadConfig() {
        TwilioConfig config = new TwilioConfig(
                getConfig("TWILIO_ACCOUNT_SID", ""),
                getConfig("TWILIO_AUTH_TOKEN", ""),
                getConfig("TWILIO_VERIFY_SERVICE_SID", "")
        );

        System.out.println("[TWILIO DEBUG] TWILIO_ACCOUNT_SID present = " + !isBlank(config.accountSid));
        System.out.println("[TWILIO DEBUG] TWILIO_AUTH_TOKEN present = " + !isBlank(config.authToken));
        System.out.println("[TWILIO DEBUG] TWILIO_VERIFY_SERVICE_SID present = " + !isBlank(config.verifyServiceSid));

        return config;
    }

    public boolean isVerificationDisabled() {
        return Boolean.parseBoolean(getConfig("TWILIO_VERIFY_DISABLED", "false"));
    }

    private void validateConfig(TwilioConfig config) {
        StringBuilder missing = new StringBuilder();

        if (isBlank(config.accountSid)) {
            missing.append("TWILIO_ACCOUNT_SID ");
        }

        if (isBlank(config.authToken)) {
            missing.append("TWILIO_AUTH_TOKEN ");
        }

        if (isBlank(config.verifyServiceSid)) {
            missing.append("TWILIO_VERIFY_SERVICE_SID ");
        }

        if (missing.length() > 0) {
            throw new IllegalStateException("Configuration Twilio manquante : " + missing.toString().trim());
        }
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

    private String formEncode(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void loadApplicationProperties() {
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            if (inputStream != null) {
                appProperties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("[TWILIO DEBUG] Impossible de lire application.properties : " + e.getMessage());
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
                envFileProperties.setProperty(parts[0].trim(), parts[1].trim());
            }
            System.out.println("[TWILIO DEBUG] .env charge : " + envPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[TWILIO DEBUG] Impossible de lire .env : " + e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class TwilioConfig {
        private final String accountSid;
        private final String authToken;
        private final String verifyServiceSid;

        private TwilioConfig(String accountSid, String authToken, String verifyServiceSid) {
            this.accountSid = accountSid;
            this.authToken = authToken;
            this.verifyServiceSid = verifyServiceSid;
        }
    }
}
