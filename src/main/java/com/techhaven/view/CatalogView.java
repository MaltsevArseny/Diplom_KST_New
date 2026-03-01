package com.techhaven.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.techhaven.model.Product;
import com.techhaven.service.CartService;
import com.techhaven.service.FavoriteService;
import com.techhaven.service.ProductService;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class CatalogView {
    private final MainLayout mainLayout;
    private final ProductService productService = new ProductService();
    private final CartService cartService = new CartService();
    private final FavoriteService favoriteService = new FavoriteService();

    private VBox productsContainer;
    private TextField searchField;
    private ComboBox<String> categoryCombo;
    private TextField minPriceField, maxPriceField;
    private CheckBox inStockOnly;
    private Label resultsLabel;

    public CatalogView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
    }

    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        // Заголовок
        Label heading = new Label("Каталог товаров");
        heading.getStyleClass().add("heading");

        // Панель фильтров
        HBox filters = new HBox(12);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(8, 0, 8, 0));

        searchField = new TextField();
        searchField.setPromptText("🔍 Поиск по названию...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);

        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("Все категории");
        categoryCombo.getItems().addAll(productService.getCategories());
        categoryCombo.setValue("Все категории");
        categoryCombo.setPrefWidth(200);
        categoryCombo.setVisibleRowCount(categoryCombo.getItems().size());

        // Custom ListCell for Category Combo Box
        javafx.util.Callback<javafx.scene.control.ListView<String>, javafx.scene.control.ListCell<String>> cellFactory = lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    String[] colors;
                    if (item.equals("Все категории")) {
                        colors = new String[]{"#a0a0b8", "transparent", "📋"};
                    } else {
                        colors = categoryColors(item);
                    }
                    String accent = colors[0];
                    String bgTint = colors[1];
                    String emoji = colors[2];

                    Label label = new Label(emoji + " " + item);
                    label.setStyle(
                        "-fx-text-fill: " + accent + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 2 6;" +
                        "-fx-background-radius: 4;"
                    );
                    setGraphic(label);
                    setText(null);

                    // Ensure cell takes the tint when hovered or selected, else transparent background
                    if (isSelected()) {
                         setStyle("-fx-background-color: " + bgTint + ";");
                    } else {
                         setStyle("-fx-background-color: transparent;");
                    }
                }
            }
        };

        categoryCombo.setCellFactory(cellFactory);
        categoryCombo.setButtonCell(cellFactory.call(null));

        minPriceField = new TextField();
        minPriceField.setPromptText("Цена от");
        minPriceField.setPrefWidth(100);

        maxPriceField = new TextField();
        maxPriceField.setPromptText("Цена до");
        maxPriceField.setPrefWidth(100);

        Button resetBtn = new Button("Сбросить");
        resetBtn.getStyleClass().add("button");
        resetBtn.setTooltip(new javafx.scene.control.Tooltip("Сбросить все фильтры"));

        inStockOnly = new CheckBox("Только в наличии");
        inStockOnly.setStyle("-fx-text-fill: #a0a0b8; -fx-font-size: 13px;");

        filters.getChildren().addAll(searchField, categoryCombo,
            new Label("Цена:"), minPriceField, new Label("—"), maxPriceField, inStockOnly, resetBtn);
        for (var node : filters.getChildren()) {
            if (node instanceof Label label) label.setStyle("-fx-text-fill: #a0a0b8;");
        }

        // Дебаунс-поиск: 0.5 секунды после последнего символа в полях поиска/цены
        PauseTransition searchDebounce = new PauseTransition(Duration.seconds(0.5));
        searchDebounce.setOnFinished(e -> loadProducts());

        searchField.textProperty().addListener((obs, o, n) -> searchDebounce.playFromStart());
        minPriceField.textProperty().addListener((obs, o, n) -> searchDebounce.playFromStart());
        maxPriceField.textProperty().addListener((obs, o, n) -> searchDebounce.playFromStart());

        // Категория и чекбокс — мгновенно без задержки
        categoryCombo.valueProperty().addListener((obs, o, n) -> {
            searchDebounce.stop();
            loadProducts();
        });
        inStockOnly.selectedProperty().addListener((obs, o, n) -> {
            searchDebounce.stop();
            applyInStockFilter();
        });

        // Сброс — мгновенно
        resetBtn.setOnAction(e -> {
            searchDebounce.stop();
            searchField.clear();
            categoryCombo.setValue("Все категории");
            minPriceField.clear();
            maxPriceField.clear();
            inStockOnly.setSelected(false);
            loadProducts();
        });

        // Enter в поле поиска — немедленно
        searchField.setOnAction(e -> { searchDebounce.stop(); loadProducts(); });


        resultsLabel = new Label();
        resultsLabel.getStyleClass().add("label-secondary");

        // Контейнер для групп
        productsContainer = new VBox(24);
        productsContainer.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(productsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(heading, filters, resultsLabel, scroll);

        loadProducts();
        return root;
    }

    private void loadProducts() {
        String query = searchField.getText();
        String category = categoryCombo.getValue();
        Double minPrice = parsePrice(minPriceField.getText());
        Double maxPrice = parsePrice(maxPriceField.getText());

        List<Product> products = productService.searchProducts(query, category, minPrice, maxPrice);

        productsContainer.getChildren().clear();

        resultsLabel.setText("Найдено товаров: " + products.size());

        if (products.isEmpty()) {
            Label empty = new Label("Товары не найдены");
            empty.getStyleClass().add("empty-state");
            productsContainer.getChildren().add(empty);
            return;
        }

        // Группируем по категории, сохраняя порядок первого появления
        Map<String, List<Product>> grouped = products.stream()
            .collect(Collectors.groupingBy(
                p -> p.getCategory() != null ? p.getCategory() : "Прочее",
                LinkedHashMap::new,
                Collectors.toList()
            ));

        // Сортируем: сначала в наличии (по убыванию цены), потом нет в наличии (по убыванию цены)
        grouped.forEach((cat, list) -> list.sort((a, b) -> {
            boolean aOut = a.getStockQuantity() <= 0;
            boolean bOut = b.getStockQuantity() <= 0;
            if (aOut != bOut) return aOut ? 1 : -1; // нет в наличии — в конец
            return Double.compare(b.getPrice(), a.getPrice());
        }));

        // Определяем, активны ли фильтры (поиск, категория, цена)
        boolean hasFilters = (query != null && !query.trim().isEmpty())
                || (category != null && !"Все категории".equals(category))
                || minPrice != null || maxPrice != null;

        // Строим секции — аккордеон, схлопнут по умолчанию (если нет фильтров)
        for (Map.Entry<String, List<Product>> entry : grouped.entrySet()) {
            String catName = entry.getKey();
            List<Product> catProducts = entry.getValue();
            String[] colors = categoryColors(catName);

            // Контент секции (скрыт по умолчанию)
            FlowPane flow = new FlowPane();
            flow.setHgap(12);
            flow.setVgap(12);
            flow.setVisible(false);
            flow.setManaged(false);

            final int PAGE = 15;
            final boolean[] loaded = {false};
            final int[] offset = {0};  // сколько карточек уже загружено

            // Кнопка «Показать ещё»
            Button moreBtn = new Button();
            moreBtn.setVisible(false);
            moreBtn.setManaged(false);
            moreBtn.setMaxWidth(Double.MAX_VALUE);
            moreBtn.setStyle(
                "-fx-background-color: " + colors[0] + ";" +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-padding: 10 24; -fx-background-radius: 10; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            );
            moreBtn.setOnMouseEntered(e -> moreBtn.setStyle(
                "-fx-background-color: " + colors[0] + ";" +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-padding: 10 24; -fx-background-radius: 10; -fx-cursor: hand;" +
                "-fx-opacity: 0.85;" +
                "-fx-effect: dropshadow(gaussian, " + colors[0] + ", 10, 0.3, 0, 0);"
            ));
            moreBtn.setOnMouseExited(e -> moreBtn.setStyle(
                "-fx-background-color: " + colors[0] + ";" +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-padding: 10 24; -fx-background-radius: 10; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            ));

            VBox content = new VBox(8, flow, moreBtn);

            // Обработчик «Показать ещё» — назначается один раз
            moreBtn.setOnAction(ev -> {
                int next = Math.min(offset[0] + PAGE, catProducts.size());
                for (int i = offset[0]; i < next; i++) {
                    VBox card = createProductCard(catProducts.get(i));
                    card.setUserData(catProducts.get(i));
                    if (inStockOnly.isSelected() && catProducts.get(i).getStockQuantity() <= 0) {
                        card.setVisible(false);
                        card.setManaged(false);
                    }
                    flow.getChildren().add(card);
                }
                offset[0] = next;
                int remaining = catProducts.size() - offset[0];
                if (remaining <= 0) {
                    moreBtn.setVisible(false);
                    moreBtn.setManaged(false);
                } else {
                    moreBtn.setText("Показать ещё " + remaining + " ↓");
                }
            });

            // Заголовок — кликабельный
            final boolean[] expanded = {false};
            Label arrow = new Label("▶");
            arrow.setStyle("-fx-text-fill: " + colors[0] + "; -fx-font-size: 12px;");
            Label title = new Label(colors[2] + "  " + catName + "  (" + catProducts.size() + ")");
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + colors[0] + ";");
            javafx.scene.layout.Region hSpacer = new javafx.scene.layout.Region();
            HBox.setHgrow(hSpacer, Priority.ALWAYS);
            HBox headerRow = new HBox(10, arrow, title, hSpacer);
            headerRow.setAlignment(Pos.CENTER_LEFT);
            headerRow.setCursor(javafx.scene.Cursor.HAND);
            headerRow.setPadding(new Insets(6, 8, 6, 4));
            headerRow.setStyle(
                "-fx-background-color: rgba(" + hexToRgb(colors[0]) + ",0.06);" +
                "-fx-background-radius: 10; -fx-border-radius: 10;"
            );
            headerRow.setOnMouseEntered(e -> headerRow.setStyle(
                "-fx-background-color: rgba(" + hexToRgb(colors[0]) + ",0.14);" +
                "-fx-background-radius: 10; -fx-border-radius: 10;"
            ));
            headerRow.setOnMouseExited(e -> headerRow.setStyle(
                "-fx-background-color: rgba(" + hexToRgb(colors[0]) + ",0.06);" +
                "-fx-background-radius: 10; -fx-border-radius: 10;"
            ));

            headerRow.setOnMouseClicked(e -> {
                expanded[0] = !expanded[0];
                if (expanded[0]) {
                    if (!loaded[0]) {
                        // Первое открытие — загружаем первые PAGE карточек
                        int initial = Math.min(PAGE, catProducts.size());
                        for (int i = 0; i < initial; i++) {
                            VBox card = createProductCard(catProducts.get(i));
                            card.setUserData(catProducts.get(i));
                            if (inStockOnly.isSelected() && catProducts.get(i).getStockQuantity() <= 0) {
                                card.setVisible(false);
                                card.setManaged(false);
                            }
                            flow.getChildren().add(card);
                        }
                        offset[0] = initial;
                        if (catProducts.size() > PAGE) {
                            moreBtn.setText("Показать ещё " + (catProducts.size() - initial) + " ↓");
                            moreBtn.setVisible(true);
                            moreBtn.setManaged(true);
                        }
                        loaded[0] = true;
                    } else {
                        // Повторное открытие — восстанавливаем moreBtn если ещё есть товары
                        int remaining = catProducts.size() - offset[0];
                        if (remaining > 0) {
                            moreBtn.setText("Показать ещё " + remaining + " ↓");
                            moreBtn.setVisible(true);
                            moreBtn.setManaged(true);
                        }
                    }
                    flow.setVisible(true);
                    flow.setManaged(true);
                    arrow.setText("▼");
                } else {
                    flow.setVisible(false);
                    flow.setManaged(false);
                    moreBtn.setVisible(false);
                    moreBtn.setManaged(false);
                    arrow.setText("▶");
                }
            });

            VBox section = new VBox(6, headerRow, content);
            productsContainer.getChildren().add(section);

            // Если активны фильтры — автоматически раскрываем секцию
            if (hasFilters) {
                // Имитируем клик по заголовку для раскрытия
                expanded[0] = true;
                int initial = Math.min(PAGE, catProducts.size());
                for (int i = 0; i < initial; i++) {
                    VBox card = createProductCard(catProducts.get(i));
                    card.setUserData(catProducts.get(i));
                    if (inStockOnly.isSelected() && catProducts.get(i).getStockQuantity() <= 0) {
                        card.setVisible(false);
                        card.setManaged(false);
                    }
                    flow.getChildren().add(card);
                }
                offset[0] = initial;
                if (catProducts.size() > PAGE) {
                    moreBtn.setText("Показать ещё " + (catProducts.size() - initial) + " ↓");
                    moreBtn.setVisible(true);
                    moreBtn.setManaged(true);
                }
                loaded[0] = true;
                flow.setVisible(true);
                flow.setManaged(true);
                arrow.setText("▼");
            }
        }
    }

    /** Переключает видимость карточек в зависимости от чекбокса «Только в наличии», не перестраивая UI */
    private void applyInStockFilter() {
        boolean onlyInStock = inStockOnly.isSelected();
        int visibleCount = 0;
        for (javafx.scene.Node section : productsContainer.getChildren()) {
            if (section instanceof VBox sectionBox) {
                for (javafx.scene.Node child : sectionBox.getChildren()) {
                    if (child instanceof VBox contentBox) {
                        for (javafx.scene.Node innerNode : contentBox.getChildren()) {
                            if (innerNode instanceof FlowPane flow) {
                                for (javafx.scene.Node card : flow.getChildren()) {
                                    if (card.getUserData() instanceof Product p) {
                                        boolean visible = !onlyInStock || p.getStockQuantity() > 0;
                                        card.setVisible(visible);
                                        card.setManaged(visible);
                                        if (visible) visibleCount++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        resultsLabel.setText("Найдено товаров: " + visibleCount);
    }

    /** Конвертирует hex #rrggbb в "r,g,b" для rgba() */
    private String hexToRgb(String hex) {
        try {
            hex = hex.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return r + "," + g + "," + b;
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            return "124,58,237";
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(180);
        card.setMaxWidth(180);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Цветовая схема по категории
        String[] cat = categoryColors(product.getCategory());
        String accentColor  = cat[0];
        String bgTint       = cat[1];
        String emoji        = cat[2];

        card.setStyle(
            "-fx-background-color: #2a2a3e;" +
            "-fx-border-color: " + accentColor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0, 0, 2);"
        );

        // Категория — бейдж
        Label categoryLabel = new Label(emoji + " " + product.getCategory());
        categoryLabel.setStyle(
            "-fx-background-color: " + bgTint + ";" +
            "-fx-text-fill: " + accentColor + ";" +
            "-fx-font-size: 10px; -fx-font-weight: bold;" +
            "-fx-padding: 2 8; -fx-background-radius: 8;"
        );

        // Название
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f0f0f0;");
        nameLabel.setWrapText(true);
        nameLabel.setMinHeight(40);
        nameLabel.setAlignment(Pos.TOP_LEFT);

        // Описание
        Label descLabel = new Label(product.getDescription() != null ? product.getDescription() : "");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0a0b8;");

        // Характеристики
        Label specLabel = new Label(product.getSpecifications() != null ? product.getSpecifications() : "");
        specLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");

        // Цена
        Label priceLabel = new Label(product.getFormattedPrice());
        priceLabel.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";"
        );

        // Наличие
        Label stockLabel = new Label(
            product.getStockQuantity() > 0 ? "✓ В наличии: " + product.getStockQuantity() + " шт." : "Нет в наличии");
        String stockClass = product.getStockQuantity() <= 0 ? "stock-out" :
                           product.getStockQuantity() <= 5 ? "stock-low" : "stock-available";
        stockLabel.getStyleClass().add(stockClass);
        stockLabel.setStyle("-fx-font-size: 11px;");

        // Кнопки
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER);

        boolean alreadyInCart = cartService.isInCart(product.getId());

        Button addToCartBtn = new Button(alreadyInCart ? "✓ В корзине" : "В корзину 🛒");
        addToCartBtn.setStyle(
            (alreadyInCart
                ? "-fx-background-color: #10b981;"
                : "-fx-background-color: " + accentColor + ";") +
            " -fx-text-fill: white;" +
            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        addToCartBtn.setDisable(product.getStockQuantity() <= 0);
        addToCartBtn.setTooltip(new javafx.scene.control.Tooltip(alreadyInCart ? "Перейти в корзину" : "Добавить товар в корзину"));
        HBox.setHgrow(addToCartBtn, Priority.ALWAYS);
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);

        addToCartBtn.setOnAction(e -> {
            e.consume();
            if (cartService.isInCart(product.getId())) {
                // Уже в корзине — переходим в корзину
                mainLayout.showCart();
            } else {
                String error = cartService.addToCart(product.getId(), 1);
                if (error != null) {
                    showAlert("Ошибка", error);
                } else {
                    addToCartBtn.setText("✓ В корзине");
                    addToCartBtn.setStyle(
                        "-fx-background-color: #10b981; -fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 8;"
                    );
                    mainLayout.updateCartBadge();
                }
            }
        });

        boolean isFav = favoriteService.isFavorite(product.getId());
        Button favBtn = new Button(isFav ? "❤" : "♡");
        favBtn.setMinWidth(32); // Запрещаем сжатие до точек
        favBtn.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP); // Убираем многоточие
        favBtn.setTooltip(new javafx.scene.control.Tooltip(isFav ? "Убрать из избранного" : "Добавить в избранное"));
        favBtn.setStyle(
            (isFav ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #6b7280;") +
            "-fx-background-color: transparent; -fx-font-size: 18px; -fx-padding: 0; -fx-cursor: hand;"
        );
        favBtn.setOnAction(e -> {
            e.consume();
            favoriteService.toggleFavorite(product.getId());
            boolean nowFav = favoriteService.isFavorite(product.getId());
            favBtn.setText(nowFav ? "❤" : "♡");
            favBtn.setStyle(
                (nowFav ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #6b7280;") +
                "-fx-background-color: transparent; -fx-font-size: 18px; -fx-padding: 0; -fx-cursor: hand;"
            );
            mainLayout.updateFavoriteBadge();
        });

        // Бейдж категории + сердечко в одной строке
        javafx.scene.layout.Region headerSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox categoryRow = new HBox(4, categoryLabel, headerSpacer, favBtn);
        categoryRow.setAlignment(Pos.CENTER_LEFT);

        buttons.getChildren().add(addToCartBtn);

        card.getChildren().addAll(categoryRow, nameLabel, descLabel, specLabel,
            new Separator(), priceLabel, stockLabel, buttons);


        // Hover-эффект
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #353555;" +
            "-fx-border-color: " + accentColor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 12;" +
            "-fx-effect: dropshadow(gaussian, " + accentColor + ", 12, 0.25, 0, 0);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #2a2a3e;" +
            "-fx-border-color: " + accentColor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0, 0, 2);"
        ));

        // Клик — детали
        card.setOnMouseClicked(e -> mainLayout.showProductDetail(product.getId()));

        return card;
    }

    /**
     * Возвращает [accentColor, bgTint, emoji] для категории товара.
     */
    private String[] categoryColors(String category) {
        if (category == null) return new String[]{"#a78bfa", "rgba(124,58,237,0.15)", "📦"};
        return switch (category.toLowerCase().trim()) {
            case "процессоры", "процессор", "cpu"
                -> new String[]{"#f59e0b", "rgba(245,158,11,0.12)", "⚙"};
            case "видеокарты", "видеокарта", "gpu"
                -> new String[]{"#10b981", "rgba(16,185,129,0.12)", "🎮"};
            case "материнские платы", "материнская плата", "motherboard"
                -> new String[]{"#3b82f6", "rgba(59,130,246,0.12)", "🔌"};
            case "оперативная память", "озу", "ram", "память"
                -> new String[]{"#8b5cf6", "rgba(139,92,246,0.12)", "💾"};
            case "накопители", "ssd", "hdd", "диски"
                -> new String[]{"#06b6d4", "rgba(6,182,212,0.12)", "💿"};
            case "блоки питания", "бп", "psu"
                -> new String[]{"#f97316", "rgba(249,115,22,0.12)", "⚡"};
            case "корпуса", "корпус", "case"
                -> new String[]{"#64748b", "rgba(100,116,139,0.12)", "🖥"};
            case "охлаждение", "кулеры", "cooling"
                -> new String[]{"#2dd4bf", "rgba(45,212,191,0.12)", "❄"};
            case "периферия", "клавиатуры", "мыши", "peripherals"
                -> new String[]{"#ec4899", "rgba(236,72,153,0.12)", "🖱"};
            case "мониторы", "monitor"
                -> new String[]{"#a3e635", "rgba(163,230,53,0.12)", "🖥"};
            case "сетевое оборудование"
                -> new String[]{"#818cf8", "rgba(129,140,248,0.12)", "🌐"};
            default
                -> new String[]{"#a78bfa", "rgba(124,58,237,0.15)", "📦"};
        };
    }


    private Double parsePrice(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showAlert(String title, String message) {
        DialogHelper.showWarning(title, message);
    }
}
