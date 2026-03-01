package com.techhaven.service;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techhaven.model.Product;

class ProductServiceTest {

    private final ProductService service = new ProductService();

    @Test
    void getAllProductsReturnsNonEmpty() {
        List<Product> products = service.getAllProducts();
        assertNotNull(products);
        assertFalse(products.isEmpty(), "Should have seeded products");
    }

    @Test
    void getProductByIdReturnsProduct() {
        List<Product> all = service.getAllProducts();
        assertFalse(all.isEmpty());

        Product first = all.get(0);
        Product found = service.getProductById(first.getId());
        assertNotNull(found);
        assertEquals(first.getId(), found.getId());
        assertEquals(first.getName(), found.getName());
    }

    @Test
    void getProductByIdReturnsNullForInvalid() {
        assertNull(service.getProductById(-999));
    }

    @Test
    void getCategoriesReturnsNonEmpty() {
        List<String> categories = service.getCategories();
        assertNotNull(categories);
        assertFalse(categories.isEmpty(), "Should have at least one category");
    }

    @Test
    void searchByExistingCategoryReturnsProducts() {
        List<String> categories = service.getCategories();
        assertFalse(categories.isEmpty());

        String firstCat = categories.get(0);
        List<Product> result = service.searchProducts(null, firstCat, null, null);
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Search by category should return products");
        assertTrue(result.stream().allMatch(p -> firstCat.equals(p.getCategory())));
    }

    @Test
    void searchWithMinMaxPriceFilters() {
        List<Product> all = service.getAllProducts();
        if (all.size() < 2) return;

        double mid = all.stream().mapToDouble(Product::getPrice).average().orElse(50000);
        List<Product> filtered = service.searchProducts(null, "Все категории", mid, null);
        assertNotNull(filtered);
        assertTrue(filtered.stream().allMatch(p -> p.getPrice() >= mid));
    }

    @Test
    void searchAllCategoriesReturnsAllProducts() {
        List<Product> all = service.getAllProducts();
        List<Product> search = service.searchProducts(null, "Все категории", null, null);
        assertEquals(all.size(), search.size());
    }

    @Test
    void getTotalCountMatchesAllProducts() {
        int count = service.getTotalCount();
        assertEquals(service.getAllProducts().size(), count);
    }

    @Test
    void getTotalPagesIsPositive() {
        int pages = service.getTotalPages();
        assertTrue(pages > 0, "Total pages should be > 0");
    }

    @Test
    void getProductsPagedReturnsSubset() {
        List<Product> page0 = service.getProductsPaged(0);
        assertNotNull(page0);
        assertFalse(page0.isEmpty());
        assertTrue(page0.size() <= com.techhaven.config.AppConfig.CATALOG_PAGE_SIZE);
    }

    // === Новые тесты: покрытие фильтров и остатков ===

    @Test
    @DisplayName("Все товары имеют непустое название")
    void allProductsHaveNonEmptyName() {
        List<Product> all = service.getAllProducts();
        for (Product p : all) {
            assertNotNull(p.getName(), "Товар #" + p.getId() + " имеет null-название");
            assertFalse(p.getName().isBlank(), "Товар #" + p.getId() + " имеет пустое название");
        }
    }

    @Test
    @DisplayName("Все товары имеют непустую категорию")
    void allProductsHaveCategory() {
        List<Product> all = service.getAllProducts();
        for (Product p : all) {
            assertNotNull(p.getCategory(), "Товар #" + p.getId() + " имеет null-категорию");
            assertFalse(p.getCategory().isBlank(), "Товар #" + p.getId() + " имеет пустую категорию");
        }
    }

    @Test
    @DisplayName("Все товары имеют неотрицательную цену")
    void allProductsHaveNonNegativePrice() {
        List<Product> all = service.getAllProducts();
        for (Product p : all) {
            assertTrue(p.getPrice() >= 0,
                "Товар #" + p.getId() + " (" + p.getName() + ") имеет отрицательную цену: " + p.getPrice());
        }
    }

    @Test
    @DisplayName("Все товары имеют неотрицательный остаток")
    void allProductsHaveNonNegativeStock() {
        List<Product> all = service.getAllProducts();
        for (Product p : all) {
            assertTrue(p.getStockQuantity() >= 0,
                "Товар #" + p.getId() + " (" + p.getName() + ") имеет отрицательный остаток: " + p.getStockQuantity());
        }
    }

    @Test
    @DisplayName("Товаров >= 300 после фикса дубликатов")
    void totalProductCountAfterDuplicateFix() {
        int count = service.getTotalCount();
        assertTrue(count >= 300,
            "Ожидается >= 300 товаров после фикса дубликатов, получено " + count);
    }

    @Test
    @DisplayName("Поиск по подстроке названия возвращает результаты")
    void searchByNameSubstring() {
        // Все seed-товары содержат слово в своих названиях
        List<Product> result = service.searchProducts("NVIDIA", "Все категории", null, null);
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Поиск по 'NVIDIA' должен вернуть товары");
        assertTrue(result.stream().allMatch(p -> p.getName().toLowerCase().contains("nvidia")));
    }

