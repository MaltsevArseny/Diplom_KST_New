package com.techhaven.view;

import com.techhaven.MainApp;
import com.techhaven.security.SecurityManager;
import com.techhaven.service.AuthService;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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

public class RegisterView {
    private final AuthService authService = new AuthService();
    private TextField usernameField, emailField, phoneField;
    private PasswordField passwordField, confirmPasswordField;
    private TextField passwordTextField, confirmPasswordTextField;
    private boolean passwordVisible = false;
    private boolean confirmVisible = false;

    private Label usernameError, emailError, phoneError, passwordError, confirmError, generalError;
    private HBox passwordBox, confirmBox;

    // Для перетаскивания окна
    private double dragX, dragY;

    // Стили полей — единые с LoginView
    private static final String FIELD_NORMAL = "-fx-background-color: -th-bg-card; -fx-border-color: -th-border; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: -th-text-primary; -fx-padding: 8 12;";
    private static final String FIELD_ERROR  = "-fx-background-color: -th-bg-card; -fx-border-color: -th-danger; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: -th-text-primary; -fx-padding: 8 12; -fx-border-width: 2;";
    private static final String FIELD_OK     = "-fx-background-color: -th-bg-card; -fx-border-color: #22c55e; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: -th-text-primary; -fx-padding: 8 12; -fx-border-width: 2;";
    private static final String BOX_NORMAL   = "-fx-background-color: -th-bg-card; -fx-background-radius: 8; -fx-border-color: -th-border; -fx-border-radius: 8; -fx-padding: 0 12 0 0;";
    private static final String BOX_ERROR    = "-fx-background-color: -th-bg-card; -fx-background-radius: 8; -fx-border-color: -th-danger; -fx-border-radius: 8; -fx-padding: 0 12 0 0; -fx-border-width: 2;";
    private static final String BOX_OK       = "-fx-background-color: -th-bg-card; -fx-background-radius: 8; -fx-border-color: #22c55e; -fx-border-radius: 8; -fx-padding: 0 12 0 0; -fx-border-width: 2;";

