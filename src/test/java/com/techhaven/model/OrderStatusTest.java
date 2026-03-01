package com.techhaven.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    @DisplayName("Enum содержит 8 статусов")
    void enumValues() {
        assertEquals(8, OrderStatus.values().length);
    }

    @Test
    @DisplayName("getDisplayName возвращает русское название")
    void displayNames() {
        assertEquals("Новый", OrderStatus.NEW.getDisplayName());
        assertEquals("В обработке", OrderStatus.PROCESSING.getDisplayName());
        assertEquals("Подтверждён", OrderStatus.CONFIRMED.getDisplayName());
        assertEquals("Собран", OrderStatus.ASSEMBLED.getDisplayName());
        assertEquals("Отправлен", OrderStatus.SHIPPED.getDisplayName());
        assertEquals("Доставлен", OrderStatus.DELIVERED.getDisplayName());
        assertEquals("Завершён", OrderStatus.COMPLETED.getDisplayName());
        assertEquals("Отменён", OrderStatus.CANCELLED.getDisplayName());
    }

    @Test
    @DisplayName("fromString парсит по displayName")
    void fromStringValid() {
        assertEquals(OrderStatus.NEW, OrderStatus.fromString("Новый"));
        assertEquals(OrderStatus.PROCESSING, OrderStatus.fromString("В обработке"));
        assertEquals(OrderStatus.COMPLETED, OrderStatus.fromString("Завершён"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.fromString("Отменён"));
    }

    @Test
    @DisplayName("fromString нормализует ё/е")
    void fromStringNormalization() {
        assertEquals(OrderStatus.COMPLETED, OrderStatus.fromString("Завершен"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.fromString("Отменен"));
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.fromString("Подтвержден"));
    }

    @Test
    @DisplayName("fromString — null → NEW")
    void fromStringNull() {
        assertEquals(OrderStatus.NEW, OrderStatus.fromString(null));
    }

    @Test
    @DisplayName("isTerminal — только Завершён и Отменён")
    void isTerminal() {
        assertTrue(OrderStatus.COMPLETED.isTerminal());
        assertTrue(OrderStatus.CANCELLED.isTerminal());
        assertFalse(OrderStatus.NEW.isTerminal());
        assertFalse(OrderStatus.PROCESSING.isTerminal());
        assertFalse(OrderStatus.SHIPPED.isTerminal());
    }

    @Test
    @DisplayName("toString возвращает displayName")
    void toStringTest() {
        assertEquals("Новый", OrderStatus.NEW.toString());
        assertEquals("Завершён", OrderStatus.COMPLETED.toString());
    }
}
