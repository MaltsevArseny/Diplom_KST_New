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

    public Parent getView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e2e;");

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
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #a78bfa;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userName = new Label("👤 " + SessionManager.getInstance().getCurrentUser().getUsername());
        userName.setStyle("-fx-text-fill: #a0a0b8;");

        Label roleBadge = new Label("ADMIN");
        roleBadge.getStyleClass().add("badge-danger");
        roleBadge.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #f87171; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        Button logoutBtn = new Button("🚪  Выйти");
        logoutBtn.getStyleClass().addAll("btn-danger", "btn-small");
        logoutBtn.setTooltip(new Tooltip("Выход из аккаунта  [Alt+Q]"));
        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().logout();
            MainApp.showLogin();
        });

        // ── Кнопки управления окном ──────────────────────────────────────
        String winBtnBase = "-fx-background-color:transparent;-fx-text-fill:#a0a0b8;" +
                            "-fx-font-size:14px;-fx-padding:2 10;-fx-cursor:hand;" +
                            "-fx-background-radius:4;-fx-min-width:32;-fx-pref-width:32;";
        Button minBtn = new Button("—");
        minBtn.setStyle(winBtnBase);
        minBtn.setOnMouseEntered(e -> minBtn.setStyle(winBtnBase + "-fx-background-color:#2d2d48;"));
        minBtn.setOnMouseExited(e  -> minBtn.setStyle(winBtnBase));
        minBtn.setOnAction(e -> MainApp.getPrimaryStage().setIconified(true));
        minBtn.setTooltip(new Tooltip("Свернуть  [Alt+M]"));

        Button maxBtn = new Button("□");
        maxBtn.setStyle(winBtnBase);
        maxBtn.setOnMouseEntered(e -> maxBtn.setStyle(winBtnBase + "-fx-background-color:#2d2d48;"));
        maxBtn.setOnMouseExited(e  -> maxBtn.setStyle(winBtnBase));
        maxBtn.setOnAction(e -> {
            Stage st = MainApp.getPrimaryStage();
            st.setMaximized(!st.isMaximized());
        });
        maxBtn.setTooltip(new Tooltip("Развернуть / восстановить"));

        Button closeBtn = new Button("×");
        closeBtn.setStyle(winBtnBase + "-fx-text-fill:#f87171;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(winBtnBase + "-fx-background-color:#ef4444;-fx-text-fill:white;"));
        closeBtn.setOnMouseExited(e  -> closeBtn.setStyle(winBtnBase + "-fx-text-fill:#f87171;"));
        closeBtn.setOnAction(e -> MainApp.getPrimaryStage().close());
        closeBtn.setTooltip(new Tooltip("Закрыть приложение  [Alt+W]"));

        HBox winControls = new HBox(2, minBtn, maxBtn, closeBtn);
        winControls.setAlignment(Pos.CENTER);

        Button helpBtn = new Button("❓  Справка");
        helpBtn.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: #a0d4ff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        helpBtn.setOnMouseEntered(e -> helpBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"));
        helpBtn.setOnMouseExited(e  -> helpBtn.setStyle("-fx-background-color: #2d2d48; -fx-text-fill: #a0d4ff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"));
        helpBtn.setTooltip(new Tooltip("Руководство администратора  [F1]"));
        helpBtn.setOnAction(e -> HelpView.show("AdminManual.md", "Справка администратора"));

        bar.getChildren().addAll(logo, spacer, userName, roleBadge, helpBtn, logoutBtn, winControls);

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
        menuTitle.setStyle("-fx-text-fill: #6b6b80; -fx-font-size: 11px; -fx-font-weight: bold;");
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