    public Parent getView() {
        // Корень — без отступов
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: -th-bg-primary;");

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
        VBox card = new VBox(12);
        card.getStyleClass().add("auth-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(0, 40, 30, 40));
        // Логотип — компактный
        javafx.scene.image.Image logoImg = MainApp.getAppIcon();
        if (logoImg != null) {
            ImageView logo = new ImageView(logoImg);
            logo.setFitWidth(130);
            logo.setFitHeight(52);
            logo.setPreserveRatio(true);
            card.getChildren().add(logo);
        }

        Label title = new Label("Регистрация");
        title.getStyleClass().add("heading");
        title.setStyle("-fx-text-fill: -th-accent-light; -fx-font-size: 24px;");

        Label subtitle = new Label("Создайте аккаунт для покупок");
        subtitle.getStyleClass().add("label-secondary");

        // === Имя пользователя ===
        usernameField = new TextField();
        usernameField.setPromptText("Имя пользователя (3-50 символов)");
        usernameField.setPrefHeight(44);
        usernameField.setMaxWidth(360);
        usernameField.setStyle(FIELD_NORMAL);
        usernameError = createErrorHint();
        usernameField.textProperty().addListener((obs, o, n) -> {
            clearFieldError(usernameField, usernameError);
            if (n.length() >= 3) usernameField.setStyle(FIELD_OK);
        });

        // === Email ===
        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(44);
        emailField.setMaxWidth(360);
        emailField.setStyle(FIELD_NORMAL);
        emailError = createErrorHint();
        emailField.textProperty().addListener((obs, o, n) -> {
            clearFieldError(emailField, emailError);
            if (SecurityManager.isValidEmail(n)) emailField.setStyle(FIELD_OK);
        });

        // === Телефон ===
        phoneField = new TextField();
        phoneField.setPromptText("+7XXXXXXXXXX");
        phoneField.setPrefHeight(44);
        phoneField.setMaxWidth(360);
        phoneField.setStyle(FIELD_NORMAL);
        phoneError = createErrorHint();
        phoneField.textProperty().addListener((obs, o, n) -> {
            clearFieldError(phoneField, phoneError);
            if (SecurityManager.isValidPhone(n)) phoneField.setStyle(FIELD_OK);
        });

        // === Пароль с глазом ===
        passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        passwordField.setPrefHeight(42);

        passwordTextField = new TextField();
        passwordTextField.setPromptText("Пароль");
        passwordTextField.setPrefHeight(42);
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        Label eyeIcon1 = new Label("\uD83D\uDC41");
        eyeIcon1.setStyle("-fx-font-size: 18px; -fx-cursor: hand; -fx-text-fill: -th-text-muted; -fx-padding: 0 0 0 8;");
        eyeIcon1.setOnMouseClicked(e -> {
            passwordVisible = !passwordVisible;
            toggleField(passwordField, passwordTextField, eyeIcon1, passwordVisible);
        });

        passwordBox = createPasswordBoxWidget(passwordField, passwordTextField, eyeIcon1);
        passwordError = createErrorHint();
        passwordField.textProperty().addListener((obs, o, n) -> {
            String err = SecurityManager.validatePassword(n);
            if (err == null) {
                passwordBox.setStyle(BOX_OK);
            } else {
                passwordBox.setStyle(BOX_NORMAL);
            }
            passwordError.setVisible(false);
            passwordError.setManaged(false);
        });

        // Подсказки по паролю
        Label passwordHint = new Label("Мин. 8 символов, заглавная, строчная, цифра, спецсимвол");
        passwordHint.setStyle("-fx-text-fill: -th-text-muted; -fx-font-size: 10px; -fx-padding: -4 0 0 4;");
        passwordHint.setWrapText(true);
        passwordHint.setMaxWidth(360);

        // === Подтверждение пароля с глазом ===
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтвердите пароль");
        confirmPasswordField.setPrefHeight(42);

        confirmPasswordTextField = new TextField();
        confirmPasswordTextField.setPromptText("Подтвердите пароль");
        confirmPasswordTextField.setPrefHeight(42);
        confirmPasswordTextField.setVisible(false);
        confirmPasswordTextField.setManaged(false);
        confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        Label eyeIcon2 = new Label("\uD83D\uDC41");
        eyeIcon2.setStyle("-fx-font-size: 18px; -fx-cursor: hand; -fx-text-fill: -th-text-muted; -fx-padding: 0 0 0 8;");
        eyeIcon2.setOnMouseClicked(e -> {
            confirmVisible = !confirmVisible;
            toggleField(confirmPasswordField, confirmPasswordTextField, eyeIcon2, confirmVisible);
        });

        confirmBox = createPasswordBoxWidget(confirmPasswordField, confirmPasswordTextField, eyeIcon2);
        confirmError = createErrorHint();
        confirmPasswordField.textProperty().addListener((obs, o, n) -> {
            confirmBox.setStyle(BOX_NORMAL);
            confirmError.setVisible(false);
            confirmError.setManaged(false);
            if (!n.isEmpty() && n.equals(passwordField.getText())) {
                confirmBox.setStyle(BOX_OK);
            }
        });

        // Общая ошибка
        generalError = new Label();
        generalError.setWrapText(true);
        generalError.setMaxWidth(360);
        generalError.setStyle("-fx-text-fill: -th-danger; -fx-font-size: 12px; -fx-padding: 8 12; -fx-background-color: rgba(239,68,68,0.1); -fx-background-radius: 8;");
        generalError.setVisible(false);
        generalError.setManaged(false);

        Button registerBtn = new Button("Зарегистрироваться");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setPrefHeight(44);
        registerBtn.setTooltip(new javafx.scene.control.Tooltip("Создать аккаунт"));
        registerBtn.setPrefWidth(360);
        registerBtn.setOnAction(e -> handleRegister());

        confirmPasswordField.setOnAction(e -> handleRegister());
        confirmPasswordTextField.setOnAction(e -> handleRegister());

        Hyperlink loginLink = new Hyperlink("Уже есть аккаунт? Войти");
        loginLink.setOnAction(e -> {
            LoginView loginView = new LoginView();
            MainApp.setScene(loginView.getView(), 500, 620, false);
        });

        // GDPR consent checkbox (обязателен для регистрации)
        CheckBox gdprCheckBox = new CheckBox(
            "Я согласен(а) на обработку моих персональных данных в соответствии с Федеральным законом № 152-ФЗ"
        );
        gdprCheckBox.setWrapText(true);
        gdprCheckBox.setMaxWidth(360);
        gdprCheckBox.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 11px;");
        gdprCheckBox.setSelected(false);
        registerBtn.setDisable(true);

        Label gdprError = createErrorHint();
        gdprError.setText("Необходимо согласие на обработку персональных данных");

        gdprCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            registerBtn.setDisable(!newVal);
            if (newVal) {
                gdprError.setVisible(false);
                gdprError.setManaged(false);
            }
        });

        card.getChildren().addAll(title, subtitle, new Separator(),
            createFieldLabel("Имя пользователя"), usernameField, usernameError,
            createFieldLabel("Email"), emailField, emailError,
            createFieldLabel("Телефон"), phoneField, phoneError,
            createFieldLabel("Пароль"), passwordBox, passwordError, passwordHint,
            createFieldLabel("Подтверждение пароля"), confirmBox, confirmError,
            generalError, gdprCheckBox, gdprError, registerBtn, loginLink);

