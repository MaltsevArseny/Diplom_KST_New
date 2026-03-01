package com.techhaven.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.techhaven.config.DatabaseManager;
import com.techhaven.model.Favorite;

/**
 * Реализация доступа к таблице Favorites в SQLite.
 * Управляет избранными товарами пользователя. Гарантирует идемпотентность при добавлении.
 */
public class FavoriteRepository implements IFavoriteRepository {
    private static final Logger LOGGER = Logger.getLogger(FavoriteRepository.class.getName());
    private final DatabaseManager db = DatabaseManager.getInstance();

    @Override
    public List<Favorite> findByUserId(int userId) {
        List<Favorite> favorites = new ArrayList<>();
        String sql = """
                SELECT f.*, p.name AS product_name, p.price AS product_price,
                       cat.name AS category, p.stock_quantity, p.description, p.specifications
                FROM Favorites f JOIN Products p ON f.product_id = p.id
                JOIN Categories cat ON p.category_id = cat.id
                WHERE f.user_id = ? ORDER BY f.created_at DESC""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Favorite fav = new Favorite();
                fav.setId(rs.getInt("id"));
                fav.setUserId(rs.getInt("user_id"));
                fav.setProductId(rs.getInt("product_id"));
                fav.setProductName(rs.getString("product_name"));
                fav.setProductPrice(rs.getDouble("product_price"));
                fav.setCategory(rs.getString("category"));
                fav.setStockQuantity(rs.getInt("stock_quantity"));
                fav.setDescription(rs.getString("description"));
                fav.setSpecifications(rs.getString("specifications"));
                favorites.add(fav);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения избранного", e);
        }
        return favorites;
    }

    @Override
    public boolean isFavorite(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM Favorites WHERE user_id = ? AND product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка проверки избранного", e);
        }
        return false;
    }

    @Override
    public void add(int userId, int productId) {
        if (isFavorite(userId, productId)) return;
        String sql = "INSERT INTO Favorites (user_id, product_id, created_at) VALUES (?, ?, datetime('now','localtime'))";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка добавления в избранное", e);
        }
    }

    @Override
    public void remove(int userId, int productId) {
        String sql = "DELETE FROM Favorites WHERE user_id = ? AND product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка удаления из избранного", e);
        }
    }

    @Override
    public int getFavoriteCount(int userId) {
        String sql = "SELECT COUNT(*) FROM Favorites WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка подсчета избранного", e);
        }
        return 0;
    }
}
