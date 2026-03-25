package com.techhaven.service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link ProductCache}.
 *
 * Используют конструктор {@code ProductCache(CategorySupplier)} и метод
 * {@code ProductCache.resetInstance()} — без реального подключения к БД.
 */
class ProductCacheTest {

    private static final List<String> SAMPLE_CATEGORIES =
            List.of("Процессоры", "Видеокарты", "Оперативная память");

    /** Счётчик вызовов поставщика (для проверки cache hit). */
    private int supplierCallCount;

    /** Тестовый экземпляр кэша с подменённым поставщиком. */
    private ProductCache cache;

    @BeforeEach
    void setUp() {
        supplierCallCount = 0;
        // Создаём тестовый экземпляр с лямбдой-поставщиком (не Singleton)
        cache = new ProductCache(() -> {
            supplierCallCount++;
            return SAMPLE_CATEGORIES;
        });
    }

    @AfterEach
    void tearDown() {
        // Сбрасываем Singleton, чтобы тесты не влияли друг на друга
        ProductCache.resetInstance();
    }

    // ── Cache Miss ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Первый вызов getCategories() возвращает непустой список (cache miss)")
    void firstCallReturnsList() {
        List<String> result = cache.getCategories();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(SAMPLE_CATEGORIES.size(), result.size());
        assertEquals(1, supplierCallCount, "Поставщик должен быть вызван ровно один раз");
    }

    @Test
    @DisplayName("Первый вызов инициализирует кэш (isCached = true)")
    void firstCallSetsCachedFlag() {
        assertFalse(cache.isCached(), "До первого вызова кэш должен быть пустым");
        cache.getCategories();
        assertTrue(cache.isCached(), "После первого вызова кэш должен быть заполнен");
    }

    @Test
    @DisplayName("Cache miss увеличивает missCount")
    void firstCallIncreasesMissCount() {
        cache.getCategories();
        assertEquals(1, cache.getMissCount());
        assertEquals(0, cache.getHitCount());
    }

    // ── Cache Hit ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Второй вызов getCategories() не обращается к поставщику (cache hit)")
    void secondCallUsesCache() {
        cache.getCategories(); // miss
        cache.getCategories(); // hit
        assertEquals(1, supplierCallCount,
                "Поставщик должен быть вызван ровно один раз за всё время");
    }

    @Test
    @DisplayName("Повторный вызов возвращает тот же объект списка (identity)")
    void repeatedCallReturnsSameReference() {
        List<String> first = cache.getCategories();
        List<String> second = cache.getCategories();
        assertSame(first, second, "Оба вызова должны вернуть один и тот же объект");
    }

    @Test
    @DisplayName("Cache hit увеличивает hitCount")
    void repeatedCallIncreasesHitCount() {
        cache.getCategories(); // miss
        cache.getCategories(); // hit №1
        cache.getCategories(); // hit №2
        assertEquals(2, cache.getHitCount());
        assertEquals(1, cache.getMissCount());
    }

    // ── Invalidate ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("После invalidate() кэш сбрасывается (isCached = false)")
    void invalidateClearsCache() {
        cache.getCategories();
        assertTrue(cache.isCached());
        cache.invalidate();
        assertFalse(cache.isCached(), "После инвалидации кэш должен быть пустым");
    }

    @Test
    @DisplayName("После invalidate() следующий getCategories() снова вызывает поставщика")
    void afterInvalidateSupplierCalledAgain() {
        cache.getCategories(); // miss
        cache.invalidate();
        cache.getCategories(); // miss снова
        assertEquals(2, supplierCallCount,
                "Поставщик должен быть вызван дважды: до и после инвалидации");
    }

    // ── Конкурентный доступ ──────────────────────────────────────────────────

    @Test
    @DisplayName("Конкурентный доступ из нескольких потоков не вызывает исключений")
    void concurrentAccessIsThreadSafe() throws InterruptedException {
        int threadCount = 8;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        Exception[] errors = new Exception[threadCount];

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    List<String> result = cache.getCategories();
                    assertNotNull(result);
                    assertFalse(result.isEmpty());
                } catch (Exception t) {
                    errors[idx] = t;
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // даём команду «старт» всем потокам
        boolean finished = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertTrue(finished, "Все потоки должны завершиться за 5 секунд");
        for (int i = 0; i < threadCount; i++) {
            if (errors[i] != null) {
                throw new AssertionError("Поток " + i + " бросил исключение: " + errors[i].getMessage());
            }
        }
        // Поставщик должен быть вызван ровно один раз, несмотря на гонку
        assertEquals(1, supplierCallCount,
                "Поставщик должен быть вызван ровно один раз при конкурентном доступе");
    }

    // ── Интеграция с ProductService ──────────────────────────────────────────

    @Test
    @DisplayName("ProductService.getCategories() через Singleton-кэш возвращает список категорий из БД")
    void productServiceDelegatestoSingletonCache() {
        // Этот тест использует настоящую БД (Singleton ProductCache)
        ProductService service = new ProductService();
        List<String> categories = service.getCategories();

        assertNotNull(categories);
        assertFalse(categories.isEmpty(), "ProductService должен вернуть список категорий");
        assertTrue(categories.size() >= 11,
                "В БД должно быть 11 категорий, получено: " + categories.size());

        // Второй вызов должен прийти из кэша
        List<String> cached = service.getCategories();
        assertSame(categories, cached, "Повторный вызов должен вернуть тот же объект из кэша");
    }

    @Test
    @DisplayName("getCategories() возвращает неизменяемый список")
    void returnedListIsUnmodifiable() {
        List<String> result = cache.getCategories();
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> result.add("НоваяКатегория"),
                "Список категорий должен быть неизменяемым"
        );
    }

    @Test
    @DisplayName("Список категорий содержит все ожидаемые элементы из поставщика")
    void categoriesContainExpectedValues() {
        List<String> result = cache.getCategories();
        assertTrue(result.containsAll(SAMPLE_CATEGORIES),
                "結果должен содержать все категории из поставщика");
    }
}
