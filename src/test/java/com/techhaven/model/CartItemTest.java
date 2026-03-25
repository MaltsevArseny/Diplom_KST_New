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

    // ─── Тесты пересчёта subtotal при изменении quantity ────────────────────
    // (покрывают логику, реализованную в CartView: plusBtn и minusBtn)

    @Test
    @DisplayName("Subtotal пересчитывается при увеличении quantity (как в CartView plusBtn)")
    void getSubtotalAfterQuantityIncrease() {
        CartItem ci = new CartItem();
        ci.setProductPrice(2000.0);
        ci.setQuantity(2);
        assertEquals(4000.0, ci.getSubtotal(), 0.01);

        // plusBtn увеличивает quantity и ожидает обновлённый subtotal
        ci.setQuantity(3);
        assertEquals(6000.0, ci.getSubtotal(), 0.01,
            "getSubtotal должен вернуть 6000 после увеличения quantity до 3");
    }

    @Test
    @DisplayName("Subtotal пересчитывается при уменьшении quantity (как в CartView minusBtn)")
    void getSubtotalAfterQuantityDecrease() {
        CartItem ci = new CartItem();
        ci.setProductPrice(2000.0);
        ci.setQuantity(3);
        assertEquals(6000.0, ci.getSubtotal(), 0.01);

        // minusBtn уменьшает quantity и ожидает обновлённый subtotal
        ci.setQuantity(1);
        assertEquals(2000.0, ci.getSubtotal(), 0.01,
            "getSubtotal должен вернуть 2000 после уменьшения quantity до 1");
    }

    @Test
    @DisplayName("getProductPrice() возвращает цену единицы товара")
    void getProductPriceReturnsCorrectValue() {
        CartItem ci = new CartItem();
        ci.setProductPrice(9990.0);
        assertEquals(9990.0, ci.getProductPrice(), 0.001);
    }

    @Test
    @DisplayName("getSubtotal = getProductPrice * quantity (точная формула)")
    void subtotalMatchesQuantityTimesPrice() {
        CartItem ci = new CartItem();
        double price = 3333.33;
        int qty = 7;
        ci.setProductPrice(price);
        ci.setQuantity(qty);
        assertEquals(price * qty, ci.getSubtotal(), 0.01);
    }
}
