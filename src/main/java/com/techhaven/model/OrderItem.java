package com.techhaven.model;

import java.time.LocalDateTime;

/**
 * Модель позиции заказа (конкретный товар и количество в заказе).
 *
 * <p>Соответствует таблице {@code OrderItems} в БД.
 * Поле {@code priceAtOrder} фиксирует цену на момент заказа, чтобы
 * последующие изменения цены товара не влияли на сумму заказа.</p>
 *
 * @see com.techhaven.repository.OrderRepository
 */
public class OrderItem {

    private int id;                   // PK — уникальный идентификатор позиции
    private int orderId;              // FK → Orders.id (к какому заказу)
    private int productId;            // FK → Products.id (какой товар)
    private int quantity;             // Количество единиц товара
    private double priceAtOrder;      // Цена за единицу на момент заказа (₽)
    private LocalDateTime createdAt;  // Дата/время добавления позиции

    // Транзиентное поле для отображения (из JOIN Products.name)
    private String productName;

    public OrderItem() {
    }

    public OrderItem(int orderId, int productId, int quantity, double priceAtOrder) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getSubtotal() {
        return priceAtOrder * quantity;
    }

    public String getFormattedPrice() {
        return String.format("%,.0f ₽", priceAtOrder);
    }

    public String getFormattedSubtotal() {
        return String.format("%,.0f ₽", getSubtotal());
    }
}
