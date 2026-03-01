package com.techhaven.service;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.techhaven.config.SessionManager;
import com.techhaven.model.Favorite;
import com.techhaven.model.User;

class FavoriteServiceTest {

    private final FavoriteService favoriteService = new FavoriteService();

    @BeforeEach
    void loginTestUser() {
        // Use seed user id=1 (exists in DB)
        User user = new User("FavTestUser", "favtest@digitalhub.local", "+70000000002", "hash", "USER");
        user.setId(1);
        SessionManager.getInstance().login(user);
    }

    @AfterEach
    void logout() {
        SessionManager.getInstance().logout();
    }

    @Test
    void getFavoritesReturnsNonNull() {
        List<Favorite> favs = favoriteService.getFavorites();
        assertNotNull(favs);
    }

    @Test
    void getFavoriteCountNonNegative() {
        int count = favoriteService.getFavoriteCount();
        assertTrue(count >= 0);
    }

    @Test
    void isFavoriteReturnsFalseForNonExistent() {
        assertFalse(favoriteService.isFavorite(-999));
    }

    @Test
    void addAndRemoveFavorite() {
        ProductService ps = new ProductService();
        var products = ps.getAllProducts();
        assertFalse(products.isEmpty());
        int productId = products.get(0).getId();

        // Clean state
        favoriteService.removeFavorite(productId);
        assertFalse(favoriteService.isFavorite(productId));

        // Add
        favoriteService.addFavorite(productId);
        assertTrue(favoriteService.isFavorite(productId));

        // Remove
        favoriteService.removeFavorite(productId);
        assertFalse(favoriteService.isFavorite(productId));
    }
}
