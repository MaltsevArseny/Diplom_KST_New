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
     * Валидирует поля товара перед сохранением.
     *
     * <p>Проверяет: непустое имя, цена ≥ 0, остаток ≥ 0, заполненная категория,
     * существование категории в справочнике, отсутствие дубля имени
     * (для другого товара). При смене существующего товара передавайте его id
     * в {@code excludeId}, чтобы исключить совпадение с самим собой;
     * для нового товара — передайте 0 или отрицательное число.</p>
     *
     * @return null если всё корректно, иначе строка ошибки для UI
     */
    public String validate(Product product, int excludeId) {
        if (product == null) return "Не передан товар";
        String name = product.getName();
        if (name == null || name.trim().isEmpty()) return "Название товара обязательно";
        if (product.getPrice() < 0) return "Цена не может быть отрицательной";
        if (product.getStockQuantity() < 0) return "Остаток на складе не может быть отрицательным";
        String category = product.getCategory();
        if (category == null || category.trim().isEmpty()) return "Категория обязательна";
        List<String> validCats = productRepo.findAllCategories();
        if (!validCats.contains(category)) {
            return "Категория «" + category + "» не существует в справочнике";
        }
        // Дубль имени: ищем по списку всех товаров, исключая редактируемый
        for (Product p : productRepo.findAll()) {
            if (p.getId() == excludeId) continue;
            if (name.trim().equalsIgnoreCase(p.getName() != null ? p.getName().trim() : null)) {
                return "Товар с названием «" + name.trim() + "» уже существует";
            }
        }
        return null;
    }

    /**
     * Создать новый товар (admin). Перед записью валидирует поля.
     * Инвалидирует кэш категорий на случай добавления товара в новую категорию.
     *
     * @throws SecurityException если вызывающий не имеет роли ADMIN
     * @throws IllegalArgumentException если валидация не пройдена
     */
    public Product createProduct(Product product) {
        com.techhaven.config.SessionManager.getInstance().requireAdmin();
        String err = validate(product, 0);
        if (err != null) throw new IllegalArgumentException(err);
        Product created = productRepo.create(product);
        ProductCache.getInstance().invalidate();
        return created;
    }

    /**
     * Обновить существующий товар (admin). Перед записью валидирует поля.
     * Инвалидирует кэш категорий на случай смены категории товара.
     *
     * @throws SecurityException если вызывающий не имеет роли ADMIN
     * @throws IllegalArgumentException если валидация не пройдена
     */
    public boolean updateProduct(Product product) {
        com.techhaven.config.SessionManager.getInstance().requireAdmin();
        String err = validate(product, product.getId());
        if (err != null) throw new IllegalArgumentException(err);
        boolean result = productRepo.update(product);
        if (result) {
            ProductCache.getInstance().invalidate();
        }
        return result;
    }

    /**
     * Удалить товар по ID (admin).
     * Инвалидирует кэш категорий.
     *
     * @throws SecurityException если вызывающий не имеет роли ADMIN
     */
    public void deleteProduct(int id) {
        com.techhaven.config.SessionManager.getInstance().requireAdmin();
        productRepo.delete(id);
        ProductCache.getInstance().invalidate();
    }
}
