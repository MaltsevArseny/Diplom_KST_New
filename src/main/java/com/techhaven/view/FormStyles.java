package com.techhaven.view;

/**
 * Централизованные inline-стили форм (HBox / TextField) для состояний
 * normal / error. Используется в LoginView, RegisterView, CheckoutView
 * для единообразного оформления password-box и полей ввода с валидацией.
 *
 * <p>Содержит ТОЛЬКО строки CSS, использующие переменные {@code -th-*}.
 * Раньше эти константы дублировались в каждом view-классе — изменение цвета
 * требовало правки в 3 местах. Теперь — одна точка.</p>
 */
public final class FormStyles {

    private FormStyles() {}

    /** Стиль HBox-контейнера password-поля в нормальном состоянии. */
    public static final String BOX_NORMAL =
        "-fx-background-color: -th-bg-card;" +
        "-fx-background-radius: 8;" +
        "-fx-border-color: -th-border;" +
        "-fx-border-radius: 8;" +
        "-fx-padding: 0 12 0 0;";

    /** Стиль HBox-контейнера password-поля в состоянии ошибки. */
    public static final String BOX_ERROR =
        "-fx-background-color: -th-bg-card;" +
        "-fx-background-radius: 8;" +
        "-fx-border-color: -th-danger;" +
        "-fx-border-radius: 8;" +
        "-fx-padding: 0 12 0 0;" +
        "-fx-border-width: 2;";

    /** Стиль TextField (без фона — наследует от родительского HBox) в норме. */
    public static final String FIELD_NORMAL =
        "-fx-border-color: -th-border;" +
        "-fx-border-radius: 8;" +
        "-fx-background-radius: 8;";

    /** Стиль TextField в состоянии ошибки. */
    public static final String FIELD_ERROR =
        "-fx-border-color: -th-danger;" +
        "-fx-border-radius: 8;" +
        "-fx-background-radius: 8;" +
        "-fx-border-width: 2;";
}
