package com.techhaven.view;

import com.techhaven.model.Product;
import com.techhaven.service.FavoriteService;
import com.techhaven.service.ProductService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ProductDetailView {
    private final MainLayout mainLayout;
    private final int productId;
    private final ProductService productService = new ProductService();
    private final FavoriteService favoriteService = new FavoriteService();

    public ProductDetailView(MainLayout mainLayout, int productId) {
        this.mainLayout = mainLayout;
        this.productId = productId;
    }

    public Parent getView() {
        Product product = productService.getProductById(productId);
        if (product == null) {
            Label error = new Label("Товар не найден");
            error.getStyleClass().add("empty-state");
            return new StackPane(error);
        }

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // Назад
        Button backBtn = new Button("← Назад в каталог");
        backBtn.getStyleClass().add("nav-button");
        backBtn.setTooltip(new javafx.scene.control.Tooltip("Вернуться в каталог"));
        backBtn.setOnAction(e -> mainLayout.showCatalog());

        // Карточка
        HBox card = new HBox(30);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(32));

        // Левая часть — иконка/плейсхолдер
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPrefWidth(200);
        imageBox.setPrefHeight(200);
        imageBox.setStyle("-fx-background-color: -th-bg-hover; -fx-background-radius: 12;");
        Label iconLabel = new Label("🖥");
        iconLabel.setStyle("-fx-font-size: 64px;");
        imageBox.getChildren().add(iconLabel);

        // Правая часть — информация
        VBox info = new VBox(12);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label categoryBadge = new Label(product.getCategory());
        categoryBadge.getStyleClass().add("badge");

        Label name = new Label(product.getName());
        name.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: -th-text-primary;");
        name.setWrapText(true);

        Label description = new Label(product.getDescription() != null ? product.getDescription() : "Описание отсутствует");
        description.getStyleClass().add("label-secondary");
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 15px; -fx-text-fill: -th-text-secondary;");

        // Спецификации
        VBox specsBox = new VBox(6);
        Label specsTitle = new Label("Характеристики");
        specsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -th-text-primary;");
        Label specsValue = new Label(product.getSpecifications() != null ?
            product.getSpecifications() : "—");
        specsValue.setStyle("-fx-text-fill: -th-text-secondary;");
        specsValue.setWrapText(true);
        specsBox.getChildren().addAll(specsTitle, specsValue);

        // Цена
        Label price = new Label(product.getFormattedPrice());
        price.getStyleClass().add("price-label");
        price.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: -th-success;");

        // Наличие
        Label stock = new Label(product.getStockStatus() +
            (product.getStockQuantity() > 0 ? " — " + product.getStockQuantity() + " шт." : ""));
        String stockClass = product.getStockQuantity() <= 0 ? "stock-out" :
                           product.getStockQuantity() <= 5 ? "stock-low" : "stock-available";
        stock.getStyleClass().add(stockClass);
        stock.setStyle("-fx-font-size: 14px;");

        // Кнопка избранного (без корзины — карточка только для просмотра)
        boolean isFav = favoriteService.isFavorite(product.getId());
        Button favBtn = new Button(isFav ? "❤ В избранном" : "♡ В избранное");
        favBtn.getStyleClass().add(isFav ? "btn-danger" : "button");
        favBtn.setTooltip(new javafx.scene.control.Tooltip(isFav ? "Убрать из избранного" : "Добавить в избранное"));
        favBtn.setPrefHeight(44);
        favBtn.setOnAction(e -> {
            favoriteService.toggleFavorite(product.getId());
            mainLayout.updateFavoriteBadge();
            mainLayout.showProductDetail(productId);
        });

        info.getChildren().addAll(categoryBadge, name, description,
            new Separator(), specsBox, new Separator(), price, stock, favBtn);

        card.getChildren().addAll(imageBox, info);

        root.getChildren().addAll(backBtn, card);
        return root;
    }
}

