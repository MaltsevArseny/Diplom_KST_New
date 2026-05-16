package com.techhaven.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.techhaven.model.CartItem;
import com.techhaven.service.CartService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CartView {
    private final MainLayout mainLayout;
    private final CartService cartService = new CartService();

    public CartView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
    }

    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        Label heading = new Label("🛒 Корзина");
        heading.getStyleClass().add("heading");

        List<CartItem> items = cartService.getCartItems();

        if (items.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            Label emptyLabel = new Label("Корзина пуста");
            emptyLabel.getStyleClass().add("empty-state");
            Button goShop = new Button("Перейти в каталог");
            goShop.getStyleClass().add("btn-primary");
            goShop.setTooltip(new Tooltip("Открыть каталог товаров  [Alt+1]"));
            goShop.setOnAction(e -> mainLayout.showCatalog());
            empty.getChildren().addAll(emptyLabel, goShop);
            root.getChildren().addAll(heading, empty);
            return root;
        }

        VBox itemsList = new VBox(16);

        // Итог корзины — объявляем ДО цикла, чтобы lambda могли его обновлять
        double initialTotal = cartService.getTotal();
        Label totalLabel = new Label("Итого: " + String.format("%,.0f ₽", initialTotal));
        totalLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: -th-success;");
        double[] totalRef = { initialTotal }; // изменяемый итог для lambda

        // Группируем по категории
        Map<String, List<CartItem>> grouped = items.stream()
            .collect(Collectors.groupingBy(
                i -> i.getCategory() != null ? i.getCategory() : "Прочее",
                LinkedHashMap::new, Collectors.toList()
            ));

        for (Map.Entry<String, List<CartItem>> entry : grouped.entrySet()) {
            String catName = entry.getKey();
            String[] colors = categoryColors(catName);

            // Заголовок категории
            Label catHeader = new Label(colors[2] + "  " + catName + "  (" + entry.getValue().size() + ")");
            catHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + colors[0] + ";");
            itemsList.getChildren().add(catHeader);

            for (CartItem item : entry.getValue()) {
            HBox row = new HBox(16);
            row.getStyleClass().add("card");
            row.setPadding(new Insets(16));
            row.setAlignment(Pos.CENTER_LEFT);

            // Иконка категории
            Label icon = new Label(colors[2]);
            icon.setStyle("-fx-font-size: 28px;");

            // Информация
            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);

            Hyperlink name = new Hyperlink(item.getProductName());
            name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -th-accent; -fx-border-color: transparent; -fx-padding: 0;");
            name.setOnAction(e -> mainLayout.showProductDetail(item.getProductId()));

            Label unitPrice = new Label("Цена: " + item.getFormattedPrice());
            unitPrice.getStyleClass().add("label-secondary");

            info.getChildren().addAll(name, unitPrice);

            // Количество
            VBox qtyBox = new VBox(4);
            qtyBox.setAlignment(Pos.CENTER);
            Label qtyLabel = new Label("Кол-во");
            qtyLabel.getStyleClass().add("label-muted");

            HBox qtyControls = new HBox(4);
            qtyControls.setAlignment(Pos.CENTER);

            Button minusBtn = new Button("−");
            minusBtn.setStyle(
                "-fx-background-color: -th-border; -fx-text-fill: -th-text-primary;" +
                "-fx-font-size: 16px; -fx-font-weight: bold;" +
                "-fx-min-width: 34; -fx-min-height: 34; -fx-background-radius: 8; -fx-cursor: hand;"
            );
            minusBtn.setTooltip(new Tooltip("Уменьшить количество"));

            Label qtyValue = new Label(String.valueOf(item.getQuantity()));
            qtyValue.setStyle("-fx-text-fill: -th-text-primary; -fx-font-size: 16px; -fx-font-weight: bold;" +
                "-fx-min-width: 36; -fx-alignment: center;");

            Button plusBtn = new Button("+");
            plusBtn.getStyleClass().add("qty-plus-button");
            plusBtn.setTooltip(new Tooltip("Увеличить количество"));

            int stock = item.getStockQuantity();
            if (item.getQuantity() >= stock) {
                plusBtn.setDisable(true);
            }

            // Subtotal для этой позиции — объявляем до обработчиков
            VBox subtotalBox = new VBox(4);
            subtotalBox.setAlignment(Pos.CENTER_RIGHT);
            subtotalBox.setPrefWidth(120);
            Label subtotalLabel = new Label("Итого");
            subtotalLabel.getStyleClass().add("label-muted");
            Label subtotalValue = new Label(item.getFormattedSubtotal());
            subtotalValue.getStyleClass().add("price-label-small");
            subtotalBox.getChildren().addAll(subtotalLabel, subtotalValue);

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
            pause.setOnFinished(e -> { cartService.removeFromCart(item.getId()); mainLayout.showCart(); });

            Runnable startUndoRemoval = () -> {
                row.getChildren().clear();
                Label undoMsg = new Label("Товар удален из корзины");
                undoMsg.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-style: italic;");
                Button undoBtn = new Button("Отмена (5с) ↩");
                undoBtn.getStyleClass().addAll("button", "btn-small", "undo-button");
                undoBtn.setTooltip(new Tooltip("Отменить удаление"));
                undoBtn.setOnAction(ue -> { pause.stop(); mainLayout.showCart(); });
                Region undoSpacer = new Region();
                HBox.setHgrow(undoSpacer, Priority.ALWAYS);
                row.getChildren().addAll(undoMsg, undoSpacer, undoBtn);
                pause.play();
            };

            minusBtn.setOnAction(e -> {
                int newQty = item.getQuantity() - 1;
                if (newQty < 1) { startUndoRemoval.run(); }
                else {
                    cartService.updateQuantity(item.getId(), newQty);
                    item.setQuantity(newQty);
                    qtyValue.setText(String.valueOf(newQty));
                    plusBtn.setDisable(newQty >= stock);
                    // Обновляем стоимость позиции
                    subtotalValue.setText(String.format("%,.0f ₽", item.getProductPrice() * newQty));
                    // Обновляем общий итог
                    totalRef[0] -= item.getProductPrice();
                    totalLabel.setText("Итого: " + String.format("%,.0f ₽", totalRef[0]));
                }
            });
            plusBtn.setOnAction(e -> {
                int newQty = item.getQuantity() + 1;
                if (newQty <= stock) {
                    cartService.updateQuantity(item.getId(), newQty);
                    item.setQuantity(newQty);
                    qtyValue.setText(String.valueOf(newQty));
                    plusBtn.setDisable(newQty >= stock);
                    // Обновляем стоимость позиции
                    subtotalValue.setText(String.format("%,.0f ₽", item.getProductPrice() * newQty));
                    // Обновляем общий итог
                    totalRef[0] += item.getProductPrice();
                    totalLabel.setText("Итого: " + String.format("%,.0f ₽", totalRef[0]));
                }
            });

            qtyControls.getChildren().addAll(minusBtn, qtyValue, plusBtn);
            qtyBox.getChildren().addAll(qtyLabel, qtyControls);

            Button removeBtn = new Button("×");
            removeBtn.getStyleClass().addAll("btn-danger", "btn-small");
            removeBtn.setTooltip(new Tooltip("Удалить из корзины"));
            removeBtn.setOnAction(e -> startUndoRemoval.run());

            row.getChildren().addAll(icon, info, qtyBox, subtotalBox, removeBtn);
            itemsList.getChildren().add(row);
            } // end item loop
        } // end category loop

        ScrollPane scroll = new ScrollPane(itemsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Итого + Оформить
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16));
        footer.setStyle("-fx-background-color: -th-bg-secondary; -fx-background-radius: 12; -fx-border-color: -th-border; -fx-border-radius: 12;");

        Button checkoutBtn = new Button("Оформить заказ →");
        checkoutBtn.getStyleClass().add("btn-primary");
        checkoutBtn.setPrefHeight(44);
        checkoutBtn.setStyle("-fx-font-size: 16px; -fx-background-color: -th-accent; -fx-text-fill: -th-cream; -fx-background-radius: 8; -fx-font-weight: bold;");
        checkoutBtn.setTooltip(new Tooltip("Перейти к оформлению заказа"));
        checkoutBtn.setOnAction(e -> mainLayout.showCheckout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(totalLabel, spacer, checkoutBtn);

        root.getChildren().addAll(heading, scroll, footer);
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
