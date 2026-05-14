package Services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Random;

public class QuoteService {

    private static final String API_URL = "https://api.api-ninjas.com/v2/randomquotes?categories=art";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    private final Gson gson = new Gson();
    private final Random random = new Random();

    private final List<ArtQuote> fallbackQuotes = List.of(
            new ArtQuote("Art washes away from the soul the dust of everyday life.", "Pablo Picasso"),
            new ArtQuote("Creativity takes courage.", "Henri Matisse"),
            new ArtQuote("Every artist was first an amateur.", "Ralph Waldo Emerson"),
            new ArtQuote("A work of art is a world in itself.", "Wassily Kandinsky")
    );

    public ArtQuote citationAleatoire() {
        String apiKey = System.getenv("API_NINJAS_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return citationLocale();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(TIMEOUT)
                    .header("X-Api-Key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body() == null || response.body().isBlank()) {
                return citationLocale();
            }

            JsonArray json = gson.fromJson(response.body(), JsonArray.class);
            if (json == null || json.isEmpty() || !json.get(0).isJsonObject()) {
                return citationLocale();
            }

            JsonObject first = json.get(0).getAsJsonObject();
            String quote = lireChamp(first, "quote");
            String author = lireChamp(first, "author");
            if (quote.isBlank()) {
                return citationLocale();
            }

            return new ArtQuote(quote, author.isBlank() ? "Artiste inconnu" : author);
        } catch (IOException | InterruptedException | RuntimeException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return citationLocale();
        }
    }

    private ArtQuote citationLocale() {
        return fallbackQuotes.get(random.nextInt(fallbackQuotes.size()));
    }

    private String lireChamp(JsonObject json, String champ) {
        return json.has(champ) && !json.get(champ).isJsonNull() ? json.get(champ).getAsString() : "";
    }

    public static class ArtQuote {
        private final String quote;
        private final String author;

        public ArtQuote(String quote, String author) {
            this.quote = quote;
            this.author = author;
        }

        public String getQuote() {
            return quote;
        }

        public String getAuthor() {
            return author;
        }
    }
}
