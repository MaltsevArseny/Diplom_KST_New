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

    @BeforeEach
    void loginTestUser() {
        // Use seed user id=1 (exists in DB)
        User user = new User("CartTestUser", "carttest@digitalhub.local", "+70000000003", "hash", "USER");
        user.setId(1);
        SessionManager.getInstance().login(user);
    }

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
}
