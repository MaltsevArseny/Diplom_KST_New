package com.techhaven.model;

import java.time.LocalDateTime;

/**
 * Модель элемента корзины покупателя.
 *
 * <p>Соответствует таблице {@code Cart} в БД.
 * Транзиентные поля ({@code productName}, {@code productPrice} и др.)
 * заполняются из JOIN с таблицей {@code Products}.</p>
 *
 * @see com.techhaven.repository.CartRepository
 */
public class CartItem {

    private int id;                   // PK — уникальный идентификатор записи
    private int userId;               // FK → Users.id (чья корзина)
    private int productId;            // FK → Products.id (какой товар)
    private int quantity;             // Количество единиц (≥ 1, ≤ stockQuantity)
    private LocalDateTime createdAt;  // Дата/время добавления в корзину
    private LocalDateTime updatedAt;  // Дата/время последнего изменения количества

    // Транзиентные поля для отображения (из JOIN Products)
    private String productName;       // Название товара
    private double productPrice;      // Цена за единицу (₽)
    private int stockQuantity;        // Остаток на складе (для валидации количества)
    private String category;          // Категория товара

    public CartItem() {
    }

    public CartItem(int userId, int productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSubtotal() {
        return productPrice * quantity;
    }

    public String getFormattedSubtotal() {
        return String.format("%,.0f ₽", getSubtotal());
    }

    public String getFormattedPrice() {
        return String.format("%,.0f ₽", productPrice);
    }
}
