package com.techhaven.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.techhaven.MainApp;
import com.techhaven.config.SessionManager;
import com.techhaven.model.CartItem;
import com.techhaven.model.Order;
import com.techhaven.model.User;
import com.techhaven.service.CartService;
import com.techhaven.service.OrderService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CheckoutView {
    private final MainLayout mainLayout;
    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    private static final String FIELD_NORMAL = "-fx-border-color: -th-border; -fx-border-radius: 8; -fx-background-radius: 8;";
    private static final String FIELD_ERROR  = "-fx-border-color: -th-danger; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2;";

    public CheckoutView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
    }

    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        Button backBtn = new Button("← Назад в корзину");
        backBtn.getStyleClass().add("nav-button");
        backBtn.setTooltip(new javafx.scene.control.Tooltip("Вернуться в корзину"));
        backBtn.setOnAction(e -> mainLayout.showCart());

        Label heading = new Label("📦 Оформление заказа");
        heading.getStyleClass().add("heading");

        List<CartItem> items = cartService.getCartItems();
        double total = cartService.getTotal();

        // ====== Сводка заказа ======
        VBox summary = new VBox(12);
        summary.getStyleClass().add("card");
        summary.setPadding(new Insets(20));

        Label summaryTitle = new Label("Сводка заказа");
        summaryTitle.getStyleClass().add("subheading");

        VBox itemList = new VBox(12);

        // Группируем по категории, сохраняя порядок
        Map<String, List<CartItem>> grouped = items.stream()
            .collect(Collectors.groupingBy(
                i -> i.getCategory() != null ? i.getCategory() : "Прочее",
                LinkedHashMap::new,
                Collectors.toList()
            ));
        // Сортируем товары внутри каждой группы по названию
        grouped.forEach((cat, list) -> list.sort((a, b) ->
            a.getProductName().compareToIgnoreCase(b.getProductName())));

        for (Map.Entry<String, List<CartItem>> entry : grouped.entrySet()) {
            String catName = entry.getKey();
            String[] catColors = categoryColors(catName);

            // Заголовок категории
            Label catHeader = new Label(catColors[2] + "  " + catName);
            catHeader.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                "-fx-text-fill: " + catColors[0] + ";" +
                "-fx-padding: 4 0 2 0;"
            );
            itemList.getChildren().add(catHeader);

            // Товары группы
            for (CartItem item : entry.getValue()) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(0, 0, 0, 12));

                Button itemName = new Button(item.getProductName());
                itemName.getStyleClass().add("hyperlink");
                itemName.setStyle("-fx-padding: 0; -fx-cursor: hand; -fx-text-fill: -th-accent-light; -fx-underline: true;");
                itemName.setTooltip(new javafx.scene.control.Tooltip("Открыть карточку товара"));
                itemName.setOnAction(e -> mainLayout.showProductDetail(item.getProductId()));

                Label qty = new Label(" × " + item.getQuantity());
                qty.setStyle("-fx-text-fill: -th-text-muted;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label itemTotal = new Label(item.getFormattedSubtotal());
                itemTotal.setStyle("-fx-text-fill: -th-text-primary;");

                row.getChildren().addAll(itemName, qty, spacer, itemTotal);
                itemList.getChildren().add(row);
            }

            itemList.getChildren().add(new Separator());
        }

        Separator sep = new Separator();
        HBox totalRow = new HBox();
        Label totalTitle = new Label("Итого к оплате (постоплата):");
        totalTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -th-text-primary; -fx-font-size: 16px;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Label totalValue = new Label(String.format("%,.0f ₽", total));
        totalValue.getStyleClass().add("price-label");
        totalRow.getChildren().addAll(totalTitle, spacer2, totalValue);

        summary.getChildren().addAll(summaryTitle, itemList, sep, totalRow);

        // ====== Загрузка данных для автозаполнения ======
        Order lastOrder = orderService.getLastOrder();
        List<String> usedPhones = orderService.getUsedPhones();
        User currentUser = SessionManager.getInstance().getCurrentUser();

        // ====== Карточка информации о доставке (Компактная) ======
        VBox deliveryCard = new VBox(16);
        deliveryCard.getStyleClass().add("card");
        deliveryCard.setPadding(new Insets(20));

        Label deliveryTitle = new Label("🚚 Информация о доставке");
        deliveryTitle.getStyleClass().add("subheading");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        // --- Ряд 1: Все поля в одну строку ---
        Label addressLabel = new Label("Адрес доставки:");
        addressLabel.getStyleClass().add("label-secondary");
        
        TextField addressField = new TextField();
        addressField.setPromptText("Город, улица, дом, квартира...");
        addressField.setStyle(FIELD_NORMAL);
        addressField.setPrefHeight(40);
        if (lastOrder != null && lastOrder.getDeliveryAddress() != null) {
            addressField.setText(lastOrder.getDeliveryAddress());
        }
        Label addressError = createErrorHint();
        addressField.textProperty().addListener((obs, o, n) -> {
            addressField.setStyle(FIELD_NORMAL);
            addressError.setVisible(false);
            addressError.setManaged(false);
        });

        VBox addressContainer = new VBox(4, addressLabel, addressField, addressError);
        grid.add(addressContainer, 0, 0);
        GridPane.setHgrow(addressContainer, Priority.ALWAYS);

        Label phoneLabel = new Label("Телефон:");
        phoneLabel.getStyleClass().add("label-secondary");
        
        Set<String> phoneSet = new LinkedHashSet<>();
        if (lastOrder != null && lastOrder.getContactPhone() != null && !lastOrder.getContactPhone().isEmpty()) {
            phoneSet.add(lastOrder.getContactPhone());
        }
        phoneSet.addAll(usedPhones);
        if (currentUser != null && currentUser.getPhone() != null && !currentUser.getPhone().isEmpty()) {
            phoneSet.add(currentUser.getPhone());
        }
        ComboBox<String> phoneCombo = new ComboBox<>();
        phoneCombo.setEditable(true);
        phoneCombo.setPromptText("+7XXXXXXXXXX");
        phoneCombo.setPrefHeight(40);
        phoneCombo.setMinWidth(160);
        phoneCombo.setPrefWidth(160);
        phoneCombo.setMaxWidth(160);
        phoneCombo.setStyle(FIELD_NORMAL);
        if (!phoneSet.isEmpty()) {
            phoneCombo.setItems(FXCollections.observableArrayList(phoneSet));
            phoneCombo.getSelectionModel().selectFirst();
        }
        Label phoneError = createErrorHint();
        phoneCombo.getEditor().textProperty().addListener((obs, o, n) -> {
            phoneCombo.setStyle(FIELD_NORMAL);
            phoneError.setVisible(false);
            phoneError.setManaged(false);
        });

        VBox phoneContainer = new VBox(4, phoneLabel, phoneCombo, phoneError);
        grid.add(phoneContainer, 1, 0);

        Label dateLabel = new Label("Дата:");
        dateLabel.getStyleClass().add("label-secondary");
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPrefHeight(40);
        datePicker.setMinWidth(140);
        datePicker.setPrefWidth(140);
        datePicker.setMaxWidth(140);
        datePicker.setStyle(FIELD_NORMAL);
        LocalDate minDate = LocalDate.now().plusDays(1);
        datePicker.setValue(minDate);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(minDate));
                if (date.isBefore(minDate)) setStyle("-fx-background-color: #35354a;");
            }
        });

        VBox dateContainer = new VBox(4, dateLabel, datePicker);
        grid.add(dateContainer, 2, 0);

        Label intervalLabel = new Label("Интервал:");
        intervalLabel.getStyleClass().add("label-secondary");
        
        ComboBox<String> intervalCombo = new ComboBox<>();
        intervalCombo.setPrefHeight(40);
        intervalCombo.setMinWidth(180);
        intervalCombo.setPrefWidth(180);
        intervalCombo.setMaxWidth(180);
        intervalCombo.setStyle(FIELD_NORMAL);
        List<String> intervals = new ArrayList<>();
        for (int h = 8; h < 22; h += 2) intervals.add(String.format("%02d:00 — %02d:00", h, h + 2));
        intervalCombo.setItems(FXCollections.observableArrayList(intervals));
        if (lastOrder != null) {
            // The original addressField.setText and phoneField.setText were already handled above.
            // This block is specifically for intervalCombo.
            if (lastOrder.getDeliveryTimeInterval() != null) {
                // Извлекаем только время из строки типа "2026-03-01 10:00 - 12:00"
                // Удаляем все даты в формате YYYY-MM-DD
                String timeOnly = lastOrder.getDeliveryTimeInterval()
                    .replaceAll("\\d{4}-\\d{2}-\\d{2}\\s*", "")
                    .trim();
                intervalCombo.setValue(timeOnly);
            } else {
                intervalCombo.setValue("10:00 — 12:00");
            }
        } else {
            intervalCombo.setValue("10:00 — 12:00");
        }
        Label intervalError = createErrorHint();
        intervalCombo.valueProperty().addListener((obs, o, n) -> {
            intervalCombo.setStyle(FIELD_NORMAL);
            intervalError.setVisible(false);
            intervalError.setManaged(false);
        });

        VBox intervalContainer = new VBox(4, intervalLabel, intervalCombo, intervalError);
        grid.add(intervalContainer, 3, 0);

        Label commentLabel = new Label("Комментарий к заказу:");
        commentLabel.getStyleClass().add("label-secondary");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Например: код домофона, этаж, пожелания...");
        commentArea.setPrefHeight(60);
        commentArea.setPrefRowCount(2);
        commentArea.setWrapText(true);
        commentArea.getStyleClass().add("text-area");
        commentArea.setStyle("-fx-control-inner-background: -th-bg-card; -fx-text-fill: -th-cream; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: -th-border;");

        Label paymentNote = new Label("💳 Оплата при получении (постоплата)");
        paymentNote.setStyle("-fx-text-fill: -th-success; -fx-font-size: 12px; -fx-padding: 4 0 0 0;");

        deliveryCard.getChildren().addAll(deliveryTitle, grid, commentLabel, commentArea, paymentNote);

        // ====== Общая ошибка ======
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: -th-danger; -fx-background-color: rgba(239,68,68,0.1); -fx-padding: 8 16; -fx-background-radius: 8;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // ====== Кнопка подтверждения ======
        Button confirmBtn = new Button("✓ Подтвердить заказ");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setPrefHeight(50);
        confirmBtn.setPrefWidth(300);
        confirmBtn.setStyle("-fx-font-size: 16px; -fx-background-color: -th-accent; -fx-text-fill: -th-cream; -fx-background-radius: 8; -fx-font-weight: bold;");
        confirmBtn.setTooltip(new javafx.scene.control.Tooltip("Подтвердить и оформить заказ"));
        confirmBtn.setOnAction(e -> {
            // Сброс ошибок
            addressField.setStyle(FIELD_NORMAL);
            addressError.setVisible(false); addressError.setManaged(false);
            phoneCombo.setStyle(FIELD_NORMAL);
            phoneError.setVisible(false); phoneError.setManaged(false);
            intervalCombo.setStyle(FIELD_NORMAL);
            intervalError.setVisible(false); intervalError.setManaged(false);
            errorLabel.setVisible(false); errorLabel.setManaged(false);

            boolean hasError = false;

            // Валидация адреса
            String address = addressField.getText();
            if (address == null || address.trim().isEmpty()) {
                addressField.setStyle(FIELD_ERROR);
                addressError.setText("Введите адрес доставки");
                addressError.setVisible(true); addressError.setManaged(true);
                hasError = true;
            }

            // Валидация телефона
            String phone = phoneCombo.getValue();
            if (phone == null) phone = phoneCombo.getEditor().getText();
            if (phone == null || !phone.matches("^\\+7\\d{10}$")) {
                phoneCombo.setStyle(FIELD_ERROR);
                phoneError.setText(phone == null || phone.isEmpty() ? "Введите контактный телефон" : "Формат: +7XXXXXXXXXX");
                phoneError.setVisible(true); phoneError.setManaged(true);
                hasError = true;
            }

            // Валидация интервала
            String interval = intervalCombo.getValue();
            if (interval == null || interval.isEmpty()) {
                intervalCombo.setStyle(FIELD_ERROR);
                intervalError.setText("Выберите интервал доставки");
                intervalError.setVisible(true); intervalError.setManaged(true);
                hasError = true;
            }

            // Валидация даты
            if (datePicker.getValue() == null) {
                datePicker.setStyle(FIELD_ERROR);
                hasError = true;
            }

            if (hasError) return;

            // Формируем строку интервала с датой
            String deliveryInterval = datePicker.getValue().toString() + " " + interval;
            String comment = commentArea.getText();

            String error = orderService.placeOrder(address, phone, deliveryInterval, comment);
            if (error != null) {
                errorLabel.setText("⚠ " + error);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            } else {
                showSuccess();
            }
        });

        HBox confirmBox = new HBox();
        confirmBox.setAlignment(Pos.CENTER);
        confirmBox.getChildren().add(confirmBtn);

        root.getChildren().addAll(backBtn, heading, summary, deliveryCard, errorLabel, confirmBox);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: -th-bg-primary; -fx-background: -th-bg-primary;");
        return scroll;
    }

    private Label createErrorHint() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: -th-danger; -fx-font-size: 11px; -fx-padding: -4 0 0 4;");
        label.setWrapText(true);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private void showSuccess() {
        Stage successStage = DialogHelper.createStage(MainApp.getPrimaryStage(), true);

        VBox root = new VBox(24);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setPrefWidth(450);
        root.setStyle(DialogHelper.cardStyle());

        // Клип — единственный надёжный способ скруглить все 4 угла в JavaFX
        root.layoutBoundsProperty().addListener((obs, old, b) -> {
            if (b.getWidth() > 0 && b.getHeight() > 0) {
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(
                    b.getWidth(), b.getHeight());
                clip.setArcWidth(32);
                clip.setArcHeight(32);
                root.setClip(clip);
            }
        });

        Label successIcon = new Label("🎉");
        successIcon.setStyle("-fx-font-size: 80px;");

        VBox textContent = new VBox(8);
        textContent.setAlignment(Pos.CENTER);

        Label title = new Label("Заказ оформлен!");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: -th-cream;");

        Label subtitle = new Label("Ваш заказ успешно принят в работу");
        subtitle.setStyle("-fx-text-fill: -th-success; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label details = new Label("Оплата будет произведена при получении.\nМы уже начали готовить товары к отправке.");
        details.setStyle("-fx-text-fill: -th-text-secondary; -fx-text-alignment: center; -fx-line-spacing: 5;");
        details.setWrapText(true);
        
        textContent.getChildren().addAll(title, subtitle, details);

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(20, 0, 0, 0));

        Button toOrdersBtn = new Button("Мои заказы");
        toOrdersBtn.getStyleClass().add("btn-primary");
        toOrdersBtn.setPrefWidth(160);
        toOrdersBtn.setPrefHeight(45);
        toOrdersBtn.setTooltip(new javafx.scene.control.Tooltip("Перейти к списку заказов"));
        toOrdersBtn.setOnAction(e -> {
            successStage.close();
            mainLayout.showOrders();
        });

        Button toCatalogBtn = new Button("В каталог");
        toCatalogBtn.getStyleClass().add("btn-secondary");
        toCatalogBtn.setPrefWidth(160);
        toCatalogBtn.setPrefHeight(45);
        toCatalogBtn.setTooltip(new javafx.scene.control.Tooltip("Вернуться в каталог"));
        toCatalogBtn.setOnAction(e -> {
            successStage.close();
            mainLayout.showCatalog();
        });

        actions.getChildren().addAll(toOrdersBtn, toCatalogBtn);
        root.getChildren().addAll(successIcon, textContent, actions);

        successStage.centerOnScreen();
        DialogHelper.applyTransparentSceneAndWait(root, successStage);
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
