package com.techhaven.model;

import java.time.LocalDateTime;

/**
 * Модель записи «Избранное» (товар в списке желаний покупателя).
 *
 * <p>Соответствует таблице {@code Favorites} в БД.
 * Транзиентные поля заполняются из JOIN с {@code Products}.</p>
 *
 * @see com.techhaven.repository.FavoriteRepository
 */
public class Favorite {
    private int id;                   // PK — уникальный идентификатор записи
    private int userId;               // FK → Users.id (чьё избранное)
    private int productId;            // FK → Products.id (какой товар)
    private LocalDateTime createdAt;  // Дата/время добавления в избранное

    // Транзиентные поля для отображения (из JOIN Products)
    private String productName;       // Название товара
    private double productPrice;      // Цена товара (₽)
    private String category;          // Категория товара
    private int stockQuantity;        // Остаток на складе (шт.)
    private String description;       // Описание товара
    private String specifications;    // Технические характеристики

    public Favorite() {}

    public Favorite(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public String getFormattedPrice() {
        return String.format("%,.0f ₽", productPrice);
    }
}