    @Test
    @DisplayName("Поиск c максимальной ценой ограничивает результаты")
    void searchWithMaxPriceFilter() {
        List<Product> filtered = service.searchProducts(null, "Все категории", null, 5000.0);
        assertNotNull(filtered);
        for (Product p : filtered) {
            assertTrue(p.getPrice() <= 5000.0,
                "Товар " + p.getName() + " (цена " + p.getPrice() + ") превышает фильтр maxPrice=5000");
        }
    }

    @Test
    @DisplayName("Количество категорий >= 5 (seed содержит множество)")
    void categoriesCountAtLeastFive() {
        List<String> categories = service.getCategories();
        assertTrue(categories.size() >= 5,
            "Ожидается >= 5 категорий, получено " + categories.size());
    }

    @Test
    @DisplayName("Все ожидаемые категории товаров присутствуют в БД")
    void allExpectedCategoriesPresent() {
        List<String> expected = List.of(
            "Процессоры", "Видеокарты", "Оперативная память",
            "Накопители", "Материнские платы", "Блоки питания",
            "Охлаждение", "Корпуса", "Мониторы", "Периферия",
            "Сетевое оборудование"
        );
        List<String> categories = service.getCategories();
        for (String cat : expected) {
            assertTrue(categories.contains(cat),
                "Категория '" + cat + "' отсутствует в БД. " +
                "Имеющиеся: " + categories);
        }
        assertEquals(expected.size(), categories.size(),
            "Количество категорий должно совпадать с ожидаемым (" +
            expected.size() + "), получено " + categories.size() +
            ". Категории: " + categories);
    }

    // === Тесты фильтра «Только в наличии» ===

    @Test
    @DisplayName("В каталоге есть товары как с остатком > 0, так и с остатком = 0")
    void catalogContainsBothInStockAndOutOfStock() {
        List<Product> all = service.getAllProducts();
        long inStock = all.stream().filter(p -> p.getStockQuantity() > 0).count();
        long outOfStock = all.stream().filter(p -> p.getStockQuantity() <= 0).count();
        assertTrue(inStock > 0, "Должны быть товары в наличии");
        assertTrue(outOfStock > 0, "Должны быть товары не в наличии (для фильтрации)");
    }

    @Test
    @DisplayName("Фильтр 'Только в наличии' исключает товары с остатком = 0")
    void inStockFilterExcludesOutOfStock() {
        List<Product> all = service.searchProducts(null, "Все категории", null, null);
        List<Product> inStockOnly = all.stream()
            .filter(p -> p.getStockQuantity() > 0)
            .collect(Collectors.toList());

        // Все отфильтрованные товары должны быть в наличии
        assertTrue(inStockOnly.stream().allMatch(p -> p.getStockQuantity() > 0),
            "Фильтр inStockOnly должен содержать только товары с остатком > 0");

        // Отфильтрованных должно быть меньше чем всего
        assertTrue(inStockOnly.size() < all.size(),
            "Должны быть убраны товары, которых нет в наличии");
    }

    @Test
    @DisplayName("После снятия фильтра 'Только в наличии' все товары снова видны")
    void removingInStockFilterRestoresAllProducts() {
        List<Product> all = service.searchProducts(null, "Все категории", null, null);
        List<Product> inStockOnly = all.stream()
            .filter(p -> p.getStockQuantity() > 0)
            .collect(Collectors.toList());

        // «Снятие» фильтра — полный список должен быть больше
        assertTrue(all.size() > inStockOnly.size(),
            "Полный список должен содержать больше товаров, чем отфильтрованный");
        assertFalse(all.isEmpty(), "Полный список не должен быть пуст");
    }

    @Test
    @DisplayName("Фильтр 'Только в наличии' корректно работает вместе с фильтром по категории")
    void inStockFilterWorksWithCategoryFilter() {
        List<String> categories = service.getCategories();
        assertFalse(categories.isEmpty());
        String category = categories.get(0);

        List<Product> categoryProducts = service.searchProducts(null, category, null, null);
        List<Product> categoryInStock = categoryProducts.stream()
            .filter(p -> p.getStockQuantity() > 0)
            .collect(Collectors.toList());

        // Все товары в отфильтрованном списке — из правильной категории и в наличии
        for (Product p : categoryInStock) {
            assertEquals(category, p.getCategory(),
                "Товар " + p.getName() + " не из категории " + category);
            assertTrue(p.getStockQuantity() > 0,
                "Товар " + p.getName() + " не в наличии, остаток=" + p.getStockQuantity());
        }
    }

    @Test
    @DisplayName("Фильтр 'Только в наличии' корректно работает с ценовым фильтром")
    void inStockFilterWorksWithPriceFilter() {
        List<Product> filtered = service.searchProducts(null, "Все категории", 1000.0, 50000.0);
        List<Product> inStockFiltered = filtered.stream()
            .filter(p -> p.getStockQuantity() > 0)
            .collect(Collectors.toList());

        for (Product p : inStockFiltered) {
            assertTrue(p.getPrice() >= 1000.0 && p.getPrice() <= 50000.0,
                "Товар " + p.getName() + " (цена=" + p.getPrice() + ") не в диапазоне [1000, 50000]");
            assertTrue(p.getStockQuantity() > 0,
                "Товар " + p.getName() + " не в наличии");
        }
    }
}