        root.getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);
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
        minimizeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -th-text-muted; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10;");
        minimizeBtn.setOnMouseEntered(e -> minimizeBtn.setStyle("-fx-background-color: -th-border; -fx-text-fill: -th-text-primary; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10; -fx-background-radius: 6;"));
        minimizeBtn.setOnMouseExited(e -> minimizeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -th-text-muted; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10;"));
        minimizeBtn.setTooltip(new javafx.scene.control.Tooltip("Свернуть"));
        minimizeBtn.setOnAction(e -> {
            Stage stage = (Stage) minimizeBtn.getScene().getWindow();
            stage.setIconified(true);
        });

        Button closeBtn = new Button("×");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -th-text-muted; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: -th-danger; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10; -fx-background-radius: 6;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -th-text-muted; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 2 10;"));
        closeBtn.setTooltip(new javafx.scene.control.Tooltip("Закрыть"));
        closeBtn.setOnAction(e -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });

        bar.getChildren().addAll(spacer, minimizeBtn, closeBtn);
        return bar;
    }

    private void handleRegister() {
        clearAllErrors();
        boolean hasError = false;

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || username.length() < 3 || username.length() > 50) {
            setFieldError(usernameField, usernameError, "Имя должно быть от 3 до 50 символов");
            hasError = true;
        }

        if (!SecurityManager.isValidEmail(email)) {
            setFieldError(emailField, emailError, email.isEmpty() ? "Введите email" : "Некорректный формат email");
            hasError = true;
        }

        if (!SecurityManager.isValidPhone(phone)) {
            setFieldError(phoneField, phoneError, phone.isEmpty() ? "Введите телефон" : "Формат: +7XXXXXXXXXX");
            hasError = true;
        }

        String passwordValidation = SecurityManager.validatePassword(password);
        if (passwordValidation != null) {
            setBoxError(passwordBox, passwordError, passwordValidation);
            hasError = true;
        }

        if (confirm.isEmpty()) {
            setBoxError(confirmBox, confirmError, "Подтвердите пароль");
            hasError = true;
        } else if (!password.equals(confirm)) {
            setBoxError(confirmBox, confirmError, "Пароли не совпадают");
            hasError = true;
        }

        if (hasError) {
            shakeNode(usernameField.getScene().getRoot());
            return;
        }

        AuthService.AuthResult result = authService.register(username, email, phone, password);

        if (result.success) {
            MainLayout mainLayout = new MainLayout();
            MainApp.setScene(mainLayout.getView());
        } else {
            String msg = result.message;
            if (msg.contains("Имя") || msg.contains("имя")) {
                setFieldError(usernameField, usernameError, msg);
            } else if (msg.contains("email") || msg.contains("Email") || msg.contains("существует")) {
                setFieldError(emailField, emailError, msg);
            } else if (msg.contains("елефон") || msg.contains("+7")) {
                setFieldError(phoneField, phoneError, msg);
            } else if (msg.contains("ароль") || msg.contains("Пароль")) {
                setBoxError(passwordBox, passwordError, msg);
            } else {
                generalError.setText("⚠ " + msg);
                generalError.setVisible(true);
                generalError.setManaged(true);
            }
            shakeNode(usernameField.getScene().getRoot());
        }
    }

    // ====== Вспомогательные методы ======

    private HBox createPasswordBoxWidget(PasswordField passField, TextField textField, Label eye) {
        HBox box = new HBox(0);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(360);
        box.setStyle(BOX_NORMAL);
        HBox.setHgrow(passField, Priority.ALWAYS);
        HBox.setHgrow(textField, Priority.ALWAYS);
        passField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: -th-text-primary;");
        textField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: -th-text-primary;");
        box.getChildren().addAll(passField, textField, eye);
        return box;
    }

    private void toggleField(PasswordField passField, TextField textField, Label eyeIcon, boolean visible) {
        if (visible) {
            passField.setVisible(false);
            passField.setManaged(false);
            textField.setVisible(true);
            textField.setManaged(true);
            eyeIcon.setText("\uD83D\uDC41\u200D\uD83D\uDDE8");
        } else {
            textField.setVisible(false);
            textField.setManaged(false);
            passField.setVisible(true);
            passField.setManaged(true);
            eyeIcon.setText("\uD83D\uDC41");
        }
    }

    private void setFieldError(TextField field, Label error, String message) {
        field.setStyle(FIELD_ERROR);
        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }

    private void setBoxError(HBox box, Label error, String message) {
        box.setStyle(BOX_ERROR);
        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }

    private void clearFieldError(TextField field, Label error) {
        field.setStyle(FIELD_NORMAL);
        error.setVisible(false);
        error.setManaged(false);
    }

    private void clearAllErrors() {
        usernameField.setStyle(FIELD_NORMAL);
        emailField.setStyle(FIELD_NORMAL);
        phoneField.setStyle(FIELD_NORMAL);
        passwordBox.setStyle(BOX_NORMAL);
        confirmBox.setStyle(BOX_NORMAL);
        for (Label err : new Label[]{usernameError, emailError, phoneError, passwordError, confirmError, generalError}) {
            err.setVisible(false);
            err.setManaged(false);
        }
    }

    private Label createErrorHint() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: -th-danger; -fx-font-size: 11px; -fx-padding: -4 0 0 4;");
        label.setWrapText(true);
        label.setMaxWidth(360);
        label.setVisible(false);
        label.setManaged(false);
        return label;
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
        label.setPadding(new Insets(0, 0, -6, 4));
        return label;
    }
}
