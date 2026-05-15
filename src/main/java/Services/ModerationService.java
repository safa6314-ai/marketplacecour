package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ModerationService {

    private static final String API_URL =
            "https://api-inference.huggingface.co/models/unitary/toxic-bert";
    private static final String HF_TOKEN = System.getenv("HF_TOKEN");
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final List<String> CATEGORIES = List.of(
            "Art", "Cours", "Evenement", "Marche", "Discussion generale"
    );

    private static final List<String> TOXIC_WORDS = List.of(
            "idiot", "stupide", "nul", "nulle", "imbecile", "imbecile",
            "connard", "con", "merde", "ta gueule", "insulte", "hate",
            "kill", "threat", "menace", "raciste", "violence", "violent"
    );

    public Result analyser(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return new Result("en_attente", 0.0, "Moderation: contenu vide ou invalide.");
        }

        try {
            String response = callHuggingFace(texte);
            Result result = parseHuggingFaceResponse(response);
            if (result != null) return result;
        } catch (Exception ignored) {
        }

        return analyserLocalement(texte);
    }

    public String categoriePour(String texte, String categorieActuelle) {
        if (isCategorieValide(categorieActuelle)) return categorieActuelle;
        if (texte == null || texte.isBlank()) return "Discussion generale";

        String lower = texte.toLowerCase();
        if (containsAny(lower, List.of("cours", "formation", "atelier", "apprendre", "lecon", "workshop"))) {
            return "Cours";
        }
        if (containsAny(lower, List.of("evenement", "exposition", "galerie", "vernissage", "festival", "rencontre"))) {
            return "Evenement";
        }
        if (containsAny(lower, List.of("marketplace", "vendre", "vente", "prix", "acheter", "disponible", "commande"))) {
            return "Marche";
        }
        if (containsAny(lower, List.of("peinture", "sculpture", "portrait", "aquarelle", "photo", "photographie", "toile", "dessin", "art"))) {
            return "Art";
        }
        return "Discussion generale";
    }

    public boolean isCategorieValide(String categorie) {
        return categorie != null && CATEGORIES.contains(categorie);
    }

    public List<String> categories() {
        return CATEGORIES;
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

    private Result parseHuggingFaceResponse(String response) throws Exception {
        JsonNode root = mapper.readTree(response);
        JsonNode predictions = root.isArray() && root.size() > 0 && root.get(0).isArray()
                ? root.get(0)
                : root;

        double maxToxic = 0.0;
        String bestLabel = "clean";
        for (JsonNode prediction : predictions) {
            String label = prediction.path("label").asText("").toLowerCase();
            double score = prediction.path("score").asDouble(0);
            if (isToxicLabel(label) && score > maxToxic) {
                maxToxic = score;
                bestLabel = label;
            }
        }
        return resultFromScore(maxToxic, "API toxic-bert: " + bestLabel);
    }

    private boolean isToxicLabel(String label) {
        if (label.contains("non_toxic") || label.contains("non-toxic") || label.contains("not_toxic")) {
            return false;
        }
        return label.contains("toxic")
                || label.contains("obscene")
                || label.contains("threat")
                || label.contains("insult")
                || label.contains("hate")
                || label.contains("attack")
                || label.contains("explicit");
    }

    private Result analyserLocalement(String texte) {
        String lower = texte.toLowerCase();
        int hits = 0;
        for (String word : TOXIC_WORDS) {
            if (lower.contains(word)) hits++;
        }
        double score = Math.min(1.0, hits * 0.35);
        return resultFromScore(score, hits > 0 ? "Fallback local: mots sensibles detectes." : "Fallback local: contenu accepte automatiquement.");
    }

    private Result resultFromScore(double score, String sourceMessage) {
        if (score >= 0.65) {
            return new Result("refuse", score, "Moderation: contenu refuse automatiquement. " + sourceMessage);
        }
        if (score >= 0.45) {
            return new Result("en_attente", score, "Moderation: verification manuelle recommandee. " + sourceMessage);
        }
        return new Result("accepte", score, "Moderation: contenu accepte automatiquement. " + sourceMessage);
    }

    private boolean containsAny(String text, List<String> words) {
        for (String word : words) {
            if (text.contains(word)) return true;
        }
        return false;
    }

    private static class Input {
        public final String inputs;

        private Input(String inputs) {
            this.inputs = inputs;
        }
    }

    public record Result(String statut, double score, String message) {
    }
}
