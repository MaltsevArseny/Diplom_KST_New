package com.techhaven.service;

import java.util.List;

import com.techhaven.config.EventBus;
import com.techhaven.config.SessionManager;
import com.techhaven.model.CartItem;
import com.techhaven.model.Product;
import com.techhaven.repository.CartRepository;
import com.techhaven.repository.ProductRepository;

/**
 * Сервис управления корзиной покупок.
 * Публикует события через {@link EventBus} при изменениях:
 * <ul>
 *   <li>{@code cart.updated} — товар добавлен/удалён/обновлён</li>
 * </ul>
 */
public class CartService {
    private final CartRepository cartRepo = new CartRepository();
    private final ProductRepository productRepo = new ProductRepository();

    /** Получить содержимое корзины текущего пользователя. */
    public List<CartItem> getCartItems() {
        return cartRepo.findByUserId(SessionManager.getInstance().getCurrentUserId());
    }

    /**
     * Добавить товар в корзину.
     * @return {@code null} при успехе, сообщение об ошибке иначе
     */
    public String addToCart(int productId, int quantity) {
        Product product = productRepo.findById(productId);
        if (product == null) return "Товар не найден";
        if (product.getStockQuantity() < quantity) {
            return "Недостаточно на складе (доступно: " + product.getStockQuantity() + ")";
        }
        cartRepo.addToCart(SessionManager.getInstance().getCurrentUserId(), productId, quantity);
        EventBus.publish("cart.updated", null);
        return null;
    }

    /** Обновить количество товара в корзине. */
    public void updateQuantity(int cartId, int quantity) {
        if (quantity <= 0) {
            cartRepo.remove(cartId);
        } else {
            cartRepo.updateQuantity(cartId, quantity);
        }
        EventBus.publish("cart.updated", null);
    }

    /** Удалить товар из корзины. */
    public void removeFromCart(int cartId) {
        cartRepo.remove(cartId);
        EventBus.publish("cart.updated", null);
    }

    /** Рассчитать общую стоимость корзины. */
    public double getTotal() {
        return getCartItems().stream()
            .mapToDouble(CartItem::getSubtotal)
            .sum();
    }

    /** Количество товаров в корзине. */
    public int getCartCount() {
        return cartRepo.getCartCount(SessionManager.getInstance().getCurrentUserId());
    }

    /** Проверить, находится ли товар в корзине. */
    public boolean isInCart(int productId) {
        return getCartItems().stream()
            .anyMatch(item -> item.getProductId() == productId);
    }
}
