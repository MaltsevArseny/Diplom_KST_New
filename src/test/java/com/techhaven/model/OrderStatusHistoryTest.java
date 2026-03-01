package com.techhaven.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderStatusHistoryTest {

    @Test
    @DisplayName("Конструктор по умолчанию")
    void defaultConstructor() {
        OrderStatusHistory h = new OrderStatusHistory();
        assertEquals(0, h.getId());
        assertEquals(0, h.getOrderId());
        assertNull(h.getStatus());
        assertEquals(0, h.getChangedBy());
    }

    @Test
    @DisplayName("Параметризированный конструктор")
    void parameterizedConstructor() {
        OrderStatusHistory h = new OrderStatusHistory(5, 2, 1);
        h.setStatus("В обработке"); // в реальности из JOIN
        assertEquals(5, h.getOrderId());
        assertEquals(2, h.getStatusId());
        assertEquals("В обработке", h.getStatus());
        assertEquals(1, h.getChangedBy());
    }

    @Test
    @DisplayName("Все геттеры/сеттеры")
    void allSettersGetters() {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setId(10);
        h.setOrderId(3);
        h.setStatus("Доставлен");
        h.setChangedAt("2026-02-28 15:30:00");
        h.setChangedBy(2);
        h.setChangedByName("Администратор");

        assertEquals(10, h.getId());
        assertEquals(3, h.getOrderId());
        assertEquals("Доставлен", h.getStatus());
        assertEquals("2026-02-28 15:30:00", h.getChangedAt());
        assertEquals(2, h.getChangedBy());
        assertEquals("Администратор", h.getChangedByName());
    }
}
