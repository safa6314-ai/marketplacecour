package Utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

/**
 * Opens the Stripe payment URL in the system's default browser and
 * shows a confirmation dialog so the user can report success or cancellation.
 * This replaces the WebView-based approach (which required javafx.web /
 * jdk.jsobject — modules unavailable on standard JDK 17 builds).
 */
public class StripePaymentWindow {

    public static void show(String url, Runnable onSuccess, Runnable onCancel) {
        // Open payment URL in the default system browser
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback: try runtime exec for environments without Desktop
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", url});
                } else {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show a modal confirmation dialog
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Paiement Sécurisé Stripe");
        stage.setResizable(false);

        Label icon    = new Label("💳");
        icon.setStyle("-fx-font-size: 36px;");

        Label title   = new Label("Paiement Stripe");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label message = new Label(
            "Votre navigateur a été ouvert avec la page de paiement Stripe.\n" +
            "Cliquez sur « Paiement effectué » après avoir finalisé la transaction\n" +
            "ou sur « Annuler » pour revenir en arrière."
        );
        message.setWrapText(true);
        message.setStyle("-fx-text-alignment: center; -fx-font-size: 13px;");

        Button btnSuccess = new Button("✅  Paiement effectué");
        btnSuccess.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                            "-fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand;");
        btnSuccess.setOnAction(e -> {
            stage.close();
            if (onSuccess != null) Platform.runLater(onSuccess);
        });

        Button btnCancel = new Button("✕  Annuler");
        btnCancel.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; " +
                           "-fx-font-size: 13px; -fx-padding: 8 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> {
            stage.close();
            if (onCancel != null) Platform.runLater(onCancel);
        });

        VBox root = new VBox(14, icon, title, message, btnSuccess, btnCancel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #1e1e2e;");

        Scene scene = new Scene(root, 460, 280);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
