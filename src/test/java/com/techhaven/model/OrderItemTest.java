package com.techhaven.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    @DisplayName("Конструктор по умолчанию")
    void defaultConstructor() {
        OrderItem oi = new OrderItem();
        assertEquals(0, oi.getId());
        assertEquals(0, oi.getOrderId());
        assertEquals(0, oi.getProductId());
        assertEquals(0, oi.getQuantity());
        assertEquals(0.0, oi.getPriceAtOrder());
    }

    @Test
    @DisplayName("Параметризированный конструктор")
    void parameterizedConstructor() {
        OrderItem oi = new OrderItem(1, 42, 3, 15000);
        assertEquals(1, oi.getOrderId());
        assertEquals(42, oi.getProductId());
        assertEquals(3, oi.getQuantity());
        assertEquals(15000, oi.getPriceAtOrder());
        assertNotNull(oi.getCreatedAt());
    }

    @Test
    @DisplayName("getSubtotal вычисляет итог позиции")
    void subtotal() {
        OrderItem oi = new OrderItem(1, 1, 5, 2000);
        assertEquals(10000, oi.getSubtotal(), 0.01);
    }

    @Test
    @DisplayName("getFormattedPrice и getFormattedSubtotal содержат ₽")
    void formattedPrices() {
        OrderItem oi = new OrderItem(1, 1, 2, 7500);
        assertTrue(oi.getFormattedPrice().contains("₽"));
        assertTrue(oi.getFormattedSubtotal().contains("₽"));
        assertTrue(oi.getFormattedSubtotal().contains("15"));
    }

    @Test
    @DisplayName("Transient-поле productName")
    void transientProductName() {
        OrderItem oi = new OrderItem();
        oi.setProductName("Видеокарта RTX");
        assertEquals("Видеокарта RTX", oi.getProductName());
    }

    @Test
    @DisplayName("Сеттеры id, createdAt")
    void setters() {
        OrderItem oi = new OrderItem();
        LocalDateTime now = LocalDateTime.now();
        oi.setId(55);
        oi.setOrderId(10);
        oi.setProductId(20);
        oi.setQuantity(1);
        oi.setPriceAtOrder(5000);
        oi.setCreatedAt(now);

        assertEquals(55, oi.getId());
        assertEquals(10, oi.getOrderId());
        assertEquals(20, oi.getProductId());
        assertEquals(1, oi.getQuantity());
        assertEquals(5000, oi.getPriceAtOrder());
        assertEquals(now, oi.getCreatedAt());
    }
}
