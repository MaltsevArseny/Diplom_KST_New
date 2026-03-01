package com.techhaven.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.techhaven.model.Product;

class ProductRepositoryTest {

    private final ProductRepository productRepo = new ProductRepository();

    @Test
    void findAllReturnsNonEmpty() {
        List<Product> products = productRepo.findAll();
        assertNotNull(products);
        assertFalse(products.isEmpty(), "Должны быть seed-товары");
    }

    @Test
    void findPagedReturnsLimitedResults() {
        List<Product> page = productRepo.findPaged(0, 10);
        assertNotNull(page);
        assertTrue(page.size() <= 10, "Размер страницы не должен превышать limit");
        assertFalse(page.isEmpty());
    }

    @Test
    void getTotalCountReturnsPositive() {
        int count = productRepo.getTotalCount();
        assertTrue(count >= 300, "Ожидается >= 300 seed товаров, получено " + count);
    }

    @Test
    void findByIdReturnsProduct() {
        // Берём первый товар из БД
        List<Product> all = productRepo.findPaged(0, 1);
        assertFalse(all.isEmpty());
        Product product = productRepo.findById(all.get(0).getId());
        assertNotNull(product);
        assertEquals(all.get(0).getId(), product.getId());
    }

    @Test
    void findByIdReturnsNullForNonExistent() {
        Product product = productRepo.findById(-999);
        assertNull(product);
    }

    @Test
    void findAllCategoriesReturnsNonEmpty() {
        List<String> categories = productRepo.findAllCategories();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
    }

    @Test
    void searchByQueryReturnsResults() {
        // Поиск пустым запросом должен вернуть все
        List<Product> results = productRepo.search("", null, null, null);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void searchByCategoryFilters() {
        List<String> categories = productRepo.findAllCategories();
        assertFalse(categories.isEmpty());
        String category = categories.get(0);
        List<Product> results = productRepo.search(null, category, null, null);
        assertNotNull(results);
        // Все результаты должны быть указанной категории
        for (Product p : results) {
            assertEquals(category, p.getCategory());
        }
    }

    @Test
    void searchByPriceRangeFilters() {
        List<Product> results = productRepo.search(null, null, 100.0, 500.0);
        assertNotNull(results);
        for (Product p : results) {
            assertTrue(p.getPrice() >= 100.0 && p.getPrice() <= 500.0,
                "Цена " + p.getPrice() + " вне диапазона [100, 500]");
        }
    }

    @Test
    void findPagedWithOffsetSkipsRecords() {
        List<Product> page0 = productRepo.findPaged(0, 5);
        List<Product> page1 = productRepo.findPaged(5, 5);
        assertFalse(page0.isEmpty());
        assertFalse(page1.isEmpty());
        // Первый элемент второй страницы != первый элемент первой
        assertNotEquals(page0.get(0).getId(), page1.get(0).getId());
    }
}
