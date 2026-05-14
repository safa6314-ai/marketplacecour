package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SentimentService {

    private static final String API_URL =
            "https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english";
    private static final String HF_TOKEN = System.getenv("HF_TOKEN");
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String POSITIVE = "\uD83D\uDE0A";
    private static final String NEGATIVE = "\uD83D\uDE20";
    private static final String NEUTRAL = "\uD83D\uDE10";

    private static final List<String> POSITIVE_WORDS = List.of(
            "bon", "bonne", "excellent", "excellente", "super", "magnifique", "j'adore",
            "bravo", "merci", "heureux", "heureuse", "nice", "good", "great", "love", "happy"
    );

    private static final List<String> NEGATIVE_WORDS = List.of(
            "mauvais", "mauvaise", "nul", "nulle", "triste", "probleme", "problème",
            "deteste", "déteste", "horrible", "erreur", "bad", "hate", "sad", "angry"
    );

    public String analyser(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return NEUTRAL;
        }

        try {
            String response = callHuggingFace(texte);
            String sentiment = parseHuggingFaceResponse(response);
            if (sentiment != null) {
                return sentiment;
            }
        } catch (Exception ignored) {}

        return analyserLocalement(texte);
    }

    private String callHuggingFace(String texte) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        if (HF_TOKEN != null && !HF_TOKEN.isBlank()) {
            conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
        }
        conn.setDoOutput(true);

        String body = mapper.writeValueAsString(new Input(texte));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
        String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (code != 200) {
            throw new IllegalStateException(response);
        }
        return response;
    }

    private String parseHuggingFaceResponse(String response) throws Exception {
        JsonNode root = mapper.readTree(response);
        JsonNode predictions = root.isArray() && root.size() > 0 && root.get(0).isArray()
                ? root.get(0)
                : root;

        String bestLabel = null;
        double bestScore = -1;
        for (JsonNode prediction : predictions) {
            String label = prediction.path("label").asText("");
            double score = prediction.path("score").asDouble(0);
            if (score > bestScore) {
                bestScore = score;
                bestLabel = label;
            }
        }

        if ("POSITIVE".equalsIgnoreCase(bestLabel)) return POSITIVE;
        if ("NEGATIVE".equalsIgnoreCase(bestLabel)) return NEGATIVE;
        return null;
    }

    private String analyserLocalement(String texte) {
        String lower = texte.toLowerCase();
        int score = 0;

        for (String word : POSITIVE_WORDS) {
            if (lower.contains(word)) score++;
        }
        for (String word : NEGATIVE_WORDS) {
            if (lower.contains(word)) score--;
        }

        if (score > 0) return POSITIVE;
        if (score < 0) return NEGATIVE;
        return NEUTRAL;
    }

    private static class Input {
        public final String inputs;

        private Input(String inputs) {
            this.inputs = inputs;
        }
    }
}
