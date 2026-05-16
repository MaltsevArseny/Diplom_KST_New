package com.techhaven.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.techhaven.MainApp;
import com.techhaven.model.User;
import com.techhaven.repository.UserRepository;
import com.techhaven.security.SecurityManager;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminUsersView {
    private final UserRepository userRepo = new UserRepository();
    private final SecurityManager security = SecurityManager.getInstance();

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DB_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TableView<User> table;
    private List<User> allUsers;
    private TextField searchField;
    private ComboBox<String> roleFilter;
    private Label userBadge;
    private Label adminBadge;

    public AdminUsersView(AdminLayout adminLayout) {
        // adminLayout зарезервирован для возможного будущего использования
    }

    @SuppressWarnings("unchecked")
    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        Label heading = new Label("👥 Управление пользователями");
        heading.getStyleClass().add("heading");

        // ── Кнопка «Добавить администратора» ──────────────────────────
        Button addAdminBtn = new Button("🛡 Добавить администратора");
        addAdminBtn.setStyle(
            "-fx-background-color: -th-accent; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        addAdminBtn.setTooltip(new javafx.scene.control.Tooltip("Добавить нового администратора"));
        addAdminBtn.setOnAction(e -> showAddAdminDialog());

        // ── Поиск и фильтр ─────────────────────────────────────────────
        searchField = new TextField();
        searchField.setPromptText("🔍 Поиск по имени или email...");
        searchField.setPrefWidth(260);
        searchField.setStyle(
            "-fx-background-color: -th-bg-card; -fx-text-fill: -th-text-primary; -fx-border-color: -th-border;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 13px;"
        );
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());

        roleFilter = new ComboBox<>(FXCollections.observableArrayList(
            "Все роли", "👤 Покупатель (USER)", "🛡 Администратор (ADMIN)"
        ));
        roleFilter.setValue("Все роли");
        roleFilter.setPrefWidth(220);
        roleFilter.setOnAction(e -> applyFilter());

        Button clearBtn = new Button("× Сбросить");
        clearBtn.getStyleClass().addAll("button", "btn-small");
        clearBtn.setTooltip(new javafx.scene.control.Tooltip("Сбросить фильтры"));
        clearBtn.setOnAction(e -> { searchField.clear(); roleFilter.setValue("Все роли"); });

        Label countLabel = new Label();
        countLabel.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Статистические бейджи (активные учётные записи)
        userBadge = new Label();
        userBadge.setStyle(
            "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: -th-success;" +
            "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 12;" +
            "-fx-background-radius: 8; -fx-border-color: rgba(16,185,129,0.3);" +
            "-fx-border-radius: 8; -fx-border-width: 1;"
        );
        adminBadge = new Label();
        adminBadge.setStyle(
            "-fx-background-color: rgba(124,58,237,0.15); -fx-text-fill: -th-accent-light;" +
            "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 12;" +
            "-fx-background-radius: 8; -fx-border-color: rgba(124,58,237,0.3);" +
            "-fx-border-radius: 8; -fx-border-width: 1;"
        );

        HBox toolbar = new HBox(10, searchField, roleFilter, clearBtn, spacer, countLabel, userBadge, adminBadge, addAdminBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // ── Таблица ────────────────────────────────────────────────────
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-font-size: 13px;");

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40); idCol.setMinWidth(35);
        idCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        addTooltip(idCol, "Уникальный идентификатор пользователя");

        // Роль — цветной бейдж
        TableColumn<User, String> roleCol = new TableColumn<>("Роль");
        roleCol.setPrefWidth(145); roleCol.setMinWidth(120);
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setStyle("-fx-alignment: CENTER;");
        addTooltip(roleCol, "Роль пользователя в системе");
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setGraphic(null); setText(null); return; }
                boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
                Label badge = new Label(isAdmin ? "🛡 Администратор" : "👤 Покупатель");
                badge.setStyle(isAdmin
                    ? "-fx-background-color: rgba(124,58,237,0.18); -fx-text-fill: -th-accent-light;" +
                      "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 13px;"
                    : "-fx-background-color: rgba(16,185,129,0.12); -fx-text-fill: -th-success;" +
                      "-fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 13px;");
                setGraphic(badge); setText(null);
            }
        });

        // Имя — жирный для админов
        TableColumn<User, String> nameCol = new TableColumn<>("Имя пользователя");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameCol.setPrefWidth(145); nameCol.setMinWidth(110);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(nameCol, "Имя учётной записи");
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setText(null); return; }
                User u = getTableView().getItems().get(getIndex());
                boolean isAdmin = u != null && "ADMIN".equalsIgnoreCase(u.getRole());
                setText(name);
                setStyle(isAdmin
                    ? "-fx-font-weight: bold; -fx-text-fill: #c4b5fd;"
                    : "-fx-text-fill: -th-text-primary;");
            }
        });

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(210); emailCol.setMinWidth(160);
        emailCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(emailCol, "Адрес электронной почты");
        emailCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(""); return; }
                User u = getTableView().getItems().get(getIndex());
                boolean isAdmin = u != null && "ADMIN".equalsIgnoreCase(u.getRole());
                setText(isAdmin ? val : maskEmail(val));
                setStyle(isAdmin ? "-fx-text-fill: -th-text-primary;" : "-fx-text-fill: -th-text-muted;");
            }
        });

        TableColumn<User, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(130); phoneCol.setMinWidth(110);
        phoneCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(phoneCol, "Контактный телефон");
        phoneCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(""); return; }
                User u = getTableView().getItems().get(getIndex());
                boolean isAdmin = u != null && "ADMIN".equalsIgnoreCase(u.getRole());
                setText(isAdmin ? val : maskPhone(val));
                setStyle(isAdmin ? "-fx-text-fill: -th-text-primary;" : "-fx-text-fill: -th-text-muted;");
            }
        });

        // Дата регистрации — форматированная dd.MM.yyyy HH:mm
        TableColumn<User, Object> createdCol = new TableColumn<>("Дата регистрации");
        createdCol.setPrefWidth(145); createdCol.setMinWidth(120);
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        addTooltip(createdCol, "Дата и время регистрации аккаунта");
        createdCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Object val, boolean empty) {
                super.updateItem(val, empty);
                setAlignment(Pos.CENTER);
                if (empty || val == null) { setText(""); return; }
                setText(formatDate(val.toString()));
            }
        });

        // Дата последнего входа
        TableColumn<User, String> lastLoginCol = new TableColumn<>("Последний вход");
        lastLoginCol.setPrefWidth(145); lastLoginCol.setMinWidth(120);
        lastLoginCol.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        addTooltip(lastLoginCol, "Дата и время последнего входа");
        lastLoginCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setAlignment(Pos.CENTER);
                if (empty || val == null || val.isEmpty()) { setText(""); return; }
                setText(formatDate(val));
            }
        });

        // Причина блокировки
        TableColumn<User, String> blockReasonCol = new TableColumn<>("Причина блокировки");
        blockReasonCol.setCellValueFactory(new PropertyValueFactory<>("blockReason"));
        blockReasonCol.setPrefWidth(185); blockReasonCol.setMinWidth(120);
        blockReasonCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(blockReasonCol, "Причина блокировки аккаунта");
        blockReasonCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null || val.isEmpty()) { setText(""); setStyle(""); return; }
                setText(val);
                setStyle("-fx-text-fill: #f87171; -fx-font-size: 13px;");
            }
        });

        // Статус — красный/зелёный
        TableColumn<User, String> statusCol = new TableColumn<>("Статус");
        statusCol.setPrefWidth(100); statusCol.setMinWidth(80);
        statusCol.setCellValueFactory(new PropertyValueFactory<>("lockUntil"));
        statusCol.setStyle("-fx-alignment: CENTER;");
        addTooltip(statusCol, "Статус аккаунта (активен/заблокирован)");
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String lock, boolean empty) {
                super.updateItem(lock, empty);
                if (empty) { setGraphic(null); return; }
                boolean blocked = lock != null && !lock.isEmpty();
                Label lbl = new Label(blocked ? "🔒 Заблок." : "✅ Активен");
                lbl.setStyle(blocked
                    ? "-fx-text-fill: -th-danger; -fx-font-size: 13px;"
                    : "-fx-text-fill: -th-success; -fx-font-size: 13px;");
                setGraphic(lbl); setText(null);
            }
        });

        // Действия — иконки с хинтами
        TableColumn<User, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setPrefWidth(100); actionsCol.setMinWidth(90);
        addTooltip(actionsCol, "Блокировка и разблокировка пользователя");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button unlockBtn = new Button("🔓");
            private final Button lockBtn   = new Button("🔒");
            {
                String baseStyle =
                    "-fx-font-size: 16px; -fx-padding: 4 8;" +
                    "-fx-background-radius: 7; -fx-cursor: hand;";
                unlockBtn.setStyle(baseStyle +
                    "-fx-background-color: #059669; -fx-text-fill: white;");
                lockBtn.setStyle(baseStyle +
                    "-fx-background-color: #dc2626; -fx-text-fill: white;");
                unlockBtn.setTooltip(new Tooltip("Разблокировать пользователя"));
                lockBtn.setTooltip(new Tooltip("Заблокировать пользователя"));

                // hover-эффекты
                unlockBtn.setOnMouseEntered(e -> unlockBtn.setStyle(baseStyle +
                    "-fx-background-color: -th-success; -fx-text-fill: white;"));
                unlockBtn.setOnMouseExited(e -> {
                    if (!unlockBtn.isDisabled())
                        unlockBtn.setStyle(baseStyle + "-fx-background-color: #059669; -fx-text-fill: white;");
                });
                lockBtn.setOnMouseEntered(e -> lockBtn.setStyle(baseStyle +
                    "-fx-background-color: -th-danger; -fx-text-fill: white;"));
                lockBtn.setOnMouseExited(e -> {
                    if (!lockBtn.isDisabled())
                        lockBtn.setStyle(baseStyle + "-fx-background-color: #dc2626; -fx-text-fill: white;");
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                boolean isLocked = u.getFailedAttempts() >= 5 ||
                    (u.getLockUntil() != null && !u.getLockUntil().isEmpty());

                // Защита от самоблокировки
                int currentUserId = com.techhaven.config.SessionManager.getInstance().getCurrentUserId();
                boolean isSelf = u.getId() == currentUserId;

                String baseStyle =
                    "-fx-font-size: 16px; -fx-padding: 4 8;" +
                    "-fx-background-radius: 7; -fx-cursor: hand;";
                String disabledStyle = baseStyle +
                    "-fx-background-color: -th-border; -fx-text-fill: -th-text-muted; -fx-opacity: 0.6;";

                unlockBtn.setDisable(!isLocked || isSelf);
                lockBtn.setDisable(isLocked || isSelf);
                unlockBtn.setStyle(isLocked && !isSelf
                    ? baseStyle + "-fx-background-color: #059669; -fx-text-fill: white;"
                    : disabledStyle);
                lockBtn.setStyle(isLocked || isSelf
                    ? disabledStyle
                    : baseStyle + "-fx-background-color: #dc2626; -fx-text-fill: white;");

                if (isSelf) {
                    lockBtn.setTooltip(new Tooltip("Нельзя заблокировать собственную учётную запись"));
                    unlockBtn.setTooltip(new Tooltip("Нельзя манипулировать собственной учётной записью"));
                } else {
                    lockBtn.setTooltip(new Tooltip("Заблокировать пользователя"));
                    unlockBtn.setTooltip(new Tooltip("Разблокировать пользователя"));
                }

                unlockBtn.setOnAction(e -> { userRepo.unlockUser(u.getId()); loadUsers(); });
                lockBtn.setOnAction(e   -> AdminUsersView.this.showLockDialog(u));

                HBox box = new HBox(6, unlockBtn, lockBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(idCol, roleCol, nameCol, emailCol, phoneCol,
            createdCol, lastLoginCol, blockReasonCol, statusCol, actionsCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.itemsProperty().addListener((obs, old, items) -> {
            if (items != null)
                countLabel.setText("Отображается: " + items.size()
                    + (allUsers != null ? " из " + allUsers.size() : ""));
        });

        loadUsers();
        root.getChildren().addAll(heading, toolbar, table);
        return root;
    }

    /** Обновляет бейджи с количеством активных покупателей и администраторов */
    private void updateBadges() {
        if (allUsers == null || userBadge == null) return;
        long activeUsers = allUsers.stream()
            .filter(u -> "USER".equalsIgnoreCase(u.getRole()))
            .filter(u -> u.getLockUntil() == null || u.getLockUntil().isEmpty())
            .count();
        long activeAdmins = allUsers.stream()
            .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
            .filter(u -> u.getLockUntil() == null || u.getLockUntil().isEmpty())
            .count();
        userBadge.setText("👤 " + activeUsers);
        userBadge.setTooltip(new javafx.scene.control.Tooltip("Активных покупателей: " + activeUsers));
        adminBadge.setText("🛡 " + activeAdmins);
        adminBadge.setTooltip(new javafx.scene.control.Tooltip("Активных администраторов: " + activeAdmins));
    }

    // ── Диалог блокировки пользователя ────────────────────────────────
    private void showLockDialog(User user) {
        Stage dlg = DialogHelper.createStage(MainApp.getPrimaryStage(), true);

        Label title = new Label("🔒 Блокировка пользователя");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f87171;");
        Label who = new Label("👤 " + user.getUsername() + "   " + maskEmail(user.getEmail()));
        who.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 12px;");

        Region closeSpc = new Region(); HBox.setHgrow(closeSpc, Priority.ALWAYS);
        Button closeBtn = new Button("×");
        closeBtn.setTooltip(new javafx.scene.control.Tooltip("Закрыть"));
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -th-text-muted; -fx-font-size: 16px; -fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -th-danger; -fx-font-size: 16px; -fx-cursor: hand;"));
        closeBtn.setOnMouseExited(e  -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -th-text-muted; -fx-font-size: 16px; -fx-cursor: hand;"));
        closeBtn.setOnAction(e -> dlg.close());
        HBox titleBar = new HBox(8, title, closeSpc, closeBtn);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        Region divider = new Region(); divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: -th-border;");

        Label reasonLbl = new Label("Причина блокировки");
        reasonLbl.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 12px;");
        TextField reasonField = new TextField();
        reasonField.setPromptText("Например: Нарушение правил пользования (необязательно)");

        Button confirmBtn = new Button("🔒 Заблокировать");
        confirmBtn.setTooltip(new javafx.scene.control.Tooltip("Подтвердить блокировку пользователя"));
        confirmBtn.getStyleClass().addAll("button", "btn-danger");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> {
            userRepo.lockUser(user.getId(), reasonField.getText().trim());
            loadUsers();
            dlg.close();
        });

        Button cancelBtn = new Button("× Отмена");
        cancelBtn.setTooltip(new javafx.scene.control.Tooltip("Отменить и закрыть"));
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, cancelBtn, confirmBtn);
        HBox.setHgrow(confirmBtn, Priority.ALWAYS);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);

        VBox card = new VBox(14, titleBar, who, divider, reasonLbl, reasonField, btnRow);
        card.setPadding(new Insets(22));
        card.setPrefWidth(420);
        card.setStyle(DialogHelper.cardStyle());
        DialogHelper.applyTransparentScene(card, dlg);
    }

    // ── Кастомный диалог добавления администратора ────────────────────
    private void showAddAdminDialog() {
        Stage dlg = DialogHelper.createStage(MainApp.getPrimaryStage(), true);

        // ── Поля формы ─────────────────────────────────────────────────
        TextField nameField  = new TextField(); nameField.setPromptText("Имя пользователя");
        TextField emailField = new TextField(); emailField.setPromptText("admin@example.com");
        TextField phoneField = new TextField(); phoneField.setPromptText("+7XXXXXXXXXX (необязательно)");
        PasswordField passField  = new PasswordField(); passField.setPromptText("Минимум 8 символов");
        PasswordField pass2Field = new PasswordField(); pass2Field.setPromptText("Повторите пароль");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        // ── Сетка полей ────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPadding(new Insets(0));

        String[] labelTexts = {"Имя пользователя", "Email", "Телефон", "Пароль", "Повтор пароля"};
        javafx.scene.control.Control[] fields = {nameField, emailField, phoneField, passField, pass2Field};
        for (int i = 0; i < labelTexts.length; i++) {
            Label lbl = new Label(labelTexts[i]);
            lbl.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 12px;");
            lbl.setMinWidth(140);
            grid.add(lbl,    0, i);
            grid.add(fields[i], 1, i);
            GridPane.setHgrow(fields[i], Priority.ALWAYS);
        }
        grid.add(errorLabel, 0, labelTexts.length, 2, 1);

        // ── Кнопки ─────────────────────────────────────────────────────
        Button createBtn = new Button("✅ Создать администратора");
        createBtn.setTooltip(new javafx.scene.control.Tooltip("Создать новый аккаунт администратора"));
        createBtn.getStyleClass().addAll("button", "btn-primary");
        createBtn.setMaxWidth(Double.MAX_VALUE);

        Button cancelBtn = new Button("× Отмена");
        cancelBtn.setTooltip(new javafx.scene.control.Tooltip("Отменить и закрыть"));
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, cancelBtn, createBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(createBtn, Priority.ALWAYS);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);

        // ── Заголовок ──────────────────────────────────────────────────
        Label title = new Label("🛡 Новый администратор");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -th-accent-light;");
        Label subtitle = new Label("Заполните данные для создания аккаунта");
        subtitle.setStyle("-fx-text-fill: -th-text-muted; -fx-font-size: 12px;");

        Region closeSpacer = new Region();
        HBox.setHgrow(closeSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("×");
        closeBtn.setTooltip(new javafx.scene.control.Tooltip("Закрыть"));
        closeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: -th-text-muted;" +
            "-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 4;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: -th-danger;" +
            "-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 4;"
        ));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: -th-text-muted;" +
            "-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 4;"
        ));
        closeBtn.setOnAction(e -> dlg.close());

        HBox titleBar = new HBox(8, title, closeSpacer, closeBtn);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        // Разделитель
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: -th-border;");

        // ── Корневой контейнер ─────────────────────────────────────────
        VBox card = new VBox(16, titleBar, subtitle, divider, grid, errorLabel, btnRow);
        // убираем errorLabel из grid (он уже в grid), удаляем дубль:
        card.getChildren().remove(errorLabel);
        card.getChildren().add(4, errorLabel);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: -th-bg-primary; -fx-background-radius: 14;" +
            "-fx-border-color: -th-border; -fx-border-radius: 14; -fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 24, 0, 0, 8);"
        );
        card.setPrefWidth(480);

        // ── Логика создания ────────────────────────────────────────────
        createBtn.setOnAction(e -> {
            String username = nameField.getText().trim();
            String email    = emailField.getText().trim().toLowerCase();
            String pass     = passField.getText();
            String pass2    = pass2Field.getText();

            if (username.length() < 3) {
                errorLabel.setText("Имя пользователя: минимум 3 символа"); return;
            }
            if (!email.contains("@")) {
                errorLabel.setText("Некорректный email"); return;
            }
            if (!pass.equals(pass2)) {
                errorLabel.setText("Пароли не совпадают"); return;
            }
            String passError = SecurityManager.validatePassword(pass);
            if (passError != null) {
                errorLabel.setText(passError); return;
            }
            if (userRepo.emailExists(email)) {
                errorLabel.setText("Пользователь с таким email уже существует"); return;
            }
            String rawPhone = phoneField.getText().trim();
            String phone    = rawPhone.isEmpty() ? "+70000000000" : rawPhone;
            String hash     = security.hashPassword(pass);
            User admin = new User(username, email, phone, hash, "ADMIN");
            userRepo.create(admin);
            loadUsers();
            dlg.close();
        });

        DialogHelper.applyTransparentScene(card, dlg);
    }

    /** us***@example.com */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        String local  = email.substring(0, at);
        String domain = email.substring(at); // включая @
        int show = Math.min(2, local.length());
        return local.substring(0, show) + "***" + domain;
    }

    /** +7***XX90 */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        int len = phone.length();
        String prefix = phone.substring(0, Math.min(2, len));
        String suffix = phone.substring(Math.max(0, len - 4));
        return prefix + "***" + suffix;
    }

    /** Форматирует строку даты из БД в "dd.MM.yyyy HH:mm" */
    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            String normalized = raw.replace("T", " ").replaceAll("\\.\\d+$", "");
            // Пробуем "yyyy-MM-dd HH:mm:ss"
            LocalDateTime ldt = LocalDateTime.parse(normalized, DB_FMT);
            return ldt.format(FMT);
        } catch (Exception e1) {
            try {
                // Пробуем "yyyy-MM-dd HH:mm"
                LocalDateTime ldt = LocalDateTime.parse(
                    raw.replace("T", " ").substring(0, 16),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return ldt.format(FMT);
            } catch (Exception ignored) {
                return raw;
            }
        }
    }

    private void loadUsers() {
        allUsers = userRepo.findAllIncludingAdmins();
        applyFilter();
        updateBadges();
    }

    private void applyFilter() {
        if (allUsers == null) return;
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String role  = roleFilter  != null ? roleFilter.getValue() : "Все роли";

        List<User> filtered = allUsers.stream()
            .filter(u -> {
                if ("👤 Покупатель (USER)".equals(role))    return "USER".equalsIgnoreCase(u.getRole());
                if ("🛡 Администратор (ADMIN)".equals(role)) return "ADMIN".equalsIgnoreCase(u.getRole());
                return true;
            })
            .filter(u -> {
                if (query.isEmpty()) return true;
                String name  = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
                String email = u.getEmail()    != null ? u.getEmail().toLowerCase()    : "";
                return name.contains(query) || email.contains(query);
            })
            .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filtered));
    }

    /** Устанавливает подсказку (tooltip) на заголовок столбца таблицы */
    private <T> void addTooltip(TableColumn<User, T> col, String text) {
        Label label = new Label(col.getText());
        label.setTooltip(new Tooltip(text));
        label.setStyle("-fx-text-fill: inherit; -fx-font-weight: inherit;");
        col.setGraphic(label);
        col.setText("");
    }
}
