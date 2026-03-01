package com.techhaven.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.techhaven.MainApp;
import com.techhaven.model.Order;
import com.techhaven.model.OrderItem;
import com.techhaven.model.OrderStatusHistory;
import com.techhaven.service.OrderService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminOrdersView {
    private final OrderService orderService = new OrderService();
    private TableView<Order> table;
    private List<Order> allOrders;
    private TextField searchField;
    private ComboBox<String> statusFilter;

    private static final String[] STATUSES = {
        "Новый", "В обработке", "Подтверждён", "Собран", "Отправлен", "Доставлен", "Завершён", "Отменён"
    };

    private static final DateTimeFormatter FMT_DATETIME =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter FMT_DATE =
        DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DB_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AdminOrdersView(AdminLayout adminLayout) {
        // adminLayout передаётся для единообразия API, но не используется
    }

    @SuppressWarnings("unchecked")
    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        Label heading = new Label("📦 Управление заказами");
        heading.getStyleClass().add("heading");

        // ── Поиск ─────────────────────────────────────────────────
        searchField = new TextField();
        searchField.setPromptText("🔍 Поиск по клиенту или адресу...");
        searchField.setPrefWidth(280);
        searchField.setStyle(
            "-fx-background-color: #2a2a3e; -fx-text-fill: #f0f0f0; -fx-border-color: #3a3a50;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 13px;"
        );
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());

        // ── Фильтр по статусу ──────────────────────────────────
        statusFilter = new ComboBox<>(FXCollections.observableArrayList(
            "Все статусы", "Новый", "В обработке", "Подтверждён", "Собран",
            "Отправлен", "Доставлен", "Завершён", "Отменён"
        ));
        statusFilter.setValue("Все статусы");
        statusFilter.setPrefWidth(200);
        statusFilter.setOnAction(e -> applyFilter());

        Button clearBtn = new Button("× Сбросить");
        clearBtn.getStyleClass().addAll("button", "btn-small");
        clearBtn.setTooltip(new Tooltip("Сбросить фильтры"));
        clearBtn.setOnAction(e -> { searchField.clear(); statusFilter.setValue("Все статусы"); });

        Label countLabel = new Label();
        countLabel.setStyle("-fx-text-fill: #a0a0b8; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10, searchField, statusFilter, clearBtn, spacer, countLabel);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // ── Таблица ────────────────────────────────────────────────
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-font-size: 13px;");

        TableColumn<Order, Integer> idCol = new TableColumn<>("№");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(45);
        idCol.setMinWidth(40);
        idCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        addTooltip(idCol, "Уникальный номер заказа");

        TableColumn<Order, String> userCol = new TableColumn<>("Клиент");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userCol.setPrefWidth(100);
        userCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(userCol, "Имя клиента, оформившего заказ");

        // Дата оформления заказа (createdAt)
        TableColumn<Order, LocalDateTime> createdCol = new TableColumn<>("Дата заказа");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdCol.setPrefWidth(115);
        addTooltip(createdCol, "Дата и время создания заказа");
        createdCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime val, boolean empty) {
                super.updateItem(val, empty);
                setAlignment(Pos.CENTER);
                setStyle("-fx-font-size: 13px;");
                if (empty || val == null) { setText(""); return; }
                setText(val.format(FMT_DATETIME));
            }
        });
        // Сортировка по умолчанию — новые сверху
        createdCol.setSortType(TableColumn.SortType.DESCENDING);

        // Дата-время доставки (желаемое) — из deliveryTimeInterval (формат: "2026-03-01 10:00 — 12:00")
        TableColumn<Order, String> desiredCol = new TableColumn<>("Доставка (жел.)");
        desiredCol.setCellValueFactory(new PropertyValueFactory<>("deliveryTimeInterval"));
        desiredCol.setPrefWidth(130);
        addTooltip(desiredCol, "Желаемая дата и время доставки, указанные клиентом");
        desiredCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setAlignment(Pos.CENTER);
                if (empty || val == null || val.isEmpty()) { setText("—"); setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;"); return; }
                // val = "2026-03-01 10:00 — 12:00" → дата (yyyy-MM-dd) + пробел + интервал
                // Извлекаем дату (первые 10 символов, если начинается с yyyy-MM-dd)
                String datePart;
                String timePart;
                if (val.length() >= 10 && val.charAt(4) == '-' && val.charAt(7) == '-') {
                    datePart = formatDateStr(val.substring(0, 10));
                    timePart = val.length() > 10 ? val.substring(10).trim() : "";
                } else {
                    // Нет даты — отображаем как есть
                    datePart = "";
                    timePart = val.trim();
                }
                if (!datePart.isEmpty() && !timePart.isEmpty()) {
                    setText(datePart + "\n" + timePart);
                } else if (!datePart.isEmpty()) {
                    setText(datePart);
                } else {
                    setText(timePart);
                }
                setStyle("-fx-text-fill: #d0d0e8; -fx-font-size: 13px;");
            }
        });

        // Дата-время доставки (установленное) — из plannedDeliveryDate + plannedDeliveryInterval
        TableColumn<Order, String> scheduledCol = new TableColumn<>("Доставка (уст.)");
        scheduledCol.setCellValueFactory(new PropertyValueFactory<>("plannedDeliveryDate"));
        scheduledCol.setPrefWidth(130);
        addTooltip(scheduledCol, "Дата и время доставки, установленные администратором");
        scheduledCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                setAlignment(Pos.CENTER);
                if (empty || val == null || val.isEmpty()) {
                    setText("—");
                    setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
                    return;
                }
                // Формат: дата (первые 10 символов) + интервал на новой строке
                String datePart = val.length() >= 10 ? formatDateStr(val.substring(0, 10)) : formatDateStr(val);
                Order o = getTableView().getItems().get(getIndex());
                String interval = o.getPlannedDeliveryInterval();
                String timePart = (interval != null && !interval.isEmpty()) ? interval : "";
                setText(datePart + (timePart.isEmpty() ? "" : "\n" + timePart));
                setStyle("-fx-text-fill: #d0d0e8; -fx-font-size: 13px;");
            }
        });

        TableColumn<Order, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setStyle("-fx-alignment: CENTER;");
        addTooltip(statusCol, "Текущий статус заказа");
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); return; }
                setGraphic(styledStatusBadge(val)); setText(null);
            }
        });

        TableColumn<Order, Double> totalCol = new TableColumn<>("Сумма");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setPrefWidth(90);
        addTooltip(totalCol, "Итоговая сумма заказа");
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_RIGHT);
                setStyle("-fx-font-size: 13px;");
                setText(empty || item == null ? null : String.format("%,.0f ₽", item));
            }
        });

        TableColumn<Order, String> addressCol = new TableColumn<>("Адрес");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        addressCol.setPrefWidth(140);
        addressCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(addressCol, "Адрес доставки заказа");

        TableColumn<Order, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setPrefWidth(220);
        addTooltip(actionsCol, "Управление статусом и деталями заказа");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(STATUSES));
            private final Button applyBtn = new Button("✔");
            private final Button detailBtn = new Button("📋");
            {
                applyBtn.getStyleClass().addAll("btn-primary", "btn-small");
                applyBtn.setTooltip(new Tooltip("Применить выбранный статус"));
                applyBtn.setStyle("-fx-min-width: 28; -fx-max-width: 28; -fx-padding: 4 6;");
                detailBtn.getStyleClass().addAll("button", "btn-small");
                detailBtn.setTooltip(new Tooltip("Подробности заказа"));
                detailBtn.setStyle("-fx-min-width: 28; -fx-max-width: 28; -fx-padding: 4 6;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order o = getTableView().getItems().get(getIndex());
                    statusCombo.setValue(o.getStatus());
                    applyBtn.setOnAction(e -> {
                        String newStatus = statusCombo.getValue();
                        if (newStatus != null && !newStatus.equals(o.getStatus())) {
                            String error = orderService.updateStatus(o.getId(), newStatus);
                            if (error != null) {
                                DialogHelper.showWarning("Ошибка смены статуса", error);
                                statusCombo.setValue(o.getStatus());
                            } else {
                                loadOrders();
                            }
                        }
                    });
                    detailBtn.setOnAction(e -> showOrderDetail(o));
                    HBox box = new HBox(4, statusCombo, applyBtn, detailBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, userCol, createdCol, desiredCol, scheduledCol,
            statusCol, totalCol, addressCol, actionsCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Сортировка по умолчанию — по дате оформления (новые сверху)
        table.getSortOrder().add(createdCol);

        table.itemsProperty().addListener((obs, old, items) -> {
            if (items != null)
                countLabel.setText("Отображается: " + items.size()
                    + (allOrders != null ? " из " + allOrders.size() : ""));
        });

        loadOrders();

        root.getChildren().addAll(heading, toolbar, table);
        return root;
    }

    private void loadOrders() {
        allOrders = orderService.getAllOrders();
        // Сортировка: новые заказы сверху
        allOrders.sort(Comparator.comparing(Order::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));
        applyFilter();
    }

    private void applyFilter() {
        if (allOrders == null) return;
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String status = statusFilter != null ? statusFilter.getValue() : "Все статусы";

        List<Order> filtered = allOrders.stream()
            .filter(o -> {
                if ("Все статусы".equals(status)) return true;
                return status.equals(o.getStatus());
            })
            .filter(o -> {
                if (query.isEmpty()) return true;
                String name = o.getUsername() != null ? o.getUsername().toLowerCase() : "";
                String addr = o.getDeliveryAddress() != null ? o.getDeliveryAddress().toLowerCase() : "";
                return name.contains(query) || addr.contains(query);
            })
            .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showOrderDetail(Order order) {
        Stage stage = DialogHelper.createStage(MainApp.getPrimaryStage(), true);

        // ─── Заголовок ─────────────────────────────────────────────────────
        Label titleLbl = new Label("📦 Заказ #" + order.getId());
        titleLbl.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:#e2e8f0;");


        // ─── Основные данные ────────────────────────────────────────────
        VBox content = new VBox(8);

        String createdFormatted = order.getCreatedAt() != null
            ? order.getCreatedAt().format(FMT_DATETIME) : "—";
        String desiredDate = formatDateStr(order.getOrderDate());
        String desiredInterval = order.getDeliveryTimeInterval();
        String desiredFull = desiredDate
            + (desiredInterval != null && !desiredInterval.isEmpty() ? "  " + desiredInterval : "");
        String scheduledDate = formatDateStr(order.getPlannedDeliveryDate());
        String scheduledInterval = order.getPlannedDeliveryInterval();
        String scheduledFull = (order.getPlannedDeliveryDate() != null && !order.getPlannedDeliveryDate().isEmpty())
            ? scheduledDate + (scheduledInterval != null && !scheduledInterval.isEmpty() ? "  " + scheduledInterval : "")
            : "Не назначена";

        content.getChildren().addAll(
            infoRow("👤 Клиент:",  order.getUsername()),
            infoRow("📅 Оформлен:", createdFormatted),
            infoRow("🗓 Доставка (желаемое):", desiredFull),
            infoRow("🚚 Доставка (установл.):", scheduledFull),
            infoRow("📍 Адрес:",   order.getDeliveryAddress()),
            infoRow("💬 Комментарий:", order.getComment() != null ? order.getComment() : "—")
        );

        Label statusBadge = styledStatusBadge(order.getStatus());
        statusBadge.setStyle(statusBadge.getStyle() + "-fx-font-size:13px;");
        HBox statusRow = new HBox(8, sectionLabel("🔄 Статус:"), statusBadge);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(statusRow);

        Label totalLbl = new Label(order.getFormattedTotal());
        totalLbl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#10b981;");
        HBox totalRow = new HBox(8, sectionLabel("💰 Итого:"), totalLbl);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(totalRow);

        // ─── Состав ───────────────────────────────────────────────────
        content.getChildren().add(new Separator());
        content.getChildren().add(sectionLabel("🗞 Состав заказа:"));
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        for (OrderItem item : items) {
            Label lbl = new Label("  • " + item.getProductName() +
                " × " + item.getQuantity() + "  =  " + item.getFormattedSubtotal());
            lbl.setStyle("-fx-text-fill:#cbd5e1;-fx-font-size:13px;");
            content.getChildren().add(lbl);
        }

        // ─── Управление доставкой ───────────────────────────────────
        content.getChildren().add(new Separator());
        content.getChildren().add(sectionLabel("🚚 Установить дату доставки:"));

        DatePicker plannedDate = new DatePicker();
        plannedDate.setStyle("-fx-background-color:#252538;-fx-border-color:#3a3a50;-fx-border-radius:8;-fx-background-radius:8;");
        if (order.getPlannedDeliveryDate() != null) {
            try { plannedDate.setValue(LocalDate.parse(order.getPlannedDeliveryDate())); } catch (Exception ignored) {}
        }
        plannedDate.setPromptText("Дата доставки");

        ComboBox<String> plannedInterval = new ComboBox<>(FXCollections.observableArrayList(
            "09:00 — 12:00", "12:00 — 15:00", "15:00 — 18:00", "18:00 — 21:00"
        ));
        plannedInterval.setValue(order.getPlannedDeliveryInterval());
        plannedInterval.setPromptText("Интервал");
        plannedInterval.setStyle("-fx-background-color:#252538;-fx-border-color:#3a3a50;-fx-border-radius:8;");

        Button savePlannedBtn = new Button("💾 Сохранить доставку");
        savePlannedBtn.getStyleClass().add("btn-primary");
        savePlannedBtn.setTooltip(new Tooltip("Сохранить дату и интервал доставки"));
        savePlannedBtn.setOnAction(e -> {
            if (plannedDate.getValue() != null && plannedInterval.getValue() != null) {
                orderService.updatePlannedDelivery(order.getId(),
                    plannedDate.getValue().toString(), plannedInterval.getValue());
                stage.close();
                loadOrders();
            }
        });

        HBox plannedBox = new HBox(8, plannedDate, plannedInterval, savePlannedBtn);
        plannedBox.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(plannedBox);

        // ─── История статусов ────────────────────────────────────────
        content.getChildren().add(new Separator());
        content.getChildren().add(sectionLabel("📜 История статусов:"));
        List<OrderStatusHistory> hist = orderService.getStatusHistory(order.getId());
        for (OrderStatusHistory h : hist) {
            String changedAt = formatDateTimeStr(h.getChangedAt());
            String c = statusColor(h.getStatus());
            Label statusLbl = new Label(h.getStatus());
            statusLbl.setStyle(
                "-fx-background-color:" + c + ";-fx-text-fill:white;" +
                "-fx-font-weight:bold;-fx-padding:1 6;-fx-background-radius:4;-fx-font-size:11px;"
            );
            Label dateLbl = new Label("— " + changedAt +
                (h.getChangedByName() != null ? " (" + h.getChangedByName() + ")" : ""));
            dateLbl.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:12px;");
            HBox histRow = new HBox(6, new Label("  •"), statusLbl, dateLbl);
            histRow.setAlignment(Pos.CENTER_LEFT);
            content.getChildren().add(histRow);
        }

        // ─── Карточка ──────────────────────────────────────────────
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setMaxHeight(540);

        VBox card = new VBox(titleLbl, scroll);
        card.setPadding(new Insets(24));
        card.setPrefWidth(580);
        card.setStyle(DialogHelper.cardStyle());

        Button closeBtn = DialogHelper.createCloseButton(stage);
        DialogHelper.applyTransparentSceneAndWait(card, stage, closeBtn);
    }

    /** Стилизованная метка секции */
    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#a0a0b8;-fx-padding:4 0 2 0;");
        return l;
    }

    /** Строка информации: метка + значение */
    private HBox infoRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#6b7280;-fx-font-size:13px;");
        lbl.setMinWidth(180);
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill:#e2e8f0;-fx-font-size:13px;");
        val.setWrapText(true);
        HBox row = new HBox(8, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Форматирует строку даты из БД в dd.MM.yyyy */
    private String formatDateStr(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        try {
            // Пробуем yyyy-MM-dd
            LocalDate d = LocalDate.parse(raw.substring(0, Math.min(10, raw.length())));
            return d.format(FMT_DATE);
        } catch (Exception e) {
            return raw;
        }
    }

    /** Форматирует строку даты-времени из БД в dd.MM.yyyy HH:mm */
    private String formatDateTimeStr(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        try {
            String normalized = raw.replace("T", " ").replaceAll("\\.\\d+$", "");
            if (normalized.length() >= 19) {
                LocalDateTime ldt = LocalDateTime.parse(normalized.substring(0, 19), DB_FMT);
                return ldt.format(FMT_DATETIME);
            } else if (normalized.length() >= 16) {
                LocalDateTime ldt = LocalDateTime.parse(normalized.substring(0, 16),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return ldt.format(FMT_DATETIME);
            }
            return raw;
        } catch (Exception e) {
            return raw;
        }
    }

    /** Устанавливает подсказку (tooltip) на заголовок столбца таблицы */
    private <T> void addTooltip(TableColumn<Order, T> col, String text) {
        Label label = new Label(col.getText());
        label.setTooltip(new Tooltip(text));
        label.setStyle("-fx-text-fill: inherit; -fx-font-weight: inherit;");
        col.setGraphic(label);
        col.setText("");
    }

    /** Цвет статуса заказа (единый источник для всех UI элементов) */
    private static String statusColor(String status) {
        if (status == null) return "#6b7280";
        return switch (status) {
            case "Новый"       -> "#3b82f6";
            case "В обработке" -> "#f97316";
            case "Подтверждён" -> "#a855f7";
            case "Собран"      -> "#eab308";
            case "Отправлен"   -> "#ec4899";
            case "Доставлен"   -> "#22c55e";
            case "Завершён"    -> "#14b8a6";
            case "Отменён"     -> "#ef4444";
            default          -> "#6b7280";
        };
    }

    /** Создаёт стилизованный бейдж статуса заказа */
    private static Label styledStatusBadge(String status) {
        String c = statusColor(status);
        Label badge = new Label(status);
        badge.setStyle(
            "-fx-background-color:" + c + ";-fx-text-fill:white;" +
            "-fx-font-weight:bold;-fx-padding:2 8;-fx-background-radius:6;-fx-font-size:13px;"
        );
        return badge;
    }
}
