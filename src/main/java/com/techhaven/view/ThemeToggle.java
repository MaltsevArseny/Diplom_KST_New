package com.techhaven.view;

import com.techhaven.config.ThemeManager;
import com.techhaven.config.ThemeManager.Theme;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Применение и переключение темы в JavaFX-сценах.
 * Логика «что такое тема» — в {@link ThemeManager} (config-слой, без JavaFX
 * зависимостей). Здесь — только склейка с javafx.scene.Scene.
 */

public final class ThemeToggle {

    private ThemeToggle() {}

    public static Button create(Scene scene) {
        Button btn = new Button();
        btn.getStyleClass().add("theme-toggle-button");
        btn.setTooltip(new Tooltip("Сменить тему  [Alt+T]"));
        refreshIcon(btn);
        btn.setOnAction(e -> toggleAndApply(scene, btn));
        return btn;
    }

    /**
     * Применить активную тему к сцене: убрать предыдущий theme-CSS из
     * stylesheets и подключить актуальный. Не трогает прочие подключённые CSS.
     */
    public static void applyTo(Scene scene) {
        if (scene == null) return;
        ThemeManager tm = ThemeManager.getInstance();
        String activeUrl = ThemeToggle.class.getResource(tm.getCssPath()).toExternalForm();
        scene.getStylesheets().removeIf(s ->
            s.endsWith("dark-theme.css") || s.endsWith("light-theme.css"));
        scene.getStylesheets().add(activeUrl);
    }

    /** Переключить тему, применить к сцене, обновить иконку кнопки. */
    public static void toggleAndApply(Scene scene, Button button) {
        ThemeManager.getInstance().toggle();
        applyTo(scene);
        if (button != null) refreshIcon(button);
    }

    /** Обновить иконку кнопки под текущую тему. */
    public static void refreshIcon(Button button) {
        if (button == null) return;
        Theme current = ThemeManager.getInstance().getCurrent();
        // Активна dark → показываем ☀ (намёк «переключиться на светлую»)
        // Активна light → показываем 🌙
        button.setText(current == Theme.DARK ? "☀  Светлая" : "🌙  Тёмная");
    }
}
