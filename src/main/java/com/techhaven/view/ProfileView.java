package com.techhaven.view;

import com.techhaven.config.SessionManager;
import com.techhaven.model.User;
import com.techhaven.repository.UserRepository;
import com.techhaven.security.SecurityManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProfileView {
    private final UserRepository userRepo = new UserRepository();

    public ProfileView(MainLayout mainLayout) {
        // mainLayout зарезервирован для возможного будущего использования
    }

    public Parent getView() {
        User user = SessionManager.getInstance().getCurrentUser();

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        Label heading = new Label("👤 Личный кабинет");
        heading.getStyleClass().add("heading");

        // ===== Карточка: Редактирование профиля =====
        VBox editCard = new VBox(14);
        editCard.getStyleClass().add("card");
        editCard.setPadding(new Insets(28));
        editCard.setMaxWidth(640);

        Label editTitle = new Label("Редактирование профиля");
        editTitle.getStyleClass().add("subheading");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        // Имя
        Label nameLabel = new Label("Имя пользователя");
        nameLabel.getStyleClass().add("label-muted");
        TextField nameField = new TextField(user.getUsername());
        nameField.setPrefWidth(280);
        nameField.setPrefHeight(38);

        // Email
        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add("label-muted");
        TextField emailField = new TextField(user.getEmail());
        emailField.setPrefWidth(280);
        emailField.setPrefHeight(38);

        // Телефон
        Label phoneLabel = new Label("Телефон");
        phoneLabel.getStyleClass().add("label-muted");
        TextField phoneField = new TextField(user.getPhone() != null ? user.getPhone() : "");
        phoneField.setPromptText("+7XXXXXXXXXX");
        phoneField.setPrefWidth(280);
        phoneField.setPrefHeight(38);

        grid.add(nameLabel,  0, 0); grid.add(nameField,  1, 0);
        grid.add(emailLabel, 0, 1); grid.add(emailField, 1, 1);
        grid.add(phoneLabel, 0, 2); grid.add(phoneField, 1, 2);

        Label profileStatus = new Label();
        profileStatus.setVisible(false);

        Button saveProfileBtn = new Button("💾 Сохранить данные");
        saveProfileBtn.getStyleClass().add("btn-primary");
        saveProfileBtn.setTooltip(new javafx.scene.control.Tooltip("Сохранить изменения профиля"));
        saveProfileBtn.setPrefHeight(40);
        saveProfileBtn.setOnAction(e -> {
            String newName  = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();

            if (newName.length() < 2) {
                showStatus(profileStatus, "❌ Имя должно быть не менее 2 символов", true);
                return;
            }
            if (!SecurityManager.isValidEmail(newEmail)) {
                showStatus(profileStatus, "❌ Некорректный формат email", true);
                return;
            }
            if (!newPhone.isEmpty() && !SecurityManager.isValidPhone(newPhone)) {
                showStatus(profileStatus, "❌ Формат телефона: +7XXXXXXXXXX", true);
                return;
            }
            // Проверка занятости email другим пользователем
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepo.emailExistsForOther(newEmail, user.getId())) {
                showStatus(profileStatus, "❌ Этот email уже используется другим аккаунтом", true);
                return;
            }
            boolean ok = userRepo.updateProfile(user.getId(), newName, newEmail, newPhone);
            if (ok) {
                user.setUsername(newName);
                user.setEmail(newEmail);
                user.setPhone(newPhone);
                showStatus(profileStatus, "✓ Данные профиля обновлены", false);
            } else {
                showStatus(profileStatus, "❌ Ошибка сохранения — попробуйте снова", true);
            }
        });

        editCard.getChildren().addAll(editTitle, new Separator(), grid, saveProfileBtn, profileStatus);

        // ===== Карточка: Смена пароля =====
        VBox passCard = new VBox(12);
        passCard.getStyleClass().add("card");
        passCard.setPadding(new Insets(24));
        passCard.setMaxWidth(640);

        Label passTitle = new Label("Смена пароля");
        passTitle.getStyleClass().add("subheading");

        PasswordField currentPassField = new PasswordField();
        currentPassField.setPromptText("Текущий пароль");
        currentPassField.setPrefHeight(38);
        currentPassField.setMaxWidth(340);

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Новый пароль");
        newPassField.setPrefHeight(38);
        newPassField.setMaxWidth(340);

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Подтвердите новый пароль");
        confirmPassField.setPrefHeight(38);
        confirmPassField.setMaxWidth(340);

        Label passHint = new Label("Мин. 8 символов, заглавная, строчная, цифра, спецсимвол");
        passHint.setStyle("-fx-text-fill: -th-text-muted; -fx-font-size: 10px;");

        Label passStatus = new Label();
        passStatus.setVisible(false);

        Button changePassBtn = new Button("🔒 Изменить пароль");
        changePassBtn.getStyleClass().addAll("button");
        changePassBtn.setTooltip(new javafx.scene.control.Tooltip("Сменить пароль учётной записи"));
        changePassBtn.setPrefHeight(40);
        changePassBtn.setOnAction(e -> {
            String currentPass = currentPassField.getText();
            String newPass     = newPassField.getText();
            String confirmPass = confirmPassField.getText();

            // Проверяем текущий пароль
            User freshUser = userRepo.findById(user.getId());
            if (freshUser == null || !SecurityManager.getInstance().verifyPassword(currentPass, freshUser.getPasswordHash())) {
                showStatus(passStatus, "❌ Неверный текущий пароль", true);
                return;
            }
            // Валидация нового пароля
            String passError = SecurityManager.validatePassword(newPass);
            if (passError != null) {
                showStatus(passStatus, "❌ " + passError, true);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                showStatus(passStatus, "❌ Пароли не совпадают", true);
                return;
            }
            String newHash = SecurityManager.getInstance().hashPassword(newPass);
            boolean ok = userRepo.updatePassword(user.getId(), newHash);
            if (ok) {
                user.setPasswordHash(newHash);
                currentPassField.clear();
                newPassField.clear();
                confirmPassField.clear();
                showStatus(passStatus, "✓ Пароль успешно изменён", false);
            } else {
                showStatus(passStatus, "❌ Ошибка обновления пароля", true);
            }
        });

        passCard.getChildren().addAll(passTitle, new Separator(),
            styledLabel("Текущий пароль"), currentPassField,
            styledLabel("Новый пароль"), newPassField, passHint,
            styledLabel("Подтверждение пароля"), confirmPassField,
            changePassBtn, passStatus);

        // ===== Карточка: Краткая информация =====
        VBox infoCard = new VBox(10);
        infoCard.getStyleClass().add("card");
        infoCard.setPadding(new Insets(20));
        infoCard.setMaxWidth(640);

        Label infoTitle = new Label("Информация аккаунта");
        infoTitle.getStyleClass().add("subheading");

        infoCard.getChildren().addAll(infoTitle, new Separator(),
            infoRow("Роль", user.isAdmin() ? "Администратор" : "Покупатель"),
            infoRow("Дата регистрации",
                user.getCreatedAt() != null ? user.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "—"));

        root.getChildren().addAll(heading, editCard, passCard, infoCard);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: -th-bg-primary; -fx-background: -th-bg-primary;");
        return scroll;
    }

    private void showStatus(Label label, String text, boolean isError) {
        label.setText(text);
        label.setStyle(isError
            ? "-fx-text-fill: -th-danger; -fx-font-size: 12px;"
            : "-fx-text-fill: -th-success; -fx-font-size: 12px;");
        label.setVisible(true);
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-muted");
        return l;
    }

    private HBox infoRow(String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":");
        lbl.getStyleClass().add("label-muted");
        lbl.setMinWidth(120);
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: -th-text-primary;");
        row.getChildren().addAll(lbl, val);
        return row;
    }
}
