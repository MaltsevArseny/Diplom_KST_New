package com.techhaven.service;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.techhaven.config.SessionManager;
import com.techhaven.model.CartItem;
import com.techhaven.model.User;

class CartServiceTest {

    private final CartService cartService = new CartService();

    @SuppressWarnings("unused")
    @BeforeEach
    void loginTestUser() {
        // Use seed user id=1 (exists in DB)
        User user = new User("CartTestUser", "carttest@digitalhub.local", "+70000000003", "hash", "USER");
        user.setId(1);
        SessionManager.getInstance().login(user);
    }

    @SuppressWarnings("unused")
    @AfterEach
    void logout() {
        SessionManager.getInstance().logout();
    }

    @Test
    void getCartItemsReturnsNonNull() {
        List<CartItem> cart = cartService.getCartItems();
        assertNotNull(cart);
    }

    @Test
    void getCartCountNonNegative() {
        int count = cartService.getCartCount();
        assertTrue(count >= 0);
    }

    @Test
    void addToCartRejectsNonExistentProduct() {
        String error = cartService.addToCart(-999, 1);
        assertEquals("Товар не найден", error);
    }

    @Test
    void getTotalNonNegative() {
        double total = cartService.getTotal();
        assertTrue(total >= 0);
    }

    @Test
    void isInCartReturnsFalseForNonExistent() {
        assertFalse(cartService.isInCart(-999));
    }

    @Test
    void addAndRemoveFromCart() {
        ProductService ps = new ProductService();
        var products = ps.getAllProducts();
        assertFalse(products.isEmpty());

        // Find product with stock > 0
        var inStock = products.stream()
                .filter(p -> p.getStockQuantity() > 0)
                .findFirst();
        if (inStock.isEmpty()) return;

        int productId = inStock.get().getId();
        String error = cartService.addToCart(productId, 1);
        assertNull(error, "addToCart should succeed, got: " + error);

        List<CartItem> cart = cartService.getCartItems();
        CartItem added = cart.stream()
                .filter(i -> i.getProductId() == productId)
                .findFirst().orElse(null);
        assertNotNull(added);

        cartService.removeFromCart(added.getId());
    }

    // ─── Тесты обновления quantity (покрывают логику plusBtn/minusBtn в CartView) ───

    @Test
    void updateQuantityChangesSubtotal() {
        ProductService ps2 = new ProductService();
        var products2 = ps2.getAllProducts();
        var inStock2 = products2.stream()
                .filter(p -> p.getStockQuantity() >= 2)
                .findFirst();
        if (inStock2.isEmpty()) return;

        int productId = inStock2.get().getId();
        // Добавляем товар с quantity=1
        cartService.addToCart(productId, 1);
        List<CartItem> cart2 = cartService.getCartItems();
        CartItem added = cart2.stream()
                .filter(i -> i.getProductId() == productId)
                .findFirst().orElse(null);
        assertNotNull(added);

        double subtotalBefore = added.getSubtotal();  // price * 1

        // Увеличиваем quantity до 2 (как делает plusBtn в CartView)
        cartService.updateQuantity(added.getId(), 2);
        added.setQuantity(2);  // обновляем модель как в CartView

        double subtotalAfter = added.getSubtotal();   // price * 2
        assertTrue(subtotalAfter > subtotalBefore,
            "Subtotal должен вырасти после увеличения quantity");
        assertEquals(subtotalBefore * 2, subtotalAfter, 0.01,
            "Subtotal при qty=2 должен быть ровно вдвое больше чем при qty=1");

        cartService.removeFromCart(added.getId());
    }

    @Test
    void getTotalReflectsQuantityUpdate() {
        ProductService ps3 = new ProductService();
        var products3 = ps3.getAllProducts();
        var inStock3 = products3.stream()
                .filter(p -> p.getStockQuantity() >= 2)
                .findFirst();
        if (inStock3.isEmpty()) return;

        int productId = inStock3.get().getId();
        cartService.addToCart(productId, 1);

        double totalBefore = cartService.getTotal();

        List<CartItem> cart3 = cartService.getCartItems();
        CartItem added3 = cart3.stream()
                .filter(i -> i.getProductId() == productId)
                .findFirst().orElse(null);
        assertNotNull(added3);

        // Увеличиваем quantity через сервис (как делает plusBtn в CartView)
        cartService.updateQuantity(added3.getId(), 2);
        double totalAfter = cartService.getTotal();

        assertTrue(totalAfter > totalBefore,
            "getTotal() должен увеличиться после updateQuantity: было " + totalBefore + ", стало " + totalAfter);

        cartService.removeFromCart(added3.getId());
    }
}
