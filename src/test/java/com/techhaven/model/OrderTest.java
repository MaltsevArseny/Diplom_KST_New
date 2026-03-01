package com.techhaven.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    @DisplayName("Конструктор по умолчанию")
    void defaultConstructor() {
        Order o = new Order();
        assertEquals(0, o.getId());
        assertEquals(0, o.getUserId());
        assertNull(o.getStatus());
        assertEquals(0.0, o.getTotalAmount());
    }

    @Test
    @DisplayName("Все геттеры/сеттеры")
    void allSettersGetters() {
        Order o = new Order();
        LocalDateTime now = LocalDateTime.now();

        o.setId(1);
        o.setUserId(5);
        o.setOrderDate("2026-02-28");
        o.setStatus("Новый");
        o.setDeliveryAddress("Москва, ул. Тверская 1");
        o.setContactPhone("+79001234567");
        o.setDeliveryTimeInterval("10:00-12:00");
        o.setComment("Позвонить за час");
        o.setPlannedDeliveryDate("2026-03-01");
        o.setPlannedDeliveryInterval("14:00-16:00");
        o.setTotalAmount(45000);
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        o.setUsername("Иван");

        assertEquals(1, o.getId());
        assertEquals(5, o.getUserId());
        assertEquals("2026-02-28", o.getOrderDate());
        assertEquals("Новый", o.getStatus());
        assertEquals("Москва, ул. Тверская 1", o.getDeliveryAddress());
        assertEquals("+79001234567", o.getContactPhone());
        assertEquals("10:00-12:00", o.getDeliveryTimeInterval());
        assertEquals("Позвонить за час", o.getComment());
        assertEquals("2026-03-01", o.getPlannedDeliveryDate());
        assertEquals("14:00-16:00", o.getPlannedDeliveryInterval());
        assertEquals(45000, o.getTotalAmount());
        assertEquals(now, o.getCreatedAt());
        assertEquals(now, o.getUpdatedAt());
        assertEquals("Иван", o.getUsername());
    }

    @Test
    @DisplayName("getFormattedTotal форматирует сумму с ₽")
    void formattedTotal() {
        Order o = new Order();
        o.setTotalAmount(123456);
        String formatted = o.getFormattedTotal();
        assertTrue(formatted.contains("₽"));
        assertTrue(formatted.contains("123"));
    }
}
