package com.techhaven.service;

import java.util.List;

import com.techhaven.model.Product;
import com.techhaven.repository.ProductRepository;

/**
 * Сервис управления каталогом товаров.
 * Предоставляет бизнес-логику поверх {@link ProductRepository}:
 * CRUD-операции, поиск, фильтрация и пагинация.
 */
public class ProductService {
    private final ProductRepository productRepo = new ProductRepository();

    /** Получить все товары без фильтрации. */
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    /** Поиск товаров с фильтрацией по запросу, категории и диапазону цен. */
    public List<Product> searchProducts(String query, String category, Double minPrice, Double maxPrice) {
        return productRepo.search(query, category, minPrice, maxPrice);
    }

    /** Найти товар по идентификатору. */
    public Product getProductById(int id) {
        return productRepo.findById(id);
    }

    /** Получить список всех уникальных категорий. */
    public List<String> getCategories() {
        return productRepo.findAllCategories();
    }

    /** Создать новый товар (admin). */
    public Product createProduct(Product product) {
        return productRepo.create(product);
    }

    /** Обновить существующий товар (admin). */
    public boolean updateProduct(Product product) {
        return productRepo.update(product);
    }

    /** Удалить товар по ID (admin). */
    public void deleteProduct(int id) {
        productRepo.delete(id);
    }

    /**
     * Пагинация: получить страницу товаров.
     * @param page номер страницы (начиная с 0)
     */
    public java.util.List<com.techhaven.model.Product> getProductsPaged(int page) {
        int pageSize = com.techhaven.config.AppConfig.CATALOG_PAGE_SIZE;
        return productRepo.findPaged(page * pageSize, pageSize);
    }

    /**
     * Общее количество товаров (для расчёта кол-ва страниц).
     */
    public int getTotalCount() {
        return productRepo.getTotalCount();
    }

    /**
     * Общее количество страниц.
     */
    public int getTotalPages() {
        int total = getTotalCount();
        int pageSize = com.techhaven.config.AppConfig.CATALOG_PAGE_SIZE;
        return (total + pageSize - 1) / pageSize;
    }
}
