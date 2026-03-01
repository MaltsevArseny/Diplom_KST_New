package com.techhaven.repository;

import java.util.List;

import com.techhaven.model.Favorite;

/**
 * Интерфейс репозитория избранного.
 * Определяет контракт для управления списком
 * избранных товаров пользователя.
 */
public interface IFavoriteRepository {
    /** Получить все избранные товары пользователя. */
    List<Favorite> findByUserId(int userId);

    /** Проверить, находится ли товар в избранном. */
    boolean isFavorite(int userId, int productId);

    /** Добавить товар в избранное. */
    void add(int userId, int productId);

    /** Удалить товар из избранного. */
    void remove(int userId, int productId);

    /** Получить количество товаров в избранном. */
    int getFavoriteCount(int userId);
}
