package com.techhaven.view;

import com.techhaven.MainApp;
import com.techhaven.config.SessionManager;
import com.techhaven.service.CartService;

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
public class MainLayout {
    private BorderPane root;
    private StackPane contentArea;
    private Label cartBadge, favBadge, ordersBadge;
    private Button catalogBtn, cartBtn, favBtn, ordersBtn, profileBtn;
    private Button themeToggleBtn;
    private final CartService cartService = new CartService();
    private final com.techhaven.service.FavoriteService favoriteService = new com.techhaven.service.FavoriteService();
    private final com.techhaven.service.OrderService orderService = new com.techhaven.service.OrderService();

    public Parent getView() {
        root = new BorderPane();
        root.getStyleClass().add("app-root");

        // Верхняя навигация
        root.setTop(createNavBar());

        // Контент
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        root.setCenter(contentArea);

        // По умолчанию — каталог
        showCatalog();

        // Горячие клавиши — навешаем после появления сцены
        root.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), () -> showCatalog());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), () -> showCart());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), () -> showFavorites());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), () -> showOrders());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN), () -> showProfile());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W,     KeyCombination.ALT_DOWN), () -> MainApp.getPrimaryStage().close());
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M,     KeyCombination.ALT_DOWN), () -> MainApp.getPrimaryStage().setIconified(true));
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Q,     KeyCombination.ALT_DOWN), () -> { SessionManager.getInstance().logout(); MainApp.showLogin(); });
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1),                             () -> HelpView.show("UserManual.md", "Справка пользователя"));
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T,     KeyCombination.ALT_DOWN), () -> ThemeToggle.toggleAndApply(scene, themeToggleBtn));
            }
        });

        return root;
    }

    // Смещение для перетаскивания окна
    private double dragOffsetX, dragOffsetY;

    private HBox createNavBar() {
        HBox navBar = new HBox(12);
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(8, 12, 8, 24));

        Label logo = new Label("DigitalHub");
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -th-accent-light;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        catalogBtn = createNavButton("🏷  Каталог");
        catalogBtn.setOnAction(e -> showCatalog());
        catalogBtn.setTooltip(new Tooltip("Каталог товаров  [Alt+1]"));

        cartBtn = createNavButton("🛒  Корзина");
        cartBtn.setOnAction(e -> showCart());
        cartBtn.setTooltip(new Tooltip("Корзина  [Alt+2]"));

        // Бейдж корзины
        cartBadge = new Label();
        cartBadge.getStyleClass().add("nav-badge");
        cartBadge.setVisible(false);

        StackPane cartPane = new StackPane(cartBtn, cartBadge);
        StackPane.setAlignment(cartBadge, Pos.TOP_RIGHT);

        favBtn = createNavButton("💜  Избранное");
        favBtn.setOnAction(e -> showFavorites());
        favBtn.setTooltip(new Tooltip("Избранное  [Alt+3]"));

        // Бейдж избранного
        favBadge = new Label();
        favBadge.getStyleClass().add("nav-badge");
        favBadge.setVisible(false);

        StackPane favPane = new StackPane(favBtn, favBadge);
        StackPane.setAlignment(favBadge, Pos.TOP_RIGHT);

        ordersBtn = createNavButton("📋  Заказы");
        ordersBtn.setOnAction(e -> showOrders());
        ordersBtn.setTooltip(new Tooltip("Мои заказы  [Alt+4]"));

        // Бейдж заказов
        ordersBadge = new Label();
        ordersBadge.getStyleClass().add("nav-badge");
        ordersBadge.setVisible(false);

        StackPane ordersPane = new StackPane(ordersBtn, ordersBadge);
        StackPane.setAlignment(ordersBadge, Pos.TOP_RIGHT);

        profileBtn = createNavButton("👤  Профиль");
        profileBtn.setOnAction(e -> showProfile());
        profileBtn.setTooltip(new Tooltip("Профиль  [Alt+5]"));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label userName = new Label(SessionManager.getInstance().getCurrentUser().getUsername());
        userName.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 13px;");

        Button logoutBtn = new Button("🚪  Выйти");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.getStyleClass().add("btn-small");
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
        helpBtn.getStyleClass().addAll("btn-small", "help-button");
        helpBtn.setTooltip(new Tooltip("Руководство пользователя  [F1]"));
        helpBtn.setOnAction(e -> HelpView.show("UserManual.md", "Справка пользователя"));

        // Кнопка переключения темы (☀/🌙)
        themeToggleBtn = new Button();
        themeToggleBtn.getStyleClass().add("theme-toggle-button");
        themeToggleBtn.setTooltip(new Tooltip("Сменить тему  [Alt+T]"));
        ThemeToggle.refreshIcon(themeToggleBtn);
        themeToggleBtn.setOnAction(e -> ThemeToggle.toggleAndApply(themeToggleBtn.getScene(), themeToggleBtn));

        navBar.getChildren().addAll(logo, spacer1,
            catalogBtn, cartPane, favPane, ordersPane, profileBtn,
            spacer2, userName, themeToggleBtn, helpBtn, logoutBtn, winControls);

        // ── Перетаскивание окна за навбар ────────────────────────────────
        navBar.setOnMousePressed(e -> {
            Stage st = MainApp.getPrimaryStage();
            if (!st.isMaximized()) {
                dragOffsetX = e.getScreenX() - st.getX();
                dragOffsetY = e.getScreenY() - st.getY();
            }
        });
        navBar.setOnMouseDragged(e -> {
            Stage st = MainApp.getPrimaryStage();
            if (!st.isMaximized()) {
                st.setX(e.getScreenX() - dragOffsetX);
                st.setY(e.getScreenY() - dragOffsetY);
            }
        });
        navBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Stage st = MainApp.getPrimaryStage();
                st.setMaximized(!st.isMaximized());
            }
        });

        return navBar;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        return btn;
    }

    private void setActiveButton(Button active) {
        for (Button btn : new Button[]{catalogBtn, cartBtn, favBtn, ordersBtn, profileBtn}) {
            btn.getStyleClass().remove("nav-button-active");
        }
        active.getStyleClass().add("nav-button-active");
    }

    private void updateBadges() {
        // Обновление корзины
        int cartCount = cartService.getCartCount();
        if (cartCount > 0) {
            cartBadge.setText(String.valueOf(cartCount));
            cartBadge.setVisible(true);
        } else {
            cartBadge.setVisible(false);
        }

        // Обновление избранного
        int favCount = favoriteService.getFavoriteCount();
        if (favCount > 0) {
            favBadge.setText(String.valueOf(favCount));
            favBadge.setVisible(true);
        } else {
            favBadge.setVisible(false);
        }

        // Обновление заказов
        int ordersCount = orderService.getIncompleteOrdersCount();
        if (ordersCount > 0) {
            ordersBadge.setText(String.valueOf(ordersCount));
            ordersBadge.setVisible(true);
        } else {
            ordersBadge.setVisible(false);
        }
    }

    public void updateCartBadge() {
        updateBadges();
    }

    public void updateFavoriteBadge() {
        updateBadges();
    }

    public void showCatalog() {
        setActiveButton(catalogBtn);
        updateBadges();
        CatalogView catalog = new CatalogView(this);
        contentArea.getChildren().setAll(catalog.getView());
    }

    public void showCart() {
        setActiveButton(cartBtn);
        updateBadges();
        CartView cart = new CartView(this);
        contentArea.getChildren().setAll(cart.getView());
    }

    public void showFavorites() {
        setActiveButton(favBtn);
        updateBadges();
        FavoritesView favorites = new FavoritesView(this);
        contentArea.getChildren().setAll(favorites.getView());
    }

    public void showOrders() {
        setActiveButton(ordersBtn);
        updateBadges();
        OrdersView orders = new OrdersView(this);
        contentArea.getChildren().setAll(orders.getView());
    }

    public void showProfile() {
        setActiveButton(profileBtn);
        updateBadges();
        ProfileView profile = new ProfileView(this);
        contentArea.getChildren().setAll(profile.getView());
    }

    public void showCheckout() {
        updateBadges();
        CheckoutView checkout = new CheckoutView(this);
        contentArea.getChildren().setAll(checkout.getView());
    }

    public void showProductDetail(int productId) {
        com.techhaven.service.ProductService productService = new com.techhaven.service.ProductService();
        com.techhaven.model.Product product = productService.getProductById(productId);
        if (product == null) return;

        Stage detailStage = DialogHelper.createStage(MainApp.getPrimaryStage(), true);

        VBox detailRoot = new VBox();
        detailRoot.setStyle(DialogHelper.cardStyle());
        detailRoot.setPadding(new Insets(24));
        detailRoot.setPrefWidth(700);

        HBox content = new HBox(24);
        content.setPadding(new Insets(8, 0, 0, 0));

        // Левая часть (Изображение/Иконка)
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setMinWidth(220);
        imageBox.setMinHeight(220);
        imageBox.setStyle("-fx-background-color: -th-bg-card; -fx-background-radius: 12;");
        Label icon = new Label("🖥");
        icon.setStyle("-fx-font-size: 80px;");
        imageBox.getChildren().add(icon);

        // Правая часть (Инфо)
        VBox info = new VBox(12);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label cat = new Label(product.getCategory());
        cat.getStyleClass().add("badge");

        Label name = new Label(product.getName());
        name.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: -th-cream;");
        name.setWrapText(true);

        Label price = new Label(product.getFormattedPrice());
        price.getStyleClass().add("price-label");

        Label desc = new Label(product.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 14px; -fx-line-spacing: 5;");

        VBox specsBox = new VBox(4);
        Label specsTitle = new Label("ХАРАКТЕРИСТИКИ");
        specsTitle.setStyle("-fx-text-fill: -th-text-muted; -fx-font-weight: bold; -fx-font-size: 11px; -fx-letter-spacing: 1px;");
        Label specsValue = new Label(product.getSpecifications() != null ? product.getSpecifications() : "—");
        specsValue.setStyle("-fx-text-fill: -th-text-muted; -fx-font-size: 13px;");
        specsValue.setWrapText(true);
        specsBox.getChildren().addAll(specsTitle, specsValue);

        info.getChildren().addAll(cat, name, price, new javafx.scene.control.Separator(), desc, specsBox);
        content.getChildren().addAll(imageBox, info);
        detailRoot.getChildren().add(content);

        Button closeBtn = DialogHelper.createCloseButton(detailStage);
        detailStage.centerOnScreen();
        DialogHelper.applyTransparentSceneAndWait(detailRoot, detailStage, closeBtn);
    }
}
