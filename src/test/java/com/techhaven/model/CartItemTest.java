package com.techhaven.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartItemTest {

    @Test
    @DisplayName("Конструктор по умолчанию")
    void defaultConstructor() {
        CartItem ci = new CartItem();
        assertEquals(0, ci.getId());
        assertEquals(0, ci.getUserId());
        assertEquals(0, ci.getProductId());
        assertEquals(0, ci.getQuantity());
    }

    @Test
    @DisplayName("Параметризированный конструктор")
    void parameterizedConstructor() {
        CartItem ci = new CartItem(1, 42, 3);
        assertEquals(1, ci.getUserId());
        assertEquals(42, ci.getProductId());
        assertEquals(3, ci.getQuantity());
        assertNotNull(ci.getCreatedAt());
        assertNotNull(ci.getUpdatedAt());
    }

    @Test
    @DisplayName("getSubtotal вычисляет итог")
    void subtotal() {
        CartItem ci = new CartItem();
        ci.setProductPrice(1500.0);
        ci.setQuantity(3);
        assertEquals(4500.0, ci.getSubtotal(), 0.01);
    }

    @Test
    @DisplayName("getSubtotal при нулевом количестве")
    void subtotalZero() {
        CartItem ci = new CartItem();
        ci.setProductPrice(1500.0);
        ci.setQuantity(0);
        assertEquals(0.0, ci.getSubtotal(), 0.01);
    }

    @Test
    @DisplayName("getFormattedSubtotal содержит символ ₽")
    void formattedSubtotal() {
        CartItem ci = new CartItem();
        ci.setProductPrice(10000);
        ci.setQuantity(2);
        String result = ci.getFormattedSubtotal();
        assertTrue(result.contains("₽"));
        assertTrue(result.contains("20"));
    }

    @Test
    @DisplayName("getFormattedPrice содержит символ ₽")
    void formattedPrice() {
        CartItem ci = new CartItem();
        ci.setProductPrice(5000);
        String result = ci.getFormattedPrice();
        assertTrue(result.contains("₽"));
        assertTrue(result.contains("5"));
    }

    @Test
    @DisplayName("Transient-поля (productName, category, stockQuantity)")
    void transientFields() {
        CartItem ci = new CartItem();
        ci.setProductName("SSD Kingston");
        ci.setCategory("Накопители");
        ci.setStockQuantity(25);

        assertEquals("SSD Kingston", ci.getProductName());
        assertEquals("Накопители", ci.getCategory());
        assertEquals(25, ci.getStockQuantity());
    }

    @Test
    @DisplayName("Сеттеры id, createdAt, updatedAt")
    void idAndTimestamps() {
        CartItem ci = new CartItem();
        LocalDateTime now = LocalDateTime.now();
        ci.setId(99);
        ci.setCreatedAt(now);
        ci.setUpdatedAt(now);

        assertEquals(99, ci.getId());
        assertEquals(now, ci.getCreatedAt());
        assertEquals(now, ci.getUpdatedAt());
    }
}
