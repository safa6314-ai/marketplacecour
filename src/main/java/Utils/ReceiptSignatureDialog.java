package Utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Modal signature pad shown after a successful payment; snapshot is used on the PDF receipt.
 */
public final class ReceiptSignatureDialog {

    private ReceiptSignatureDialog() {}

    /**
     * @return snapshot of the signature if the user clicks export; {@code null} if they close or skip
     */
    public static WritableImage showAndCapture(Window owner) {
        AtomicReference<WritableImage> result = new AtomicReference<>();
        AtomicBoolean hasInk = new AtomicBoolean(false);

        Stage stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Signature — Reçu de paiement");

        Canvas canvas = new Canvas(520, 180);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.web("#1e1b4b"));
        gc.setLineWidth(2.2);

        final double[] last = { -1, -1 };
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            last[0] = e.getX();
            last[1] = e.getY();
        });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (last[0] >= 0) {
                gc.strokeLine(last[0], last[1], e.getX(), e.getY());
                last[0] = e.getX();
                last[1] = e.getY();
                hasInk.set(true);
            }
        });

        Button clear = new Button("Effacer");
        clear.setOnAction(ev -> {
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setStroke(Color.web("#1e1b4b"));
            hasInk.set(false);
            last[0] = -1;
        });

        Button export = new Button("Télécharger le PDF");
        export.setDefaultButton(true);
        export.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold;");
        export.setOnAction(ev -> {
            if (!hasInk.get()) {
                NotificationHelper.warning("Signature requise", "Signez dans la zone blanche avant d'exporter.");
                return;
            }
            WritableImage snap = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, snap);
            result.set(snap);
            stage.close();
        });

        Button skip = new Button("Plus tard");
        skip.setOnAction(ev -> stage.close());

        HBox actions = new HBox(10, clear, skip, export);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        Label hint = new Label("Signez ci-dessous, puis cliquez sur « Télécharger le PDF » pour enregistrer votre reçu signé.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #475569;");

        VBox root = new VBox(12, hint, canvas, actions);
        root.setPadding(new Insets(20));
        root.setPrefWidth(560);

        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();

        return result.get();
    }
}
