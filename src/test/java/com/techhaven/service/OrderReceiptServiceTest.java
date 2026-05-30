package com.techhaven.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techhaven.model.Order;

class OrderReceiptServiceTest {

    @Test
    @DisplayName("orderCode возвращает восьмизначный код заказа")
    void orderCodeUsesPaddedOrderId() {
        Order order = new Order();
        order.setId(42);

        assertEquals("00000042", OrderReceiptService.orderCode(order));
    }

    @Test
    @DisplayName("formatOrderDate показывает дату оформления как dd.MM.yyyy HH:mm")
    void formatOrderDateUsesDisplayPattern() {
        assertEquals("23.05.2026 18:45",
            OrderReceiptService.formatOrderDate("2026-05-23 18:45:17"));
    }

    @Test
    @DisplayName("Code 128-C sequence содержит Start C, checksum и Stop")
    void code128CValuesIncludeChecksumAndStop() {
        List<Integer> values = OrderReceiptService.code128CValues("00000042");

        assertEquals(List.of(105, 0, 0, 0, 42, 67, 106), values);
    }

    @Test
    @DisplayName("code128CValues отклоняет значение без цифр")
    void code128CValuesRejectsEmptyDigits() {
        assertThrows(IllegalArgumentException.class,
            () -> OrderReceiptService.code128CValues("ABC"));
    }
}
