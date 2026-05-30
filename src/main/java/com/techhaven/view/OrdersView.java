package com.techhaven.view;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.techhaven.model.Order;
import com.techhaven.model.OrderItem;
import com.techhaven.model.OrderStatusHistory;
import com.techhaven.service.OrderService;
import com.techhaven.view.component.OrderReceiptPane;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class OrdersView {
    private final MainLayout mainLayout;
    private final OrderService orderService = new OrderService();
    private VBox ordersList;

    private static final String[] ALL_STATUSES = {
        "Все", "Новый", "В обработке", "Подтверждён", "Собран", "Отправлен", "Доставлен", "Выдан", "Завершён", "Отменён"
    };

    private static final List<String> STATUS_ORDER = List.of(
        "Новый", "В обработке", "Подтверждён", "Собран", "Отправлен", "Доставлен", "Выдан", "Завершён", "Отменён"
    );

    private static final int PAGE_SIZE = 10;

    public OrdersView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
    }

    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("📦 Мои заказы");
        heading.getStyleClass().add("heading");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label filterLabel = new Label("Фильтр:");
        filterLabel.setStyle("-fx-text-fill: -th-text-secondary;");
        
        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList(ALL_STATUSES));
        statusFilter.setValue("Все");
        statusFilter.setPrefWidth(150);
        statusFilter.setOnAction(e -> renderOrders(statusFilter.getValue()));

        topBar.getChildren().addAll(heading, sp, filterLabel, statusFilter);

        this.ordersList = new VBox(12);
        renderOrders("Все");

        ScrollPane scroll = new ScrollPane(ordersList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(topBar, scroll);
        return root;
    }

    private void renderOrders(String filter) {
        ordersList.getChildren().clear();
        List<Order> orders = orderService.getMyOrders();

        if (orders.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            Label emptyLabel = new Label("У вас пока нет заказов");
            emptyLabel.getStyleClass().add("empty-state");
            Button goShop = new Button("Перейти в каталог");
            goShop.getStyleClass().add("btn-primary");
            goShop.setTooltip(new javafx.scene.control.Tooltip("Открыть каталог товаров"));
            goShop.setOnAction(e -> mainLayout.showCatalog());
            empty.getChildren().addAll(emptyLabel, goShop);
            ordersList.getChildren().add(empty);
            return;
        }

        // Фильтрация
        List<Order> filtered = orders.stream()
            .filter(o -> "Все".equals(filter) || filter.equals(o.getStatus()))
            .sorted(Comparator.comparing(Order::getOrderDate).reversed())
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Label noMatch = new Label("Нет заказов со статусом \"" + filter + "\"");
            noMatch.getStyleClass().add("label-secondary");
            noMatch.setPadding(new Insets(40));
            ordersList.getChildren().add(noMatch);
            return;
        }

        // Группировка по статусу
        Map<String, List<Order>> grouped = new LinkedHashMap<>();
        for (String s : STATUS_ORDER) {
            List<Order> group = filtered.stream()
                .filter(o -> s.equals(o.getStatus()))
                .collect(Collectors.toList());
            if (!group.isEmpty()) {
                grouped.put(s, group);
            }
        }

        // Аккордион: TitledPane для каждой группы
        boolean firstExpanded = true;
        for (Map.Entry<String, List<Order>> entry : grouped.entrySet()) {
            String status = entry.getKey();
            List<Order> groupOrders = entry.getValue();
            String accent = OrderUiSupport.statusColor(status);

            TitledPane section = new TitledPane();
            section.setText(status.toUpperCase() + " (" + groupOrders.size() + ")");
            section.setExpanded(firstExpanded);
            section.setAnimated(true);
            section.setStyle("-fx-text-fill: " + accent + ";");
            firstExpanded = false;

            VBox sectionContent = new VBox(6);
            sectionContent.setPadding(new Insets(4));

            // FlowPane для карточек (5 колонок)
            FlowPane cardsPane = new FlowPane();
            cardsPane.setHgap(6);
            cardsPane.setVgap(6);

            // Ленивая подгрузка: первые PAGE_SIZE карточек
            int initialCount = Math.min(PAGE_SIZE, groupOrders.size());
            for (int i = 0; i < initialCount; i++) {
                addOrderCard(cardsPane, groupOrders.get(i), accent);
            }

            sectionContent.getChildren().add(cardsPane);

            // Кнопка "Показать ещё" если есть ещё карточки
            if (groupOrders.size() > PAGE_SIZE) {
                final int[] loaded = {initialCount};
                Button showMore = new Button("Показать ещё (" + (groupOrders.size() - initialCount) + ")");
                showMore.getStyleClass().add("btn-secondary");
                showMore.setMaxWidth(Double.MAX_VALUE);
                showMore.setStyle("-fx-background-color: -th-bg-card; -fx-text-fill: " + accent + "; -fx-cursor: hand; -fx-padding: 6;");

                showMore.setOnAction(e -> {
                    int nextBatch = Math.min(loaded[0] + PAGE_SIZE, groupOrders.size());
                    for (int i = loaded[0]; i < nextBatch; i++) {
                        addOrderCard(cardsPane, groupOrders.get(i), accent);
                    }
                    loaded[0] = nextBatch;
                    int remaining = groupOrders.size() - loaded[0];
                    if (remaining <= 0) {
                        sectionContent.getChildren().remove(showMore);
                    } else {
                        showMore.setText("Показать ещё (" + remaining + ")");
                    }
                });

                sectionContent.getChildren().add(showMore);
            }

            section.setContent(sectionContent);
            ordersList.getChildren().add(section);
        }
    }

    private void addOrderCard(FlowPane cardsPane, Order order, String accent) {
        VBox card = createOrderCard(order);
        card.prefWidthProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
            () -> OrderUiSupport.cardWidthForContainer(cardsPane.getWidth()),
            cardsPane.widthProperty()
        ));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(card.getStyle() + "-fx-border-color: " + accent
            + " transparent transparent transparent; -fx-border-width: 3 0 0 0;");
        cardsPane.getChildren().add(card);
    }

    private VBox createOrderCard(Order order) {
            int bf = OrderUiSupport.ORDER_CARD_BASE_FONT_SIZE;
            int sf = bf - 1; // мелкий
            int tf = bf + 1; // крупный

            VBox card = new VBox(3);
            card.getStyleClass().add("card");
            card.setPadding(new Insets(5));

            // Заголовок
            HBox header = new HBox(4);
            header.setAlignment(Pos.CENTER_LEFT);

            Label orderNum = new Label("#" + order.getId());
            orderNum.setStyle("-fx-font-size: " + bf + "px; -fx-font-weight: bold; -fx-text-fill: -th-text-primary;");

            Label statusBadge = new Label(order.getStatus());
            statusBadge.setStyle(OrderUiSupport.statusBadgeStyle(order.getStatus())
                + "-fx-font-size: " + sf + "px; -fx-padding: 2 5;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label dateLabel = new Label(OrderUiSupport.formatDate(order.getOrderDate()));
            dateLabel.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: " + sf + "px;");

            header.getChildren().addAll(orderNum, statusBadge, spacer, dateLabel);

            // Инфо-блок доставки
            VBox deliveryInfo = new VBox(2);
            deliveryInfo.setStyle("-fx-background-color: -th-bg-card; -fx-padding: 4; -fx-background-radius: 4;");

            Label deliveryTitle = new Label("📍 Доставка");
            deliveryTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -th-accent-light; -fx-font-size: " + bf + "px;");

            Label addrLbl = new Label(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "—");
            addrLbl.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: " + bf + "px;");
            addrLbl.setWrapText(true);

            Label phoneLbl = new Label(order.getContactPhone() != null ? order.getContactPhone() : "—");
            phoneLbl.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: " + bf + "px;");

            deliveryInfo.getChildren().addAll(deliveryTitle, addrLbl, phoneLbl);

            if (order.getComment() != null && !order.getComment().trim().isEmpty()) {
                Label commentLbl = new Label("💬 " + order.getComment());
                commentLbl.setStyle("-fx-text-fill: -th-text-muted; -fx-font-style: italic; -fx-font-size: " + sf + "px;");
                commentLbl.setWrapText(true);
                deliveryInfo.getChildren().add(commentLbl);
            }

            // Ожидаемая дата от администратора
            if (order.getPlannedDeliveryDate() != null || order.getPlannedDeliveryInterval() != null) {
                String pDate = OrderUiSupport.formatDate(order.getPlannedDeliveryDate());
                String pTime = order.getPlannedDeliveryInterval() != null ? order.getPlannedDeliveryInterval() : "";
                Label plannedValue = new Label("🗓 " + pDate + " " + pTime);
                plannedValue.setStyle("-fx-text-fill: -th-success; -fx-font-size: " + bf + "px; -fx-font-weight: bold;");
                deliveryInfo.getChildren().add(plannedValue);
            }

            // Итого
            Label total = new Label(order.getFormattedTotal());
            total.setStyle("-fx-font-size: " + tf + "px; -fx-font-weight: bold; -fx-text-fill: -th-success;");

            TitledPane receiptPane = new TitledPane();
            receiptPane.setText("Данные для получения");
            receiptPane.setExpanded(false);
            receiptPane.setStyle("-fx-text-fill: -th-text-secondary;");
            receiptPane.setContent(OrderReceiptPane.create(order, true));

            // Позиции
            TitledPane itemsPane = createLazyPane("Состав заказа", () -> createItemsBox(order.getId()));

            // История статусов — прогресс-трекер
            TitledPane historyPane = createLazyPane("История статусов", () -> createHistoryBox(order));

            card.getChildren().addAll(header, deliveryInfo, total, receiptPane, itemsPane, historyPane);
            return card;
    }

    private TitledPane createLazyPane(String title, Supplier<Node> contentFactory) {
        TitledPane pane = new TitledPane();
        pane.setText(title);
        pane.setExpanded(false);
        pane.setStyle("-fx-text-fill: -th-text-secondary;");

        final boolean[] loaded = {false};
        pane.expandedProperty().addListener((obs, wasExpanded, expanded) -> {
            if (expanded && !loaded[0]) {
                pane.setContent(contentFactory.get());
                loaded[0] = true;
            }
        });
        return pane;
    }

    private VBox createItemsBox(int orderId) {
        VBox itemsBox = new VBox(8);
        List<OrderItem> items = orderService.getOrderItems(orderId);
        for (OrderItem item : items) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Button itemLink = new Button(item.getProductName());
            itemLink.setStyle("-fx-background-color: transparent; -fx-text-fill: -th-accent; -fx-cursor: hand; -fx-padding: 0; -fx-underline: true;");
            itemLink.setOnAction(e -> mainLayout.showProductDetail(item.getProductId()));

            Label qtyLbl = new Label(" × " + item.getQuantity());
            qtyLbl.setStyle("-fx-text-fill: -th-text-secondary;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label subtotal = new Label(item.getFormattedSubtotal());
            subtotal.setStyle("-fx-text-fill: -th-text-primary; -fx-font-weight: bold;");

            row.getChildren().addAll(itemLink, qtyLbl, sp, subtotal);
            itemsBox.getChildren().add(row);
        }
        return itemsBox;
    }

    private VBox createHistoryBox(Order order) {
            List<OrderStatusHistory> history = orderService.getStatusHistory(order.getId());
            String currentStatus = order.getStatus();
            boolean isCancelled = "Отменён".equals(currentStatus);

            // Выбираем путь: стандартный или «Отменен»
            List<String> path = isCancelled
                ? List.of("Новый", "В обработке", "Подтверждён", "Собран", "Отправлен", "Доставлен", "Выдан", "Завершён", "Отменён")
                : List.of("Новый", "В обработке", "Подтверждён", "Собран", "Отправлен", "Доставлен", "Выдан", "Завершён");

            int currentIdx = path.indexOf(currentStatus);

            VBox historyBox = new VBox(0);

            for (int si = 0; si < path.size(); si++) {
                String stepStatus = path.get(si);
                boolean isDone    = si < currentIdx || (si == currentIdx && !isCancelled);
                boolean isCurrent = si == currentIdx;

                // --- Цвета и иконка ---
                String dotColor, textColor, dotIcon;
                if (isCancelled && isCurrent) {
                    dotColor = "#ef4444"; textColor = "#ef4444"; dotIcon = "×";
                } else if (isDone) {
                    dotColor = "#10b981"; textColor = "#10b981"; dotIcon = "✓";
                } else if (isCurrent) {
                    dotColor = "#7c3aed"; textColor = "#f0f0f0"; dotIcon = "●";
                } else {
                    dotColor = "#3a3a50"; textColor = "#6b7280"; dotIcon = "○";
                }

                // --- Индикатор шага ---
                Label dot = new Label(dotIcon);
                dot.setStyle(
                    "-fx-text-fill: " + dotColor + "; -fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-min-width: 20; -fx-alignment: center;"
                );

                Label stepLabel = new Label(stepStatus);
                stepLabel.setStyle(
                    "-fx-text-fill: " + textColor + "; -fx-font-size: 11px;" +
                    (isCurrent ? " -fx-font-weight: bold;" : "")
                );

                // Подпись даты под шагом
                VBox stepTextBox = new VBox(1, stepLabel);
                history.stream()
                    .filter(h -> stepStatus.equals(h.getStatus()))
                    .findFirst()
                    .ifPresent(h -> {
                        if (h.getChangedAt() != null) {
                            String formatted = OrderUiSupport.formatDateTime(h.getChangedAt());
                            Label dateLbl = new Label(formatted);
                            dateLbl.setStyle("-fx-text-fill: -th-text-muted; -fx-font-size: 11px;");
                            stepTextBox.getChildren().add(dateLbl);
                        }
                    });

                HBox stepRow = new HBox(6, dot, stepTextBox);
                stepRow.setAlignment(Pos.CENTER_LEFT);
                stepRow.setPadding(new Insets(3, 6, 3, 6));
                if (isCurrent) {
                    stepRow.setStyle(
                        "-fx-background-color: rgba(124,58,237,0.12); -fx-background-radius: 8;"
                    );
                }

                historyBox.getChildren().add(stepRow);

                // Соединительная линия между шагами (не после последнего)
                if (si < path.size() - 1) {
                    Label line = new Label("");
                    line.setStyle(
                        "-fx-border-color: " + (si < currentIdx ? "-th-success" : "-th-border") + ";" +
                        "-fx-border-width: 0 0 0 2; -fx-min-height: 12; -fx-padding: 0 0 0 19;"
                    );
                    historyBox.getChildren().add(line);
                }
            }
            return historyBox;
    }

}
