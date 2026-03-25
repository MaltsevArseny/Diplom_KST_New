package com.techhaven.service;

import java.util.List;

import com.techhaven.model.Product;
import com.techhaven.repository.ProductRepository;

/**
 * Сервис управления каталогом товаров.
 *
 * <p>Предоставляет бизнес-логику поверх {@link ProductRepository}:
 * CRUD-операции, поиск, фильтрация и пагинация.</p>
 *
 * <p><b>Кэширование категорий:</b> список категорий статичен и запрашивается
 * очень часто (открытие каталога, фильтры). Чтобы снизить latency,
 * {@link #getCategories()} делегирует запрос в {@link ProductCache}.
 * При изменении каталога (create/update/delete) кэш инвалидируется.</p>
 */
public class ProductService {

    private final ProductRepository productRepo = new ProductRepository();

    /**
     * Флаг: использовать ли {@link ProductCache}.
     * {@code false} применяется в {@link ProductCache.ProductCacheHolder},
     * чтобы разорвать циклическую зависимость ProductService ↔ ProductCache.
     */
    private final boolean useCache;

    /** Конструктор по умолчанию — с кэшированием категорий. */
    public ProductService() {
        this.useCache = true;
    }

    /**
     * Конструктор для внутреннего использования {@link ProductCache}.
     *
     * @param useCache {@code false} — отключает кэш, обращается к репозиторию напрямую
     */
    public ProductService(boolean useCache) {
        this.useCache = useCache;
    }

    // ── Чтение ──────────────────────────────────────────────────────────────

    /** Получить все товары без фильтрации. */
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    /** Поиск товаров с фильтрацией по запросу, категории и диапазону цен. */
    public List<Product> searchProducts(String query, String category,
                                        Double minPrice, Double maxPrice) {
        return productRepo.search(query, category, minPrice, maxPrice);
    }

    /** Найти товар по идентификатору. */
    public Product getProductById(int id) {
        return productRepo.findById(id);
    }

    /**
     * Получить список всех уникальных категорий.
     *
     * <p>При {@code useCache = true} результат возвращается из {@link ProductCache}
     * (запрос к БД только при первом вызове или после инвалидации).</p>
     */
    public List<String> getCategories() {
        if (useCache) {
            return ProductCache.getInstance().getCategories();
        }
        return productRepo.findAllCategories();
    }

    // ── Пагинация ────────────────────────────────────────────────────────────

    /**
     * Пагинация: получить страницу товаров.
     *
     * @param page номер страницы (начиная с 0)
     */
    public List<Product> getProductsPaged(int page) {
        int pageSize = com.techhaven.config.AppConfig.CATALOG_PAGE_SIZE;
        return productRepo.findPaged(page * pageSize, pageSize);
    }

    /** Общее количество товаров (для расчёта кол-ва страниц). */
    public int getTotalCount() {
        return productRepo.getTotalCount();
    }

    /** Общее количество страниц. */
    public int getTotalPages() {
        int total = getTotalCount();
        int pageSize = com.techhaven.config.AppConfig.CATALOG_PAGE_SIZE;
        return (total + pageSize - 1) / pageSize;
    }

    // ── Запись (CRUD) ────────────────────────────────────────────────────────

    /**
     * Создать новый товар (admin).
     * Инвалидирует кэш категорий на случай добавления товара в новую категорию.
     */
    public Product createProduct(Product product) {
        Product created = productRepo.create(product);
        ProductCache.getInstance().invalidate();
        return created;
    }

    /**
     * Обновить существующий товар (admin).
     * Инвалидирует кэш категорий на случай смены категории товара.
     */
    public boolean updateProduct(Product product) {
        boolean result = productRepo.update(product);
        if (result) {
            ProductCache.getInstance().invalidate();
        }
        return result;
    }

    /**
     * Удалить товар по ID (admin).
     * Инвалидирует кэш категорий.
     */
    public void deleteProduct(int id) {
        productRepo.delete(id);
        ProductCache.getInstance().invalidate();
    }
}
