package com.techhaven.repository;

import java.util.List;

import com.techhaven.model.CartItem;

/**
 * Интерфейс репозитория корзины покупок.
 * Определяет контракт для хранения и управления
 * товарами в корзине пользователя.
 */
public interface ICartRepository {
    /** Получить все позиции корзины пользователя. */
    List<CartItem> findByUserId(int userId);

    /** Найти конкретную позицию по паре пользователь + товар. */
    CartItem findByUserAndProduct(int userId, int productId);

    /** Добавить товар в корзину (или увеличить количество). */
    void addToCart(int userId, int productId, int quantity);

    /** Обновить количество товара в позиции корзины. */
    void updateQuantity(int cartId, int quantity);

    /** Удалить позицию из корзины по ID записи. */
    void remove(int cartId);

    /** Полностью очистить корзину пользователя. */
    void clearCart(int userId);

    /** Получить суммарное количество товаров в корзине. */
    int getCartCount(int userId);
}
