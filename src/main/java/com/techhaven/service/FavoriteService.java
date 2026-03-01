package com.techhaven.service;

import java.util.List;

import com.techhaven.config.EventBus;
import com.techhaven.config.SessionManager;
import com.techhaven.model.Favorite;
import com.techhaven.repository.FavoriteRepository;

/**
 * Сервис управления списком избранного.
 * Публикует события через {@link EventBus} при изменениях:
 * <ul>
 *   <li>{@code favorites.updated} — товар добавлен/удалён из избранного</li>
 * </ul>
 */
public class FavoriteService {
    private final FavoriteRepository favRepo = new FavoriteRepository();

    /** Получить список избранного текущего пользователя. */
    public List<Favorite> getFavorites() {
        return favRepo.findByUserId(SessionManager.getInstance().getCurrentUserId());
    }

    /** Переключить состояние избранного для товара. */
    public void toggleFavorite(int productId) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (favRepo.isFavorite(userId, productId)) {
            favRepo.remove(userId, productId);
        } else {
            favRepo.add(userId, productId);
        }
        EventBus.publish("favorites.updated", null);
    }

    /** Проверить, есть ли товар в избранном. */
    public boolean isFavorite(int productId) {
        return favRepo.isFavorite(SessionManager.getInstance().getCurrentUserId(), productId);
    }

    /** Добавить товар в избранное. */
    public void addFavorite(int productId) {
        favRepo.add(SessionManager.getInstance().getCurrentUserId(), productId);
        EventBus.publish("favorites.updated", null);
    }

    /** Удалить товар из избранного. */
    public void removeFavorite(int productId) {
        favRepo.remove(SessionManager.getInstance().getCurrentUserId(), productId);
        EventBus.publish("favorites.updated", null);
    }

    /** Количество товаров в избранном. */
    public int getFavoriteCount() {
        return favRepo.getFavoriteCount(SessionManager.getInstance().getCurrentUserId());
    }
}
