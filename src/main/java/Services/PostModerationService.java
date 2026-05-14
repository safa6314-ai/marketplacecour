package Services;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class PostModerationService {

    private static final List<String> BLOCKED_TERMS = List.of(
            "insulte",
            "haine",
            "raciste",
            "terrorisme",
            "violence",
            "porn",
            "arnaque",
            "scam",
            "spam"
    );

    private static final List<String> REVIEW_TERMS = List.of(
            "urgent",
            "gratuit",
            "clique",
            "whatsapp",
            "telegram",
            "promotion",
            "vente rapide",
            "contactez moi"
    );

    public ModerationResult moderer(String contenu) {
        String texte = contenu == null ? "" : contenu.trim();
        if (texte.isBlank()) {
            return new ModerationResult("refuse", "Publication vide.");
        }
        if (texte.length() < 8) {
            return new ModerationResult("en_attente", "Texte trop court, verification admin requise.");
        }
        if (texte.length() > 500) {
            return new ModerationResult("refuse", "Publication trop longue.");
        }

        String normalise = normaliser(texte);
        for (String term : BLOCKED_TERMS) {
            if (normalise.contains(normaliser(term))) {
                return new ModerationResult("refuse", "Contenu refuse automatiquement: terme interdit detecte.");
            }
        }

        int reviewScore = 0;
        for (String term : REVIEW_TERMS) {
            if (normalise.contains(normaliser(term))) {
                reviewScore++;
            }
        }
        if (reviewScore >= 2 || repetitionExcessive(normalise) || liensExcessifs(normalise)) {
            return new ModerationResult("en_attente", "Contenu suspect: verification admin recommandee.");
        }

        return new ModerationResult("accepte", "Contenu accepte automatiquement.");
    }

    private boolean liensExcessifs(String texte) {
        int links = 0;
        int index = texte.indexOf("http");
        while (index >= 0) {
            links++;
            index = texte.indexOf("http", index + 4);
        }
        return links > 1;
    }

    private boolean repetitionExcessive(String texte) {
        return texte.matches(".*(.)\\1{7,}.*") || texte.matches(".*(!|\\?){5,}.*");
    }

    private String normaliser(String texte) {
        return Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public record ModerationResult(String statut, String raison) {
    }
}
