package com.techhaven.view.component;

import com.techhaven.model.Order;
import com.techhaven.service.OrderReceiptService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Общий блок данных для получения заказа: штрих-код, код, дата и сумма.
 */
public final class OrderReceiptPane {
    private OrderReceiptPane() {}

    public static VBox create(Order order, boolean compact) {
        String code = OrderReceiptService.orderCode(order);
        String orderDate = OrderReceiptService.formatOrderDate(order);
        String total = order != null ? order.getFormattedTotal() : "—";

        VBox root = new VBox(compact ? 8 : 12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(compact ? 10 : 16));
        root.setMaxWidth(compact ? 320 : 420);
        root.setStyle(
            "-fx-background-color: -th-bg-primary;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: -th-border;" +
            "-fx-border-radius: 8;"
        );

        if (code.isEmpty()) {
            Label empty = new Label("Данные получения пока недоступны");
            empty.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 13px;");
            root.getChildren().add(empty);
            return root;
        }

        root.getChildren().add(Code128Barcode.create(
            code,
            compact ? 1.35 : 1.9,
            compact ? 54 : 82
        ));

        Label codeLabel = new Label(code);
        codeLabel.setStyle(
            "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
            "-fx-font-size: " + (compact ? 20 : 28) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: -th-text-primary;"
        );

        Label dateLabel = new Label("Дата оформления: " + orderDate);
        dateLabel.setStyle(
            "-fx-text-fill: -th-text-secondary;" +
            "-fx-font-size: " + (compact ? 12 : 14) + "px;"
        );

        Label totalLabel = new Label("Сумма заказа: " + total);
        totalLabel.setStyle(
            "-fx-text-fill: -th-success;" +
            "-fx-font-size: " + (compact ? 13 : 16) + "px;" +
            "-fx-font-weight: bold;"
        );

        root.getChildren().addAll(codeLabel, dateLabel, totalLabel);
        return root;
    }
}
