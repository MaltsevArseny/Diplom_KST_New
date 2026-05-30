package com.techhaven.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class OrderUiSupportTest {
    @Test
    void cardColumnsDependOnAvailableWidth() {
        assertEquals(1, OrderUiSupport.cardColumnsForWidth(320));
        assertEquals(2, OrderUiSupport.cardColumnsForWidth(640));
        assertEquals(3, OrderUiSupport.cardColumnsForWidth(900));
        assertEquals(4, OrderUiSupport.cardColumnsForWidth(1280));
        assertEquals(5, OrderUiSupport.cardColumnsForWidth(1600));
    }

    @Test
    void dateFormattingSupportsDatabaseValues() {
        assertEquals("23.05.2026", OrderUiSupport.formatDate("2026-05-23"));
        assertEquals("23.05.2026 14:05", OrderUiSupport.formatDateTime("2026-05-23 14:05:11"));
        assertEquals("23.05.2026 14:05", OrderUiSupport.formatDateTime("2026-05-23T14:05:11.123"));
        assertEquals("—", OrderUiSupport.formatDate(null));
    }

    @Test
    void issuedStatusHasDedicatedColor() {
        assertEquals("#14b8a6", OrderUiSupport.statusColor("Выдан"));
        assertEquals("#6b7280", OrderUiSupport.statusColor("Неизвестный"));
    }

    @Test
    void ordersViewDoesNotDependOnPhysicalScreenWidth() throws IOException {
        String content = Files.readString(Path.of("src/main/java/com/techhaven/view/OrdersView.java"));

        assertFalse(content.contains("Screen.getPrimary"),
            "Карточки заказов должны адаптироваться к ширине окна, а не монитора");
        assertFalse(content.contains("baseFontSize"),
            "Размер шрифта карточек не должен вычисляться от ширины экрана");
    }
}
