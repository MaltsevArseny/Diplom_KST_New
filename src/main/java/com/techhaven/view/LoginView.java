package com.techhaven.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import com.techhaven.MainApp;
import com.techhaven.security.SecurityManager;
import com.techhaven.service.AuthService;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginView {
    private final AuthService authService = new AuthService();
    // ComboBox вместо TextField — хранит историю email
    private ComboBox<String> emailCombo;
    private PasswordField passwordField;
    private TextField passwordTextField;
    private boolean passwordVisible = false;

    private Label emailError;
    private Label passwordError;
    private Label generalError;
    private HBox passwordBox;

    // Для перетаскивания окна
    private double dragX, dragY;

    private static final String BOX_NORMAL   = "-fx-background-color: #2a2a3e; -fx-background-radius: 8; -fx-border-color: #3f3f5a; -fx-border-radius: 8; -fx-padding: 0 12 0 0;";
    private static final String BOX_ERROR    = "-fx-background-color: #2a2a3e; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-radius: 8; -fx-padding: 0 12 0 0; -fx-border-width: 2;";

    public Parent getView() {
        // Корень окна — без отступов
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #1e1e2e;");

        // === Кастомная панель управления окном (в самом верху) ===
        HBox windowBar = createWindowBar();
        root.getChildren().add(windowBar);

        // Перетаскивание окна
        windowBar.setOnMousePressed(e -> {
            Stage stage = (Stage) root.getScene().getWindow();
            dragX = e.getScreenX() - stage.getX();
            dragY = e.getScreenY() - stage.getY();
        });
        windowBar.setOnMouseDragged(e -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setX(e.getScreenX() - dragX);
            stage.setY(e.getScreenY() - dragY);
        });

        // Контент формы с отступами
        VBox card = new VBox(16);
        card.getStyleClass().add("auth-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(0, 40, 40, 40));

        // Логотип
        javafx.scene.image.Image logoImg = MainApp.getAppIcon();
        if (logoImg != null) {
            ImageView logo = new ImageView(logoImg);
            logo.setFitWidth(200);
            logo.setFitHeight(80);
            logo.setPreserveRatio(true);
            card.getChildren().add(logo);
        }

        // Заголовок
        Label title = new Label("DigitalHub");
        title.getStyleClass().add("heading");
        title.setStyle("-fx-text-fill: #a78bfa;");

        Label subtitle = new Label("Магазин компьютерного оборудования");
        subtitle.getStyleClass().add("label-secondary");

        // Поле Email — ComboBox с историей
        Preferences prefs = Preferences.userRoot().node("com/techhaven/digitalhub");
        List<String> history = loadHistory(prefs);

        emailCombo = new ComboBox<>(FXCollections.observableArrayList(history));
        emailCombo.setEditable(true);
        emailCombo.setPromptText("Email");
        emailCombo.setPrefHeight(44);
        emailCombo.setMaxWidth(Double.MAX_VALUE);
        emailCombo.setStyle(
            "-fx-background-color:#2a2a3e;" +
            "-fx-border-color:#3f3f5a;" +
            "-fx-border-radius:8;" +
            "-fx-background-radius:8;" +
            "-fx-font-size:14px;"
        );
        // Стилизуем внутренний TextField ComboBox
        emailCombo.getEditor().setStyle("-fx-background-color:transparent;-fx-text-fill:#e2e8f0;-fx-prompt-text-fill:#9ca3af;");

        // Кнопка очистки истории
        Button clearHistory = new Button("×");
        clearHistory.setStyle("-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:0 4;");
        clearHistory.setTooltip(new javafx.scene.control.Tooltip("Очистить историю"));
        clearHistory.setOnAction(e -> {
            prefs.remove("email_history");
            emailCombo.getItems().clear();
            emailCombo.getEditor().clear();
        });

        HBox emailRow = new HBox(4, emailCombo, clearHistory);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        emailRow.setMaxWidth(340);
        HBox.setHgrow(emailCombo, Priority.ALWAYS);

        emailError = createErrorHint();
        emailCombo.getEditor().textProperty().addListener((obs, o, n) -> {
            emailCombo.getEditor().setStyle("-fx-background-color:transparent;-fx-text-fill:#e2e8f0;-fx-prompt-text-fill:#9ca3af;");
            emailError.setVisible(false); emailError.setManaged(false);
            generalError.setVisible(false); generalError.setManaged(false);
        });

        // При выборе email из списка — подставляем сохранённый пароль
        emailCombo.setOnAction(e -> {
            String sel = emailCombo.getValue();
            if (sel != null && !sel.isBlank()) {
                String enc = prefs.get(passKey(sel), "");
                if (!enc.isBlank()) {
                    try {
                        passwordField.setText(SecurityManager.getInstance().decrypt(enc));
                    } catch (Exception ignored) {}
                } else {
                    passwordField.clear();
                }
            }
        });

        // Пароль с глазом
        passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        passwordField.setPrefHeight(44);

        passwordTextField = new TextField();
        passwordTextField.setPromptText("Пароль");
        passwordTextField.setPrefHeight(44);
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        Label eyeIcon = new Label("\uD83D\uDC41");
        eyeIcon.setStyle("-fx-font-size: 18px; -fx-cursor: hand; -fx-text-fill: #9ca3af; -fx-padding: 0 0 0 8;");
        eyeIcon.setOnMouseClicked(e -> togglePasswordVisibility(eyeIcon));

        passwordBox = new HBox(0);
        passwordBox.setAlignment(Pos.CENTER_LEFT);
        passwordBox.setMaxWidth(340);
        passwordBox.setStyle(BOX_NORMAL);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(passwordTextField, Priority.ALWAYS);
        passwordField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #e2e8f0;");
        passwordTextField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #e2e8f0;");
        passwordBox.getChildren().addAll(passwordField, passwordTextField, eyeIcon);

        passwordError = createErrorHint();

        passwordField.textProperty().addListener((obs, o, n) -> {
            passwordBox.setStyle(BOX_NORMAL);
            passwordError.setVisible(false);
            passwordError.setManaged(false);
            generalError.setVisible(false);
            generalError.setManaged(false);
        });

        // Общая ошибка
        generalError = new Label();
        generalError.setWrapText(true);
        generalError.setMaxWidth(340);
        generalError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-padding: 8 12; -fx-background-color: rgba(239,68,68,0.1); -fx-background-radius: 8;");
        generalError.setVisible(false);
        generalError.setManaged(false);

        // Восстановление сохранённых данных Remember Me
        boolean remembered = prefs.getBoolean("remember_me", false);
        CheckBox rememberMe = new CheckBox("Запомнить меня");
        rememberMe.setSelected(remembered);
        rememberMe.setStyle("-fx-text-fill: #a0a0b8; -fx-font-size: 12px;");

        if (remembered) {
            String savedEmail = prefs.get("saved_email", "");
            if (!savedEmail.isEmpty()) {
                emailCombo.getEditor().setText(savedEmail);
                // Подставляем пароль по его ключу
                String enc = prefs.get(passKey(savedEmail), "");
                if (!enc.isBlank()) {
                    try {
                        passwordField.setText(SecurityManager.getInstance().decrypt(enc));
                    } catch (Exception ignored) {}
                }
            }
        }

        // Кнопка входа
        Button loginBtn = new Button("Войти");
        loginBtn.getStyleClass().addAll("btn-primary");
        loginBtn.setPrefHeight(44);
        loginBtn.setTooltip(new javafx.scene.control.Tooltip("Войти в аккаунт"));
        loginBtn.setPrefWidth(340);
        loginBtn.setOnAction(e -> handleLogin(rememberMe.isSelected(), prefs));

        // enter
        passwordField.setOnAction(e -> handleLogin(rememberMe.isSelected(), prefs));
        passwordTextField.setOnAction(e -> handleLogin(rememberMe.isSelected(), prefs));
        emailCombo.getEditor().setOnAction(e -> passwordField.requestFocus());
        loginBtn.setOnAction(e -> handleLogin(rememberMe.isSelected(), prefs));

        // Ссылка на регистрацию
        Hyperlink registerLink = new Hyperlink("Нет аккаунта? Зарегистрироваться");
        registerLink.setOnAction(e -> {
            RegisterView registerView = new RegisterView();
            MainApp.setScene(registerView.getView(), 500, 790, false);
        });

        card.getChildren().addAll(title, subtitle,
            new Separator(),
            createFieldLabel("Email"), emailRow, emailError,
            createFieldLabel("Пароль"), passwordBox, passwordError,
            generalError, rememberMe, loginBtn, registerLink);

        root.getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        javafx.application.Platform.runLater(() -> emailCombo.getEditor().requestFocus());

        return root;
    }

    /**
     * Кастомная панель управления окном (свернуть / закрыть)
     */
    private HBox createWindowBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(2, 2, 0, 0));
        bar.setStyle("-fx-cursor: move;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeBtn = new Button("_");
        minimizeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10;");
        minimizeBtn.setOnMouseEntered(e -> minimizeBtn.setStyle("-fx-background-color: #3f3f5a; -fx-text-fill: #e2e8f0; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10; -fx-background-radius: 6;"));
        minimizeBtn.setOnMouseExited(e -> minimizeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10;"));
        minimizeBtn.setTooltip(new javafx.scene.control.Tooltip("Свернуть"));
        minimizeBtn.setOnAction(e -> {
            Stage stage = (Stage) minimizeBtn.getScene().getWindow();
            stage.setIconified(true);
        });

        Button closeBtn = new Button("×");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10; -fx-background-radius: 6;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10;"));
        closeBtn.setTooltip(new javafx.scene.control.Tooltip("Закрыть"));
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });

        bar.getChildren().addAll(spacer, minimizeBtn, closeBtn);
        return bar;
    }

    private void handleLogin(boolean rememberMe, Preferences prefs) {
        clearAllErrors();

        String email = emailCombo.getEditor().getText().trim();
        String password = passwordField.getText();

        boolean hasError = false;

        if (email.isEmpty()) {
            emailCombo.getEditor().setStyle("-fx-background-color:transparent;-fx-text-fill:#ef4444;-fx-prompt-text-fill:#9ca3af;");
            emailError.setText("Введите email");
            emailError.setVisible(true); emailError.setManaged(true);
            hasError = true;
        }
        if (password.isEmpty()) {
            setBoxError(passwordBox, passwordError, "Введите пароль");
            hasError = true;
        }

        if (hasError) {
            shakeNode(emailCombo.getScene().getRoot());
            return;
        }

        AuthService.AuthResult result = authService.login(email, password);

        if (result.success) {
            // Сохраняем email в историю
            addToHistory(prefs, email);
            emailCombo.getItems().setAll(loadHistory(prefs));

            // Сохраняем пароль для этого email (encrypted)
            try {
                prefs.put(passKey(email), SecurityManager.getInstance().encrypt(password));
            } catch (Exception ignored) {}

            // Remember Me
            prefs.putBoolean("remember_me", rememberMe);
            if (rememberMe) {
                prefs.put("saved_email", email);
            } else {
                prefs.remove("saved_email");
            }

            if (result.user.isAdmin()) {
                AdminLayout adminLayout = new AdminLayout();
                MainApp.setScene(adminLayout.getView());
            } else {
                MainLayout mainLayout = new MainLayout();
                MainApp.setScene(mainLayout.getView());
            }
        } else {
            String msg = result.message;
            if (msg.contains("email") || msg.contains("найден")) {
                emailCombo.getEditor().setStyle("-fx-background-color:transparent;-fx-text-fill:#ef4444;");
                emailError.setText(msg); emailError.setVisible(true); emailError.setManaged(true);
            } else if (msg.contains("пароль") || msg.contains("Пароль") || msg.contains("попыток")) {
                setBoxError(passwordBox, passwordError, msg);
            } else {
                generalError.setText("⚠ " + msg);
                generalError.setVisible(true); generalError.setManaged(true);
            }
            shakeNode(emailCombo.getScene().getRoot());
        }
    }


    // ====== Вспомогательные методы ======

    private void setBoxError(HBox box, Label error, String message) {
        box.setStyle(BOX_ERROR);
        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }

    private void clearAllErrors() {
        emailCombo.getEditor().setStyle("-fx-background-color:transparent;-fx-text-fill:#e2e8f0;-fx-prompt-text-fill:#9ca3af;");
        passwordBox.setStyle(BOX_NORMAL);
        emailError.setVisible(false); emailError.setManaged(false);
        passwordError.setVisible(false); passwordError.setManaged(false);
        generalError.setVisible(false); generalError.setManaged(false);
    }

    // ─── История email ─────────────────────────────────────────────────────
    private static final String HISTORY_KEY = "email_history";
    private static final int    MAX_HISTORY  = 10;

    private List<String> loadHistory(Preferences prefs) {
        String raw = prefs.get(HISTORY_KEY, "");
        if (raw.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(";")));
    }

    private void addToHistory(Preferences prefs, String email) {
        List<String> list = loadHistory(prefs);
        list.remove(email);           // удаляем дубликат
        list.add(0, email);           // свежий в начало
        if (list.size() > MAX_HISTORY) list = list.subList(0, MAX_HISTORY);
        prefs.put(HISTORY_KEY, String.join(";", list));
    }

    /** Ключ Preferences для хранения пароля данного email. */
    private static String passKey(String email) {
        return "pass_KEY_" + email.replace("@", "_at_").replace(".", "_");
    }

    private Label createErrorHint() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: -8 0 0 4;");
        label.setWrapText(true);
        label.setMaxWidth(340);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private void togglePasswordVisibility(Label eyeIcon) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            eyeIcon.setText("\uD83D\uDC41\u200D\uD83D\uDDE8");
        } else {
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            eyeIcon.setText("\uD83D\uDC41");
        }
    }

    private void shakeNode(Node node) {
        if (node == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-secondary");
        label.setPadding(new Insets(0, 0, -12, 4));
        return label;
    }
}
