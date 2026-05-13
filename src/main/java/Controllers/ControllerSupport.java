package Controllers;

import Entities.Question;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.Locale;

final class ControllerSupport {

    private ControllerSupport() {
    }

    static Label creerBadge(String texte, String styleClass) {
        Label badge = new Label(texte == null || texte.isBlank() ? "Non defini" : texte);
        badge.getStyleClass().addAll("badge", styleClass);
        return badge;
    }

    static Label creerMessageVide(String texte) {
        Label label = new Label(texte);
        label.getStyleClass().add("empty-label");
        return label;
    }

    static void appliquerAnimationCarte(Node card) {
        card.setOnMouseEntered(event -> animerCarte(card, 1.01));
        card.setOnMouseExited(event -> animerCarte(card, 1.0));
    }

    static void animerCarte(Node card, double scale) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(120), card);
        transition.setToX(scale);
        transition.setToY(scale);
        transition.play();
    }

    static String normaliser(String texte) {
        return normaliserEspaces(texte).toLowerCase(Locale.ROOT);
    }

    static String normaliserEspaces(String texte) {
        return texte == null ? "" : texte.trim().replaceAll("\\s+", " ");
    }

    static String normaliserNiveau(String texte) {
        String niveau = normaliser(texte);
        if (niveau.equals("facile")) {
            return "Facile";
        }
        if (niveau.equals("moyen")) {
            return "Moyen";
        }
        if (niveau.equals("difficile")) {
            return "Difficile";
        }
        return normaliserEspaces(texte);
    }

    static boolean questionValidePourAffichage(Question question) {
        return question != null
                && texteQuestionValide(question.getContenu(), 5)
                && texteSignificatif(question.getCategorie())
                && niveauValide(question.getNiveau());
    }

    static boolean texteQuestionValide(String texte, int tailleMin) {
        String valeur = normaliserEspaces(texte);
        return valeur.length() >= tailleMin
                && texteSignificatif(valeur)
                && !valeur.matches(".*[?.!]{4,}.*");
    }

    static boolean texteSignificatif(String texte) {
        String valeur = normaliserEspaces(texte);
        String compact = valeur.replaceAll("[\\s\\p{Punct}]+", "");
        return !valeur.isBlank()
                && valeur.matches(".*\\p{L}.*")
                && !valeur.matches("\\d+")
                && !valeur.matches("[\\p{Punct}\\p{S}\\s]+")
                && compact.length() >= 2
                && !compact.matches("(?iu)(.)\\1{2,}");
    }

    static boolean niveauValide(String texte) {
        String niveau = normaliser(texte);
        return niveau.equals("facile") || niveau.equals("moyen") || niveau.equals("difficile");
    }

    static void changerInterface(ActionEvent event, String fxml) {
        try {
            NavigationUtil.changerInterface((Node) event.getSource(), fxml);
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Navigation", e.getMessage());
        }
    }

    static void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
