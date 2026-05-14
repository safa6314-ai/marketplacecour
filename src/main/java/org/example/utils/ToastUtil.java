package org.example.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ToastUtil {

    public static void show(Stage owner, String message) {
        Stage toastStage = new Stage();
        toastStage.initOwner(owner);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Label text = new Label(message);
        text.getStyleClass().add("toast-text");

        StackPane root = new StackPane(text);
        root.getStyleClass().add("toast");
        root.setOpacity(0);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(ToastUtil.class.getResource("/styles.css").toExternalForm());
        
        // Inherit theme from owner if possible
        if (owner.getScene().getRoot().getStyleClass().contains("dark-theme")) {
            root.getStyleClass().add("dark-theme");
        }

        toastStage.setScene(scene);

        toastStage.show();

        // Position at bottom center of owner
        toastStage.setX(owner.getX() + owner.getWidth() / 2 - toastStage.getWidth() / 2);
        toastStage.setY(owner.getY() + owner.getHeight() * 0.8);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.millis(2000));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition anim = new SequentialTransition(fadeIn, pause, fadeOut);
        anim.setOnFinished(e -> toastStage.close());
        anim.play();
    }
}
