package com.techhaven.repository;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techhaven.model.CartItem;

/**
 * Тесты для CartRepository — CRUD-операции корзины покупок.
 * Используется seed-пользователь userId=1 и seed-продукты.
 * Перед каждым тестом корзина очищается для изоляции.
 */
class CartRepositoryTest {

    private final CartRepository cartRepo = new CartRepository();
    // Используем ID из seed-данных (существующие пользователь и товары)
    private static final int TEST_USER = 1;
    private static final int PROD_A = 1;
    private static final int PROD_B = 2;

    @BeforeEach
    void setup() {
        cartRepo.clearCart(TEST_USER);
    }

    @AfterEach
    void cleanup() {
        cartRepo.clearCart(TEST_USER);
    }

    // ── findByUserId ────────────────────────────────────────────

    @Test
    @DisplayName("findByUserId возвращает пустой список после очистки корзины")
    void findByUserIdReturnsEmptyAfterClear() {
        List<CartItem> items = cartRepo.findByUserId(TEST_USER);
        assertNotNull(items, "Результат не должен быть null");
        assertTrue(items.isEmpty(), "Корзина должна быть пуста после очистки");
    }

    @Test
    @DisplayName("findByUserId возвращает добавленные товары")
    void findByUserIdReturnsAddedItems() {
        cartRepo.addToCart(TEST_USER, PROD_A, 2);
        List<CartItem> items = cartRepo.findByUserId(TEST_USER);
        assertNotNull(items);
        assertEquals(1, items.size(), "Должен быть 1 товар в корзине");
        assertEquals(PROD_A, items.get(0).getProductId());
        assertEquals(2, items.get(0).getQuantity());
    }

    // ── findByUserAndProduct ────────────────────────────────────

    @Test
    @DisplayName("findByUserAndProduct возвращает null если товара нет в корзине")
    void findByUserAndProductReturnsNullIfNotInCart() {
        CartItem item = cartRepo.findByUserAndProduct(TEST_USER, PROD_A);
        assertNull(item, "Товар не должен быть найден в пустой корзине");
    }

    @Test
    @DisplayName("findByUserAndProduct возвращает товар после добавления")
    void findByUserAndProductReturnsItemAfterAdd() {
        cartRepo.addToCart(TEST_USER, PROD_A, 3);
        CartItem item = cartRepo.findByUserAndProduct(TEST_USER, PROD_A);
        assertNotNull(item, "Товар должен быть найден");
        assertEquals(3, item.getQuantity());
        assertNotNull(item.getProductName(), "Имя товара должно быть заполнено (JOIN)");
        assertTrue(item.getProductPrice() > 0, "Цена товара должна быть > 0");
    }

    // ── addToCart ────────────────────────────────────────────────

    @Test
    @DisplayName("addToCart суммирует количество при дублирующем добавлении")
    void addToCartMergesQuantityForDuplicate() {
        cartRepo.addToCart(TEST_USER, PROD_A, 2);
        cartRepo.addToCart(TEST_USER, PROD_A, 3);
        CartItem item = cartRepo.findByUserAndProduct(TEST_USER, PROD_A);
        assertNotNull(item);
        assertEquals(5, item.getQuantity(), "Количество должно суммироваться: 2 + 3 = 5");
    }

    @Test
    @DisplayName("addToCart для разных товаров создаёт разные записи")
    void addToCartDifferentProductsCreateSeparateEntries() {
        cartRepo.addToCart(TEST_USER, PROD_A, 1);
        cartRepo.addToCart(TEST_USER, PROD_B, 1);
        List<CartItem> items = cartRepo.findByUserId(TEST_USER);
        assertEquals(2, items.size(), "Должно быть 2 разных товара в корзине");
    }

    // ── updateQuantity ──────────────────────────────────────────

    @Test
    @DisplayName("updateQuantity обновляет количество товара")
    void updateQuantityChangesValue() {
        cartRepo.addToCart(TEST_USER, PROD_A, 1);
        CartItem before = cartRepo.findByUserAndProduct(TEST_USER, PROD_A);
        assertNotNull(before);
        cartRepo.updateQuantity(before.getId(), 10);
        CartItem after = cartRepo.findByUserAndProduct(TEST_USER, PROD_A);
        assertNotNull(after);
        assertEquals(10, after.getQuantity(), "Количество должно обновиться до 10");
    }

    // ── remove ──────────────────────────────────────────────────

    @Test
    @DisplayName("remove удаляет конкретный товар из корзины")
    void removeDeletesSpecificItem() {
        cartRepo.addToCart(TEST_USER, PROD_A, 1);
        cartRepo.addToCart(TEST_USER, PROD_B, 1);
        CartItem toRemove = cartRepo.findByUserAndProduct(TEST_USER, PROD_A);
        assertNotNull(toRemove);
        cartRepo.remove(toRemove.getId());

        assertNull(cartRepo.findByUserAndProduct(TEST_USER, PROD_A), "Товар A должен быть удалён");
        assertNotNull(cartRepo.findByUserAndProduct(TEST_USER, PROD_B), "Товар B должен остаться");
    }

    // ── clearCart ────────────────────────────────────────────────

    @Test
    @DisplayName("clearCart очищает корзину пользователя полностью")
    void clearCartRemovesAllItems() {
        cartRepo.addToCart(TEST_USER, PROD_A, 1);
        cartRepo.addToCart(TEST_USER, PROD_B, 2);
        cartRepo.clearCart(TEST_USER);
        List<CartItem> items = cartRepo.findByUserId(TEST_USER);
        assertTrue(items.isEmpty(), "Корзина должна быть пустой после очистки");
    }

    // ── getCartCount ────────────────────────────────────────────

    @Test
    @DisplayName("getCartCount возвращает 0 для пустой корзины")
    void getCartCountReturnsZeroForEmptyCart() {
        assertEquals(0, cartRepo.getCartCount(TEST_USER));
    }

    @Test
    @DisplayName("getCartCount возвращает суммарное количество товаров")
    void getCartCountReturnsTotalQuantity() {
        cartRepo.addToCart(TEST_USER, PROD_A, 3);
        cartRepo.addToCart(TEST_USER, PROD_B, 5);
        assertEquals(8, cartRepo.getCartCount(TEST_USER),
            "Суммарное количество: 3 + 5 = 8");
    }
}
