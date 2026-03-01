package com.techhaven.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FavoriteTest {

    @Test
    @DisplayName("Конструктор по умолчанию")
    void defaultConstructor() {
        Favorite f = new Favorite();
        assertEquals(0, f.getId());
        assertEquals(0, f.getUserId());
        assertEquals(0, f.getProductId());
    }

    @Test
    @DisplayName("Параметризированный конструктор")
    void parameterizedConstructor() {
        Favorite f = new Favorite(1, 42);
        assertEquals(1, f.getUserId());
        assertEquals(42, f.getProductId());
        assertNotNull(f.getCreatedAt());
    }

    @Test
    @DisplayName("Transient-поля для отображения")
    void transientFields() {
        Favorite f = new Favorite();
        f.setProductName("SSD Samsung");
        f.setProductPrice(8500);
        f.setCategory("Накопители");
        f.setStockQuantity(50);
        f.setDescription("Быстрый SSD");
        f.setSpecifications("1 ТБ NVMe");

        assertEquals("SSD Samsung", f.getProductName());
        assertEquals(8500, f.getProductPrice());
        assertEquals("Накопители", f.getCategory());
        assertEquals(50, f.getStockQuantity());
        assertEquals("Быстрый SSD", f.getDescription());
        assertEquals("1 ТБ NVMe", f.getSpecifications());
    }

    @Test
    @DisplayName("getFormattedPrice форматирует цену с ₽")
    void formattedPrice() {
        Favorite f = new Favorite();
        f.setProductPrice(25000);
        String result = f.getFormattedPrice();
        assertTrue(result.contains("₽"));
        assertTrue(result.contains("25"));
    }

    @Test
    @DisplayName("Сеттеры id, createdAt")
    void idAndCreatedAt() {
        Favorite f = new Favorite();
        LocalDateTime now = LocalDateTime.now();
        f.setId(77);
        f.setCreatedAt(now);
        assertEquals(77, f.getId());
        assertEquals(now, f.getCreatedAt());
    }
}
