package com.techhaven.repository;

import java.util.List;

import com.techhaven.model.Product;

/**
 * Интерфейс репозитория товаров.
 * Определяет контракт для доступа к данным каталога продуктов.
 *
 * <p>Поддерживает полный CRUD, постраничную навигацию,
 * поиск с фильтрацией по категории/цене и управление складскими остатками.</p>
 */
public interface IProductRepository {
    /** Получить все товары. */
    List<Product> findAll();

    /** Постраничная выборка товаров. */
    List<Product> findPaged(int offset, int limit);

    /** Общее количество товаров (для расчёта страниц). */
    int getTotalCount();

    /** Полнотекстовый поиск с фильтрацией по категории и диапазону цен. */
    List<Product> search(String query, String category, Double minPrice, Double maxPrice);

    /** Найти товар по ID. */
    Product findById(int id);

    /** Получить список всех уникальных категорий. */
    List<String> findAllCategories();

    /** Создать новый товар. */
    Product create(Product product);

    /** Обновить существующий товар. */
    boolean update(Product product);

    /** Удалить товар по ID. */
    boolean delete(int id);

    /** Уменьшить складской остаток на указанное количество. */
    void updateStock(int productId, int quantity);
}
