package Utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;

public class StripePaymentWindow {

    public static void show(String url, Runnable onSuccess, Runnable onCancel) {
        openInBrowser(url);

        ButtonType paidButton = new ButtonType("Paiement termine", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "La page Stripe a ete ouverte dans votre navigateur.",
                paidButton,
                cancelButton
        );
        alert.setTitle("Paiement Stripe");
        alert.setHeaderText("Finalisez le paiement dans le navigateur");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == paidButton) {
            if (onSuccess != null) {
                Platform.runLater(onSuccess);
            }
        } else if (onCancel != null) {
            Platform.runLater(onCancel);
        }
    }

    private static void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'ouvrir la page de paiement: " + url, e);
        }
    }
}
