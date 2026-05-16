package com.techhaven.view.component;

import com.techhaven.service.UndoService;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationPanel extends VBox {
    private static NotificationPanel instance;
    private Timeline countdownTimeline;
    private FadeTransition fadeOut;

    private NotificationPanel() {
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(12, 20, 12, 20));
        this.setStyle("-fx-background-color: #3f3f46; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 4);");
        this.setMaxWidth(400);
        this.setMaxHeight(70);
        this.setVisible(false);
        this.setOpacity(0);
        
        // Ensure it doesn't block clicks when hidden
        this.setPickOnBounds(false); 
    }

    public static synchronized NotificationPanel getInstance() {
        if (instance == null) {
            instance = new NotificationPanel();
        }
        return instance;
    }

    public void showUndoableAction(String message, Class<?> entityType, int entityId, Runnable actionToExecute, Runnable onUIUpdate) {
        this.getChildren().clear();
        this.setPickOnBounds(true);

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-text-fill: -th-cream; -fx-font-size: 14px;");

        Button undoBtn = new Button("Отменить");
        undoBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #34d399; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
        undoBtn.setOnAction(e -> {
            UndoService.getInstance().undo();
            hideNotification();
            if (onUIUpdate != null) {
                onUIUpdate.run();
            }
        });

        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.getChildren().addAll(msgLabel, undoBtn);

        ProgressBar progressBar = new ProgressBar(1.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(6);
        progressBar.setStyle("-fx-accent: #34d399; -fx-control-inner-background: #27272a;");

        VBox contentBox = new VBox(8, topBox, progressBar);
        this.getChildren().add(contentBox);

        this.setVisible(true);
        this.toFront();
        FadeTransition ftIn = new FadeTransition(Duration.millis(300), this);
        ftIn.setFromValue(this.getOpacity());
        ftIn.setToValue(1);
        ftIn.play();

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        countdownTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 1.0)),
            new KeyFrame(Duration.seconds(5), new KeyValue(progressBar.progressProperty(), 0.0))
        );
        countdownTimeline.play();

        UndoService.getInstance().requestAction(
            entityType, entityId,
            actionToExecute,
            () -> { // On Complete
                hideNotification();
                if (onUIUpdate != null) {
                    onUIUpdate.run();
                }
            },
            () -> {} // On Undo (handled by button above)
        );
    }

    public void hideNotification() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        if (fadeOut != null) {
            fadeOut.stop();
        }
        this.setPickOnBounds(false);
        fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(this.getOpacity());
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> this.setVisible(false));
        fadeOut.play();
    }
}
