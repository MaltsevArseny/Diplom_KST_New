package com.techhaven.view;

import com.techhaven.MainApp;
import com.techhaven.config.SessionManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminLayout {
    private BorderPane root;
    private StackPane contentArea;
    private Button productsBtn, ordersBtn, usersBtn, reportsBtn;
    private Button themeToggleBtn;

    public Parent getView() {
        root = new BorderPane();
        root.getStyleClass().add("app-root");

        root.setTop(createTopBar());
        root.setLeft(createSidebar());

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        root.setCenter(contentArea);

        showProducts();

        // Горячие клавиши — навесить после появления сцены
        root.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), () -> showProducts());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), () -> showOrders());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), () -> showUsers());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), () -> showReports());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W,     KeyCombination.ALT_DOWN), () -> MainApp.getPrimaryStage().close());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M,     KeyCombination.ALT_DOWN), () -> MainApp.getPrimaryStage().setIconified(true));
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Q,     KeyCombination.ALT_DOWN), () -> { SessionManager.getInstance().logout(); MainApp.showLogin(); });
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1),                             () -> HelpView.show("AdminManual.md", "Справка администратора"));
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T,     KeyCombination.ALT_DOWN), () -> ThemeToggle.toggleAndApply(scene, themeToggleBtn));
            }
        });

        return root;
    }

    // Смещение для перетаскивания окна
    private double dragOffsetX, dragOffsetY;

    private HBox createTopBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("nav-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 12, 10, 24));

        Label logo = new Label("DigitalHub — Панель администратора");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -th-accent-light;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userName = new Label("👤 " + SessionManager.getInstance().getCurrentUser().getUsername());
        userName.setStyle("-fx-text-fill: -th-text-secondary;");

        Label roleBadge = new Label("ADMIN");
        roleBadge.getStyleClass().add("role-badge-admin");

        Button logoutBtn = new Button("🚪  Выйти");
        logoutBtn.getStyleClass().addAll("btn-danger", "btn-small");
        logoutBtn.setTooltip(new Tooltip("Выход из аккаунта  [Alt+Q]"));
        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().logout();
            MainApp.showLogin();
        });

        // ── Кнопки управления окном ──────────────────────────────────────
        Button minBtn = new Button("_");
        minBtn.getStyleClass().add("window-control-button");
        minBtn.setOnAction(e -> MainApp.getPrimaryStage().setIconified(true));
        minBtn.setTooltip(new Tooltip("Свернуть  [Alt+M]"));

        Button maxBtn = new Button("□");
        maxBtn.getStyleClass().add("window-control-button");
        maxBtn.setOnAction(e -> {
            Stage st = MainApp.getPrimaryStage();
            st.setMaximized(!st.isMaximized());
        });
        maxBtn.setTooltip(new Tooltip("Развернуть / восстановить"));

        Button closeBtn = new Button("×");
        closeBtn.getStyleClass().addAll("window-control-button", "window-close-button");
        closeBtn.setOnAction(e -> MainApp.getPrimaryStage().close());
        closeBtn.setTooltip(new Tooltip("Закрыть приложение  [Alt+W]"));

        HBox winControls = new HBox(2, minBtn, maxBtn, closeBtn);
        winControls.setAlignment(Pos.CENTER);

        Button helpBtn = new Button("❓  Справка");
        helpBtn.getStyleClass().add("help-button");
        helpBtn.setTooltip(new Tooltip("Руководство администратора  [F1]"));
        helpBtn.setOnAction(e -> HelpView.show("AdminManual.md", "Справка администратора"));

        // Кнопка переключения темы
        themeToggleBtn = new Button();
        themeToggleBtn.getStyleClass().add("theme-toggle-button");
        themeToggleBtn.setTooltip(new Tooltip("Сменить тему  [Alt+T]"));
        ThemeToggle.refreshIcon(themeToggleBtn);
        themeToggleBtn.setOnAction(e -> ThemeToggle.toggleAndApply(themeToggleBtn.getScene(), themeToggleBtn));

        bar.getChildren().addAll(logo, spacer, userName, roleBadge, themeToggleBtn, helpBtn, logoutBtn, winControls);

        // ── Перетаскивание окна за топбар ────────────────────────────────
        bar.setOnMousePressed(e -> {
            Stage st = MainApp.getPrimaryStage();
            if (!st.isMaximized()) {
                dragOffsetX = e.getScreenX() - st.getX();
                dragOffsetY = e.getScreenY() - st.getY();
            }
        });
        bar.setOnMouseDragged(e -> {
            Stage st = MainApp.getPrimaryStage();
            if (!st.isMaximized()) {
                st.setX(e.getScreenX() - dragOffsetX);
                st.setY(e.getScreenY() - dragOffsetY);
            }
        });
        bar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Stage st = MainApp.getPrimaryStage();
                st.setMaximized(!st.isMaximized());
            }
        });

        return bar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(240);

        Label menuTitle = new Label("Меню");
        menuTitle.getStyleClass().add("sidebar-menu-title");
        menuTitle.setPadding(new Insets(0, 0, 8, 8));

        productsBtn = new Button("🏷  Товары");
        productsBtn.getStyleClass().add("sidebar-item");
        productsBtn.setOnAction(e -> showProducts());
        productsBtn.setTooltip(new Tooltip("Управление товарами  [Alt+1]"));

        ordersBtn = new Button("📋  Заказы");
        ordersBtn.getStyleClass().add("sidebar-item");
        ordersBtn.setOnAction(e -> showOrders());
        ordersBtn.setTooltip(new Tooltip("Управление заказами  [Alt+2]"));

        usersBtn = new Button("👥  Пользователи");
        usersBtn.getStyleClass().add("sidebar-item");
        usersBtn.setOnAction(e -> showUsers());
        usersBtn.setTooltip(new Tooltip("Управление пользователями  [Alt+3]"));

        reportsBtn = new Button("📊  Отчёты и аналитика");
        reportsBtn.getStyleClass().add("sidebar-item");
        reportsBtn.setOnAction(e -> showReports());
        reportsBtn.setTooltip(new Tooltip("Отчёты и аналитика  [Alt+4]"));

        sidebar.getChildren().addAll(menuTitle, productsBtn, ordersBtn, usersBtn, reportsBtn);
        return sidebar;
    }

    private void setActive(Button active) {
        for (Button btn : new Button[]{productsBtn, ordersBtn, usersBtn, reportsBtn}) {
            btn.getStyleClass().remove("sidebar-item-active");
        }
        active.getStyleClass().add("sidebar-item-active");
    }

    public void showProducts() {
        setActive(productsBtn);
        AdminProductsView view = new AdminProductsView(this);
        contentArea.getChildren().setAll(view.getView());
    }

    public void showOrders() {
        setActive(ordersBtn);
        AdminOrdersView view = new AdminOrdersView(this);
        contentArea.getChildren().setAll(view.getView());
    }

    public void showUsers() {
        setActive(usersBtn);
        AdminUsersView view = new AdminUsersView(this);
        contentArea.getChildren().setAll(view.getView());
    }

    public void showReports() {
        setActive(reportsBtn);
        AdminReportsView view = new AdminReportsView(this);
        contentArea.getChildren().setAll(view.getView());
    }
}
