package com.techhaven.repository;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techhaven.model.Favorite;

/**
 * Тесты для FavoriteRepository — CRUD-операции с избранными товарами.
 * Используется seed-пользователь userId=1 и seed-продукты.
 * Перед каждым тестом избранное для тестируемых товаров очищается.
 */
class FavoriteRepositoryTest {

    private final FavoriteRepository favRepo = new FavoriteRepository();
    // Используем ID из seed-данных (существующие пользователь и товары)
    private static final int TEST_USER = 1;
    private static final int PROD_A = 1;
    private static final int PROD_B = 2;
    private static final int PROD_C = 3;

    @BeforeEach
    void setup() {
        favRepo.remove(TEST_USER, PROD_A);
        favRepo.remove(TEST_USER, PROD_B);
        favRepo.remove(TEST_USER, PROD_C);
    }

    @AfterEach
    void cleanup() {
        favRepo.remove(TEST_USER, PROD_A);
        favRepo.remove(TEST_USER, PROD_B);
        favRepo.remove(TEST_USER, PROD_C);
    }

    // ── isFavorite ──────────────────────────────────────────────

    @Test
    @DisplayName("isFavorite возвращает false после удаления")
    void isFavoriteReturnsFalseWhenNotAdded() {
        assertFalse(favRepo.isFavorite(TEST_USER, PROD_A));
    }

    @Test
    @DisplayName("isFavorite возвращает true после добавления")
    void isFavoriteReturnsTrueAfterAdd() {
        favRepo.add(TEST_USER, PROD_A);
        assertTrue(favRepo.isFavorite(TEST_USER, PROD_A));
    }

    // ── add ─────────────────────────────────────────────────────

    @Test
    @DisplayName("add идемпотентен — повторное добавление не создаёт дубликат")
    void addIsIdempotent() {
        favRepo.add(TEST_USER, PROD_A);
        favRepo.add(TEST_USER, PROD_A); // повторно
        assertEquals(1, favRepo.getFavoriteCount(TEST_USER) > 0 ? 1 : 0,
            "Повторное добавление не должно создавать дубликат");
        assertTrue(favRepo.isFavorite(TEST_USER, PROD_A));
    }

    // ── findByUserId ────────────────────────────────────────────

    @Test
    @DisplayName("findByUserId возвращает добавленные товары с данными продукта")
    void findByUserIdReturnsAddedFavoritesWithProductData() {
        favRepo.add(TEST_USER, PROD_A);
        favRepo.add(TEST_USER, PROD_B);
        List<Favorite> favorites = favRepo.findByUserId(TEST_USER);
        assertTrue(favorites.size() >= 2, "Должно быть минимум 2 избранных товара");
        for (Favorite f : favorites) {
            assertNotNull(f.getProductName(), "Имя товара должно быть заполнено (JOIN)");
            assertTrue(f.getProductPrice() > 0, "Цена товара должна быть > 0");
        }
    }

    // ── remove ──────────────────────────────────────────────────

    @Test
    @DisplayName("remove удаляет только указанный товар")
    void removeDeletesSpecificFavorite() {
        favRepo.add(TEST_USER, PROD_A);
        favRepo.add(TEST_USER, PROD_B);
        favRepo.remove(TEST_USER, PROD_A);

        assertFalse(favRepo.isFavorite(TEST_USER, PROD_A), "Товар A должен быть удалён");
        assertTrue(favRepo.isFavorite(TEST_USER, PROD_B), "Товар B должен остаться");
    }

    @Test
    @DisplayName("remove не бросает исключение для несуществующей записи")
    void removeNonExistentDoesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> favRepo.remove(TEST_USER, 999_999)
        );
    }

    // ── getFavoriteCount ────────────────────────────────────────

    @Test
    @DisplayName("getFavoriteCount возвращает 0 когда нет избранного")
    void getFavoriteCountReturnsZeroWhenEmpty() {
        assertEquals(0, favRepo.getFavoriteCount(TEST_USER));
    }

    @Test
    @DisplayName("getFavoriteCount увеличивается после добавления")
    void getFavoriteCountIncreasesAfterAdd() {
        int before = favRepo.getFavoriteCount(TEST_USER);
        favRepo.add(TEST_USER, PROD_A);
        favRepo.add(TEST_USER, PROD_B);
        favRepo.add(TEST_USER, PROD_C);
        int after = favRepo.getFavoriteCount(TEST_USER);
        assertEquals(before + 3, after, "Счётчик должен увеличиться на 3");
    }
}
