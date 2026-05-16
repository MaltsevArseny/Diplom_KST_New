package com.techhaven.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.techhaven.model.Favorite;
import com.techhaven.service.CartService;
import com.techhaven.service.FavoriteService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FavoritesView {
    private final MainLayout mainLayout;
    private final FavoriteService favoriteService = new FavoriteService();
    private final CartService cartService = new CartService();

    public FavoritesView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
    }

    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        Label heading = new Label("❤ Избранное");
        heading.getStyleClass().add("heading");

        List<Favorite> favorites = favoriteService.getFavorites();

        if (favorites.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            Label emptyLabel = new Label("Список избранного пуст");
            emptyLabel.getStyleClass().add("empty-state");
            Button goShop = new Button("Перейти в каталог");
            goShop.getStyleClass().add("btn-primary");
            goShop.setTooltip(new javafx.scene.control.Tooltip("Открыть каталог товаров"));
            goShop.setOnAction(e -> mainLayout.showCatalog());
            empty.getChildren().addAll(emptyLabel, goShop);
            root.getChildren().addAll(heading, empty);
            return root;
        }

        // Группируем по категории, сортируем по цене (по убыванию) внутри группы
        Map<String, List<Favorite>> grouped = favorites.stream()
            .collect(Collectors.groupingBy(
                f -> f.getCategory() != null ? f.getCategory() : "Прочее",
                LinkedHashMap::new,
                Collectors.toList()
            ));
        grouped.forEach((cat, list) ->
            list.sort((a, b) -> Double.compare(b.getProductPrice(), a.getProductPrice())));

        Label count = new Label("Товаров в избранном: " + favorites.size());
        count.getStyleClass().add("label-secondary");

        VBox allSections = new VBox(20);

        for (Map.Entry<String, List<Favorite>> entry : grouped.entrySet()) {
            String catName = entry.getKey();
            List<Favorite> catFavs = entry.getValue();
            String[] colors = categoryColors(catName);

            // Заголовок категории
            Label catLabel = new Label(colors[2] + "  " + catName + "  (" + catFavs.size() + ")");
            catLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + colors[0] + ";"
            );
            Separator catSep = new Separator();

            // Карточки
            FlowPane grid = new FlowPane();
            grid.setHgap(16);
            grid.setVgap(16);

            for (Favorite fav : catFavs) {
                VBox card = new VBox(10);
                card.getStyleClass().add("product-card");
                card.setPrefWidth(260);

                Label catBadge = new Label(colors[2] + " " + catName);
                catBadge.setStyle(
                    "-fx-background-color: " + colors[1] + "; -fx-text-fill: " + colors[0] + ";" +
                    "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 6;"
                );

                Label name = new Label(fav.getProductName());
                name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -th-text-primary;");
                name.setWrapText(true);

                Label desc = new Label(fav.getDescription() != null ? fav.getDescription() : "");
                desc.getStyleClass().add("label-secondary");
                desc.setWrapText(true);
                desc.setMaxHeight(36);

                Label price = new Label(fav.getFormattedPrice());
                price.getStyleClass().add("price-label");

                String stockText = fav.getStockQuantity() <= 0 ? "Нет в наличии"
                    : "В наличии (" + fav.getStockQuantity() + " шт.)";
                Label stock = new Label(stockText);
                String stockClass = fav.getStockQuantity() <= 0 ? "stock-out" :
                                   fav.getStockQuantity() <= 5 ? "stock-low" : "stock-available";
                stock.getStyleClass().add(stockClass);

                HBox buttons = new HBox(8);
                buttons.setAlignment(Pos.CENTER);

                Button addToCartBtn = new Button("🛒 В корзину");
                addToCartBtn.getStyleClass().addAll("btn-primary", "btn-small");
                addToCartBtn.setTooltip(new javafx.scene.control.Tooltip("Добавить товар в корзину"));
                addToCartBtn.setDisable(fav.getStockQuantity() <= 0);
                addToCartBtn.setOnAction(e -> {
                    String error = cartService.addToCart(fav.getProductId(), 1);
                    if (error == null) {
                        addToCartBtn.setText("✓ Добавлено");
                        addToCartBtn.getStyleClass().remove("btn-primary");
                        addToCartBtn.getStyleClass().add("btn-success");
                    }
                });

                Button removeBtn = new Button("× Удалить");
                removeBtn.getStyleClass().addAll("btn-danger", "btn-small");
                removeBtn.setTooltip(new javafx.scene.control.Tooltip("Удалить из избранного"));

                javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
                pause.setOnFinished(e -> {
                    favoriteService.removeFavorite(fav.getProductId());
                    mainLayout.updateFavoriteBadge();
                    mainLayout.showFavorites();
                });
                removeBtn.setOnAction(e -> {
                    e.consume();
                    card.getChildren().clear();
                    card.setAlignment(Pos.CENTER);
                    Label undoMsg = new Label("Удалено");
                    undoMsg.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-style: italic;");
                    Button undoBtn = new Button("Отмена (5с) ↩");
                    undoBtn.getStyleClass().addAll("button", "btn-small");
                    undoBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
                    undoBtn.setOnAction(ue -> { pause.stop(); mainLayout.showFavorites(); });
                    card.getChildren().addAll(undoMsg, undoBtn);
                    pause.play();
                });

                buttons.getChildren().addAll(addToCartBtn, removeBtn);
                card.getChildren().addAll(catBadge, name, desc, new Separator(), price, stock, buttons);
                card.setCursor(javafx.scene.Cursor.HAND);
                card.setOnMouseClicked(e -> mainLayout.showProductDetail(fav.getProductId()));
                grid.getChildren().add(card);
            }

            VBox section = new VBox(8, catLabel, catSep, grid);
            allSections.getChildren().add(section);
        }

        ScrollPane scroll = new ScrollPane(allSections);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(heading, count, scroll);
        return root;
    }

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
}
