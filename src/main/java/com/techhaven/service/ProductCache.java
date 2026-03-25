package com.techhaven.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * In-memory кэш категорий товаров (Singleton).
 *
 * <p>Список категорий статичен на весь сеанс работы приложения — он загружается
 * из справочника {@code Categories} при первом обращении и более не запрашивается
 * у базы данных. Это снижает latency навигации по каталогу и панели фильтров.</p>
 *
 * <h3>Потокобезопасность</h3>
 * <p>Использует {@link CopyOnWriteArrayList} для безопасного чтения из нескольких
 * потоков (JavaFX Application Thread + фоновые потоки). Запись происходит только
 * при первой инициализации и при явной инвалидации.</p>
 *
 * <h3>Когда сбрасывать кэш</h3>
 * <p>Категории товаров не меняются через UI приложения — список фиксирован в
 * {@code DatabaseManager.seedCategories()}. Метод {@link #invalidate()} предусмотрен
 * для тестов и на случай будущего расширения (добавление новых категорий).</p>
 */
public class ProductCache {

    private static final Logger LOGGER = Logger.getLogger(ProductCache.class.getName());

    /** Singleton-экземпляр; volatile обеспечивает корректную публикацию в многопоточной среде. */
    private static volatile ProductCache instance;

    /**
     * Поставщик данных: вызывается один раз при «cache miss».
     * Позволяет подменить реализацию в тестах без реальной БД.
     */
    private final CategorySupplier supplier;

    /** Хранилище кэшированных категорий; null означает «кэш пуст» (lazy init). */
    private volatile List<String> cachedCategories;

    /** Счётчик обращений к кэшу (для диагностики). */
    private int hitCount;

    /** Счётчик промахов кэша (для диагностики). */
    private int missCount;

    // ── Конструкторы ────────────────────────────────────────────────────────

    /**
     * Конструктор Singleton (production): обращается к репозиторию напрямую,
     * разрывая циклическую зависимость ProductService ↔ ProductCache.
     */
    private ProductCache() {
        final com.techhaven.repository.IProductRepository repo =
                new com.techhaven.repository.ProductRepository();
        this.supplier = repo::findAllCategories;
    }

    /**
     * Конструктор для тестов: позволяет передать произвольный поставщик данных.
     *
     * @param supplier лямбда, возвращающая список категорий
     */
    public ProductCache(CategorySupplier supplier) {
        this.supplier = supplier;
    }

    // ── Singleton ────────────────────────────────────────────────────────────

    /**
     * Возвращает единственный экземпляр кэша.
     * Реализован через идиому «double-checked locking» с корректным volatile.
     */
    public static ProductCache getInstance() {
        ProductCache local = instance;
        if (local == null) {
            synchronized (ProductCache.class) {
                local = instance;
                if (local == null) {
                    instance = local = new ProductCache();
                }
            }
        }
        return local;
    }

    // ── Основной API ─────────────────────────────────────────────────────────

    /**
     * Возвращает список всех категорий товаров.
     *
     * <p>При первом вызове («cache miss») загружает данные через поставщик и сохраняет.
     * При последующих вызовах («cache hit») возвращает сохранённый список без обращения к БД.</p>
     *
     * @return неизменяемый список категорий (никогда не {@code null})
     */
    public List<String> getCategories() {
        List<String> cached = cachedCategories;
        if (cached != null) {
            hitCount++;
            LOGGER.fine("ProductCache HIT");
            return cached;
        }
        synchronized (this) {
            cached = cachedCategories;
            if (cached != null) {
                hitCount++;
                return cached;
            }
            missCount++;
            LOGGER.fine("ProductCache MISS, загрузка из БД");
            List<String> fresh = supplier.load();
            cachedCategories = Collections.unmodifiableList(new CopyOnWriteArrayList<>(fresh));
            LOGGER.log(Level.FINE, "ProductCache загружено {0} категорий", fresh.size());
            return cachedCategories;
        }
    }

    /**
     * Сбрасывает кэш.
     * Следующий вызов {@link #getCategories()} снова обратится к БД.
     */
    public synchronized void invalidate() {
        cachedCategories = null;
        LOGGER.fine("ProductCache инвалидирован");
    }

    /**
     * Проверяет, заполнен ли кэш (для тестов и диагностики).
     *
     * @return {@code true}, если данные уже загружены в память
     */
    public boolean isCached() {
        return cachedCategories != null;
    }

    /** @return количество cache hit с момента последней инвалидации */
    public int getHitCount() { return hitCount; }

    /** @return количество cache miss с момента последней инвалидации */
    public int getMissCount() { return missCount; }

    /**
     * Сбрасывает Singleton-экземпляр.
     * <b>Использовать только в тестах!</b>
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    // ── Вспомогательный функциональный интерфейс ─────────────────────────────

    /**
     * Функциональный интерфейс для поставщика категорий.
     * Позволяет подменять источник данных в тестах.
     */
    @FunctionalInterface
    public interface CategorySupplier {
        /** Загружает и возвращает список категорий. */
        List<String> load();
    }
}
