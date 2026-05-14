package Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationService {

    private static final String DEFAULT_API_URL = "https://libretranslate.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(6);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    private final Gson gson = new Gson();
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String traduire(String texte, String langueCible) throws IOException, InterruptedException {
        String valeur = texte == null ? "" : texte.trim();
        String cible = langueCible == null ? "fr" : langueCible.trim().toLowerCase();

        if (valeur.isBlank() || cible.equals("fr")) {
            return valeur;
        }

        String cacheKey = cible + "::" + valeur;
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        IOException derniereErreur = null;
        for (String url : apiUrls()) {
            try {
                String body = creerFormBody(valeur, cible, url);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url + "/translate"))
                        .timeout(TIMEOUT)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("Erreur LibreTranslate HTTP " + response.statusCode() + " : " + response.body());
                }

                String traduction = lireTraduction(response.body());
                cache.put(cacheKey, traduction);
                return traduction;
            } catch (IOException e) {
                derniereErreur = e;
            }
        }

        throw derniereErreur == null ? new IOException("Aucun serveur LibreTranslate disponible.") : derniereErreur;
    }

    private String lireTraduction(String responseBody) throws IOException {
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        if (json == null || !json.has("translatedText") || json.get("translatedText").isJsonNull()) {
            throw new IOException("LibreTranslate a retourne une reponse vide.");
        }

        String traduction = json.get("translatedText").getAsString();
        if (traduction == null || traduction.isBlank()) {
            throw new IOException("LibreTranslate a retourne un texte vide.");
        }

        return traduction;
    }

    public String codeLangue(String libelle) {
        String langue = libelle == null ? "" : libelle.trim().toLowerCase();
        if (langue.contains("english") || langue.contains("anglais") || langue.equals("en")) {
            return "en";
        }
        if (langue.contains("arab") || langue.contains("arabe") || langue.contains("\u0639\u0631\u0628") || langue.equals("ar")) {
            return "ar";
        }
        return "fr";
    }

    private String creerFormBody(String texte, String cible, String url) {
        StringBuilder body = new StringBuilder();
        ajouterParametre(body, "q", texte);
        ajouterParametre(body, "source", "fr");
        ajouterParametre(body, "target", cible);
        ajouterParametre(body, "format", "text");

        String apiKey = System.getenv("LIBRETRANSLATE_API_KEY");
        if (apiKey != null && !apiKey.isBlank() && url.equals(DEFAULT_API_URL)) {
            ajouterParametre(body, "api_key", apiKey);
        }

        return body.toString();
    }

    private void ajouterParametre(StringBuilder body, String nom, String valeur) {
        if (!body.isEmpty()) {
            body.append("&");
        }
        body.append(encoder(nom)).append("=").append(encoder(valeur));
    }

    private String encoder(String valeur) {
        return URLEncoder.encode(valeur, StandardCharsets.UTF_8);
    }

    private List<String> apiUrls() {
        List<String> urls = new ArrayList<>();
        String customUrl = System.getenv("LIBRETRANSLATE_URL");
        if (customUrl != null && !customUrl.isBlank()) {
            urls.add(nettoyerUrl(customUrl));
            return urls;
        }

        String apiKey = System.getenv("LIBRETRANSLATE_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            urls.add(DEFAULT_API_URL);
        } else {
            urls.add("https://translate.fedilab.app");
            urls.add("https://translate.cutie.dating");
            urls.add(DEFAULT_API_URL);
        }

        return urls;
    }

    private String nettoyerUrl(String url) {
        String cleanUrl = url.trim();
        return cleanUrl.endsWith("/") ? cleanUrl.substring(0, cleanUrl.length() - 1) : cleanUrl;
    }
}
