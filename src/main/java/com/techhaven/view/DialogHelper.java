package com.techhaven.view;

import com.techhaven.MainApp;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Утилита создания стилизованных диалоговых окон приложения.
 * Единый тёмный стиль, закруглённые края и кнопка × в абсолютном верхнем правом углу.
 */
public final class DialogHelper {

    private DialogHelper() {}

    // ─── Stage ──────────────────────────────────────────────────────────────

    public static Stage createStage(Stage owner, boolean modal) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        if (modal && owner != null)
            stage.initModality(Modality.WINDOW_MODAL);
        else if (modal)
            stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null)
            stage.initOwner(owner);
        if (MainApp.getAppIcon() != null)
            stage.getIcons().add(MainApp.getAppIcon());
        stage.setResizable(false);
        return stage;
    }

    // ─── Кнопка × ────────────────────────────────────────────────────────────

    /**
     * Создаёт стандартную кнопку закрытия окна для передачи в applyTransparentSceneAndWait.
     * Кнопка будет помещена в абсолютный верхний правый угол окна (overlay через StackPane).
     */
    public static Button createCloseButton(Stage stage) {
        Button btn = new Button("×");
        btn.setStyle(
            "-fx-background-color: rgba(58,58,80,0.85);" +
            "-fx-text-fill: #a0a0b8;" +
            "-fx-font-size: 18px;" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 28px; -fx-min-height: 28px;" +
            "-fx-max-width: 28px; -fx-max-height: 28px;" +
            "-fx-cursor: hand; -fx-padding: 0;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #ef4444;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 28px; -fx-min-height: 28px;" +
            "-fx-max-width: 28px; -fx-max-height: 28px;" +
            "-fx-cursor: hand; -fx-padding: 0;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: rgba(58,58,80,0.85);" +
            "-fx-text-fill: #a0a0b8;" +
            "-fx-font-size: 18px;" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 28px; -fx-min-height: 28px;" +
            "-fx-max-width: 28px; -fx-max-height: 28px;" +
            "-fx-cursor: hand; -fx-padding: 0;"
        ));
        btn.setOnAction(e -> stage.close());
        btn.setTooltip(new javafx.scene.control.Tooltip("Закрыть"));
        return btn;
    }

    // ─── Применение прозрачной сцены ────────────────────────────────────────

    /**
     * Неблокирующий показ. Автоматически клипирует карточку (скруглённые углы)
     * и при необходимости накладывает кнопку × в верхнем правом углу.
     *
     * @param card     корневой узел диалога
     * @param stage    целевой Stage
     * @param closeBtn кнопка × (опционально, из createCloseButton)
     */
    public static void applyTransparentScene(Region card, Stage stage, Button... closeBtn) {
        applyClip(card);
        StackPane wrapper = buildWrapper(card, closeBtn.length > 0 ? closeBtn[0] : null);
        stage.setScene(buildScene(wrapper));
        stage.show();
        stage.centerOnScreen();
        clearBackground(wrapper);
    }

    /**
     * Блокирующий вариант (showAndWait).
     */
    public static void applyTransparentSceneAndWait(Region card, Stage stage, Button... closeBtn) {
        applyClip(card);
        StackPane wrapper = buildWrapper(card, closeBtn.length > 0 ? closeBtn[0] : null);
        stage.setScene(buildScene(wrapper));
        clearBackground(wrapper);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();
    }

    // ─── Стиль карточки ─────────────────────────────────────────────────────

    public static String cardStyle() {
        return "-fx-background-color: #1e1e2e;" +
               "-fx-background-radius: 16;" +
               "-fx-border-color: #3a3a50;" +
               "-fx-border-radius: 16;" +
               "-fx-border-width: 1;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 28, 0, 0, 10);";
    }

    // ─── Стилизованные уведомления ───────────────────────────────────────────

    public static void showInfo(String title, String message) {
        showNotification("✅", title, message, "#10b981");
    }

    public static void showError(String title, String message) {
        showNotification("❌", title, message, "#ef4444");
    }

    public static void showWarning(String title, String message) {
        showNotification("⚠", title, message, "#f59e0b");
    }

    private static void showNotification(String icon, String title, String message, String accent) {
        Stage stage = createStage(MainApp.getPrimaryStage(), true);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:34px;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#e2e8f0;");
        titleLbl.setWrapText(true);

        Label msgLbl = new Label(message);
        msgLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#a0a0b8;");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(300);

        VBox textBox = new VBox(5, titleLbl, msgLbl);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox body = new HBox(14, iconLbl, textBox);
        body.setAlignment(Pos.CENTER_LEFT);

        Button okBtn = new Button("OK");
        okBtn.setStyle(
            "-fx-background-color:" + accent + ";" +
            "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:8 32;-fx-cursor:hand;"
        );
        okBtn.setOnAction(e -> stage.close());
        okBtn.setDefaultButton(true);

        HBox btnRow = new HBox(okBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(14, 0, 0, 0));

        // Акцентная полоса слева
        Region accentBar = new Region();
        accentBar.setMinWidth(4);
        accentBar.setMaxWidth(4);
        accentBar.setStyle("-fx-background-color:" + accent + ";-fx-background-radius:12 0 0 12;");

        VBox inner = new VBox(body, btnRow);
        inner.setPadding(new Insets(20, 20, 20, 16));
        HBox.setHgrow(inner, Priority.ALWAYS);

        HBox card = new HBox(accentBar, inner);
        card.setMaxWidth(420);
        card.setStyle(cardStyle());

        // × в абсолютном правом верхнем углу
        Button closeX = createCloseButton(stage);

        applyTransparentSceneAndWait(card, stage, closeX);
    }

    // ─── Внутренние утилиты ──────────────────────────────────────────────────

    /** Клип с rounded corners — единственный способ скруглить все 4 угла в JavaFX. */
    private static void applyClip(Region card) {
        card.layoutBoundsProperty().addListener((obs, old, b) -> {
            if (b.getWidth() > 0 && b.getHeight() > 0) {
                Rectangle clip = new Rectangle(b.getWidth(), b.getHeight());
                clip.setArcWidth(32);
                clip.setArcHeight(32);
                card.setClip(clip);
            }
        });
    }

    /**
     * Строит wrapper StackPane с прозрачным фоном.
     * Если closeBtn != null — помещает его в TOP_RIGHT с отступом 8px.
     */
    private static StackPane buildWrapper(Region card, Button closeBtn) {
        StackPane wrapper = new StackPane(card);
        wrapper.setBackground(Background.EMPTY);
        wrapper.setStyle("-fx-background-color: transparent;");
        if (closeBtn != null) {
            StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(closeBtn, new Insets(8, 8, 0, 0));
            wrapper.getChildren().add(closeBtn);
        }
        return wrapper;
    }

    private static Scene buildScene(StackPane wrapper) {
        Scene scene = new Scene(wrapper);
        scene.setFill(Color.TRANSPARENT);
        try {
            scene.getStylesheets().add(
                DialogHelper.class.getResource("/styles/dark-theme.css").toExternalForm());
        } catch (Exception ignored) {}
        return scene;
    }

    private static void clearBackground(StackPane wrapper) {
        Platform.runLater(() -> {
            wrapper.setBackground(Background.EMPTY);
            wrapper.setStyle("-fx-background-color: transparent;");
            Platform.runLater(() -> {
                wrapper.setBackground(Background.EMPTY);
                wrapper.setStyle("-fx-background-color: transparent;");
            });
        });
    }
}
