package com.techhaven.model;

import java.time.LocalDateTime;

/**
 * Модель товара в каталоге интернет-магазина электроники.
 *
 * <p>Соответствует таблице {@code Products} в БД.
 * Поле {@code category} заполняется из JOIN с таблицей {@code Categories}.</p>
 *
 * <p>Логика остатков на складе ({@code stockQuantity}):
 * <ul>
 *   <li>0 — «Нет в наличии» (stock-out, красный)</li>
 *   <li>1–5 — «Мало» (stock-low, жёлтый)</li>
 *   <li>&gt;5 — «В наличии» (stock-available, зелёный)</li>
 * </ul></p>
 *
 * @see com.techhaven.repository.ProductRepository
 */
public class Product {
    private int id;                   // PK — уникальный идентификатор товара
    private String name;              // Название товара (уникально в БД)
    private String description;       // Краткое описание для карточки
    private int categoryId;           // FK → Categories.id
    private String category;          // Название категории (из JOIN, не хранится в Products)
    private double price;             // Цена в рублях (₽)
    private int stockQuantity;        // Остаток на складе (шт.)
    private String specifications;    // Тех. характеристики (формат: ключ:значение;…)
    private String imagePath;         // Относительный путь к изображению
    private LocalDateTime createdAt;  // Дата/время добавления
    private LocalDateTime updatedAt;  // Дата/время последнего изменения

    public Product() {}

    public Product(String name, String description, int categoryId, double price,
                   int stockQuantity, String specifications) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.specifications = specifications;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFormattedPrice() {
        return String.format("%,.0f ₽", price);
    }

    public String getStockStatus() {
        if (stockQuantity <= 0) return "Нет в наличии";
        if (stockQuantity <= 5) return "Мало";
        return "В наличии";
    }
}
