package com.techhaven.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("Конструктор по умолчанию создаёт объект с null-полями")
    void defaultConstructor() {
        Product p = new Product();
        assertEquals(0, p.getId());
        assertNull(p.getName());
        assertNull(p.getCategory());
        assertEquals(0.0, p.getPrice());
        assertEquals(0, p.getStockQuantity());
    }

    @Test
    @DisplayName("Параметризированный конструктор заполняет все поля")
    void parameterizedConstructor() {
        Product p = new Product("Intel i7", "Мощный процессор", 1, 35000, 10, "8 ядер, 3.6 ГГц");
        p.setCategory("Процессоры"); // в реальности заполняется из JOIN
        assertEquals("Intel i7", p.getName());
        assertEquals("Мощный процессор", p.getDescription());
        assertEquals(1, p.getCategoryId());
        assertEquals("Процессоры", p.getCategory());
        assertEquals(35000, p.getPrice());
        assertEquals(10, p.getStockQuantity());
        assertEquals("8 ядер, 3.6 ГГц", p.getSpecifications());
        assertNotNull(p.getCreatedAt());
        assertNotNull(p.getUpdatedAt());
    }

    @Test
    @DisplayName("Геттеры и сеттеры работают корректно")
    void gettersAndSetters() {
        Product p = new Product();
        p.setId(42);
        p.setName("RTX 4090");
        p.setDescription("Топовая видеокарта");
        p.setCategory("Видеокарты");
        p.setPrice(179990);
        p.setStockQuantity(3);
        p.setSpecifications("24 ГБ GDDR6X");
        p.setImagePath("/images/rtx4090.png");

        LocalDateTime now = LocalDateTime.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        assertEquals(42, p.getId());
        assertEquals("RTX 4090", p.getName());
        assertEquals("Топовая видеокарта", p.getDescription());
        assertEquals("Видеокарты", p.getCategory());
        assertEquals(179990, p.getPrice());
        assertEquals(3, p.getStockQuantity());
        assertEquals("24 ГБ GDDR6X", p.getSpecifications());
        assertEquals("/images/rtx4090.png", p.getImagePath());
        assertEquals(now, p.getCreatedAt());
        assertEquals(now, p.getUpdatedAt());
    }

    @Test
    @DisplayName("getFormattedPrice форматирует цену с разделителем тысяч и символом ₽")
    void formattedPrice() {
        Product p = new Product();
        p.setPrice(179990);
        String formatted = p.getFormattedPrice();
        assertTrue(formatted.contains("₽"));
        // Должно быть "179 990 ₽" или "179,990 ₽" в зависимости от локали
        assertTrue(formatted.contains("179"));
    }

    @Test
    @DisplayName("getStockStatus возвращает корректные статусы")
    void stockStatus() {
        Product p = new Product();

        p.setStockQuantity(0);
        assertEquals("Нет в наличии", p.getStockStatus());

        p.setStockQuantity(-1);
        assertEquals("Нет в наличии", p.getStockStatus());

        p.setStockQuantity(3);
        assertEquals("Мало", p.getStockStatus());

        p.setStockQuantity(5);
        assertEquals("Мало", p.getStockStatus());

        p.setStockQuantity(6);
        assertEquals("В наличии", p.getStockStatus());

        p.setStockQuantity(100);
        assertEquals("В наличии", p.getStockStatus());
    }

    @Test
    @DisplayName("Граничные значения остатка для фильтра 0/1-3/>3")
    void stockFilterBoundaryValues() {
        Product p = new Product();

        // Остаток 0 → «Нет в наличии»
        p.setStockQuantity(0);
        assertTrue(p.getStockQuantity() <= 0, "stock=0 попадает в фильтр 'Нет в наличии'");
        assertEquals("Нет в наличии", p.getStockStatus());

        // Остаток 1 → «Мало» (фильтр 1-3)
        p.setStockQuantity(1);
        assertTrue(p.getStockQuantity() >= 1 && p.getStockQuantity() <= 3, "stock=1 попадает в фильтр '1–3 шт.'");
        assertEquals("Мало", p.getStockStatus());

        // Остаток 3 → «Мало» (верхняя граница фильтра 1-3)
        p.setStockQuantity(3);
        assertTrue(p.getStockQuantity() >= 1 && p.getStockQuantity() <= 3, "stock=3 попадает в фильтр '1–3 шт.'");
        assertEquals("Мало", p.getStockStatus());

        // Остаток 4 → «Мало» по getStockStatus (≤5), но фильтр >3 (другая категория)
        p.setStockQuantity(4);
        assertTrue(p.getStockQuantity() > 3, "stock=4 попадает в фильтр 'Более 3 шт.'");

        // Остаток 10 → «В наличии»
        p.setStockQuantity(10);
        assertTrue(p.getStockQuantity() > 3, "stock=10 попадает в фильтр 'Более 3 шт.'");
        assertEquals("В наличии", p.getStockStatus());
    }

    @Test
    @DisplayName("getFormattedPrice для нулевой цены")
    void formattedPriceZero() {
        Product p = new Product();
        p.setPrice(0);
        String formatted = p.getFormattedPrice();
        assertTrue(formatted.contains("₽"));
        assertTrue(formatted.contains("0"));
    }

    @Test
    @DisplayName("Параметризированный конструктор: imagePath по умолчанию null")
    void parameterizedConstructorImagePathNull() {
        Product p = new Product("Test", "Desc", 1, 100, 5, "Spec");
        assertNull(p.getImagePath(), "imagePath должен быть null по умолчанию");
    }
}
