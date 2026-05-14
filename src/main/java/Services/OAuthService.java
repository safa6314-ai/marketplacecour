package Services;

import Entites.OAuthUser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.BindException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAuthService {

    private static final int CALLBACK_PORT = 8765;
    private static final int CALLBACK_TIMEOUT_SECONDS = 120;
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:8765/oauth/google";
    private static final String FACEBOOK_REDIRECT_URI = "http://localhost:8765/oauth/facebook";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final SecureRandom random = new SecureRandom();
    private final Properties appProperties = new Properties();
    private final Properties envFileProperties = new Properties();

    public OAuthService() {
        loadApplicationProperties();
        loadEnvFile();
    }

    public OAuthUser loginWithGoogle() throws Exception {
        String clientId = getConfig("GOOGLE_CLIENT_ID", "");
        String clientSecret = getConfig("GOOGLE_CLIENT_SECRET", "");
        logOAuthDebug("Google", clientId, clientSecret, GOOGLE_REDIRECT_URI);
        validateOAuthConfig("Google", clientId, clientSecret);

        String redirectUri = GOOGLE_REDIRECT_URI;
        String state = generateState();
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(state)
                + "&prompt=select_account";

        String code = waitForAuthorizationCode("/oauth/google", authUrl, state, redirectUri);
        String tokenResponse = postForm("https://oauth2.googleapis.com/token",
                "code=" + encode(code)
                        + "&client_id=" + encode(clientId)
                        + "&client_secret=" + encode(clientSecret)
                        + "&redirect_uri=" + encode(redirectUri)
                        + "&grant_type=authorization_code");

        String accessToken = extractJsonValue(tokenResponse, "access_token");

        if (isBlank(accessToken)) {
            throw new IllegalStateException("Google n'a pas retourne access_token.");
        }

        String profileResponse = getJson("https://www.googleapis.com/oauth2/v3/userinfo", accessToken);

        return new OAuthUser(
                "GOOGLE",
                extractJsonValue(profileResponse, "sub"),
                extractJsonValue(profileResponse, "name"),
                extractJsonValue(profileResponse, "email")
        );
    }

    public OAuthUser loginWithFacebook() throws Exception {
        String clientId = getConfig("FACEBOOK_CLIENT_ID", "");
        String clientSecret = getConfig("FACEBOOK_CLIENT_SECRET", "");
        String apiVersion = getConfig("FACEBOOK_API_VERSION", "v20.0");
        logOAuthDebug("Facebook", clientId, clientSecret, FACEBOOK_REDIRECT_URI);
        validateOAuthConfig("Facebook", clientId, clientSecret);

        String redirectUri = FACEBOOK_REDIRECT_URI;
        String state = generateState();
        String authUrl = "https://www.facebook.com/" + apiVersion + "/dialog/oauth"
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode("email,public_profile")
                + "&state=" + encode(state);

        String code = waitForAuthorizationCode("/oauth/facebook", authUrl, state, redirectUri);
        String tokenUrl = "https://graph.facebook.com/" + apiVersion + "/oauth/access_token"
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&client_secret=" + encode(clientSecret)
                + "&code=" + encode(code);

        String tokenResponse = getRaw(tokenUrl);
        String accessToken = extractJsonValue(tokenResponse, "access_token");

        if (isBlank(accessToken)) {
            throw new IllegalStateException("Facebook n'a pas retourne access_token.");
        }

        String profileResponse = getRaw("https://graph.facebook.com/me?fields=id,name,email&access_token=" + encode(accessToken));

        return new OAuthUser(
                "FACEBOOK",
                extractJsonValue(profileResponse, "id"),
                extractJsonValue(profileResponse, "name"),
                extractJsonValue(profileResponse, "email")
        );
    }

    private String waitForAuthorizationCode(String path, String authUrl, String expectedState, String redirectUri) throws Exception {
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server;

        try {
            server = HttpServer.create(new InetSocketAddress("localhost", CALLBACK_PORT), 0);
        } catch (BindException e) {
            throw new IllegalStateException("Le port OAuth 8765 est deja utilise. Fermez l'ancien test OAuth ou relancez l'application.");
        }

        server.createContext(path, exchange -> handleCallback(exchange, expectedState, codeFuture));
        server.start();
        System.out.println("[OAUTH DEBUG] Serveur OAuth demarre sur http://localhost:" + CALLBACK_PORT + path);

        try {
            openBrowser(authUrl);
            return codeFuture.get(CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new IllegalStateException("Temps expire. Le fournisseur n'a pas renvoye le code OAuth. Verifiez le redirect URI : " + redirectUri);
        } finally {
            server.stop(0);
            System.out.println("[OAUTH DEBUG] Serveur OAuth arrete.");
        }
    }

    private void handleCallback(HttpExchange exchange, String expectedState, CompletableFuture<String> codeFuture) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
        String code = params.get("code");
        String state = params.get("state");
        String error = params.get("error");

        String message;

        if (!isBlank(error)) {
            codeFuture.completeExceptionally(new IllegalStateException("OAuth annule : " + error));
            message = "Connexion annulee. Vous pouvez fermer cette page.";
        } else if (isBlank(code) || !expectedState.equals(state)) {
            codeFuture.completeExceptionally(new IllegalStateException(
                    "Reponse OAuth invalide. N'ouvrez pas directement localhost:8765/oauth/google. Cliquez sur le bouton Google Login."));
            message = "Reponse OAuth invalide. Vous pouvez fermer cette page.";
        } else {
            codeFuture.complete(code);
            message = "Connexion validee. Vous pouvez revenir a Artevia.";
        }

        byte[] response = ("<html><body style='font-family:Arial;text-align:center;padding:40px;'>"
                + "<h2>Artevia</h2><p>" + message + "</p></body></html>").getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    private String postForm(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response);
        return response.body();
    }

    private String getJson(String url, String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response);
        return response.body();
    }

    private String getRaw(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response);
        return response.body();
    }

    private void ensureSuccess(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Erreur OAuth HTTP " + response.statusCode() + " : " + response.body());
        }
    }

    private void openBrowser(String url) throws Exception {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IllegalStateException("Navigateur non disponible. Ouvrez ce lien : " + url);
        }

        Desktop.getDesktop().browse(URI.create(url));
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.trim().isEmpty()) {
            return params;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            params.put(key, value);
        }

        return params;
    }

    private String extractJsonValue(String json, String key) {
        if (json == null) {
            return "";
        }

        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .replace("\\n", "\n");
    }

    private void validateOAuthConfig(String provider, String clientId, String clientSecret) {
        StringBuilder missing = new StringBuilder();

        if (isBlank(clientId)) {
            missing.append(provider.toUpperCase()).append("_CLIENT_ID ");
        }

        if (isBlank(clientSecret)) {
            missing.append(provider.toUpperCase()).append("_CLIENT_SECRET ");
        }

        if (missing.length() > 0) {
            throw new IllegalStateException("Configuration " + provider + " manquante : " + missing.toString().trim()
                    + ". Remplissez le fichier .env puis relancez l'application.");
        }
    }

    private void logOAuthDebug(String provider, String clientId, String clientSecret, String redirectUri) {
        System.out.println("[OAUTH DEBUG] Provider = " + provider);
        System.out.println("[OAUTH DEBUG] CLIENT_ID configured = " + !isBlank(clientId));
        System.out.println("[OAUTH DEBUG] CLIENT_SECRET configured = " + !isBlank(clientSecret));
        System.out.println("[OAUTH DEBUG] Redirect URI = " + redirectUri);
        System.out.println("[OAUTH DEBUG] Callback port = " + CALLBACK_PORT);
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

    private void loadApplicationProperties() {
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            if (inputStream != null) {
                appProperties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("[OAUTH DEBUG] Impossible de lire application.properties : " + e.getMessage());
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
            System.err.println("[OAUTH DEBUG] Impossible de lire .env : " + e.getMessage());
        }
    }

    private String generateState() {
        return Long.toHexString(random.nextLong()) + Long.toHexString(System.nanoTime());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

