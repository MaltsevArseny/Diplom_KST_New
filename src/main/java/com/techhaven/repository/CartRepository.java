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
import com.techhaven.model.CartItem;

/**
 * Реализация доступа к таблице Cart в SQLite.
 * Управляет элементами корзины: добавление, обновление количества, удаление.
 * При добавлении дублирующего товара автоматически суммирует количество.
 */
public class CartRepository implements ICartRepository {
    private static final Logger LOGGER = Logger.getLogger(CartRepository.class.getName());
    private final DatabaseManager db = DatabaseManager.getInstance();

    @Override
    public List<CartItem> findByUserId(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = """
                SELECT c.*, p.name AS product_name, p.price AS product_price,
                       p.stock_quantity, cat.name AS category
                FROM Cart c JOIN Products p ON c.product_id = p.id
                JOIN Categories cat ON p.category_id = cat.id
                WHERE c.user_id = ? ORDER BY c.created_at DESC""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getInt("id"));
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setProductName(rs.getString("product_name"));
                item.setProductPrice(rs.getDouble("product_price"));
                item.setStockQuantity(rs.getInt("stock_quantity"));
                item.setCategory(rs.getString("category"));
                items.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения корзины", e);
        }
        return items;
    }

    @Override
    public CartItem findByUserAndProduct(int userId, int productId) {
        String sql = """
                SELECT c.*, p.name AS product_name, p.price AS product_price,
                       p.stock_quantity, cat.name AS category
                FROM Cart c JOIN Products p ON c.product_id = p.id
                JOIN Categories cat ON p.category_id = cat.id
                WHERE c.user_id = ? AND c.product_id = ?""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getInt("id"));
                item.setUserId(rs.getInt("user_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setProductName(rs.getString("product_name"));
                item.setProductPrice(rs.getDouble("product_price"));
                item.setStockQuantity(rs.getInt("stock_quantity"));
                item.setCategory(rs.getString("category"));
                return item;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка поиска в корзине", e);
        }
        return null;
    }

    @Override
    public void addToCart(int userId, int productId, int quantity) {
        CartItem existing = findByUserAndProduct(userId, productId);
        if (existing != null) {
            updateQuantity(existing.getId(), existing.getQuantity() + quantity);
        } else {
            String sql = """
                    INSERT INTO Cart (user_id, product_id, quantity, created_at, updated_at)
                    VALUES (?, ?, ?, datetime('now','localtime'), datetime('now','localtime'))""";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, productId);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Ошибка добавления в корзину", e);
            }
        }
    }

    @Override
    public void updateQuantity(int cartId, int quantity) {
        String sql = "UPDATE Cart SET quantity = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, cartId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления количества", e);
        }
    }

    @Override
    public void remove(int cartId) {
        String sql = "DELETE FROM Cart WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка удаления из корзины", e);
        }
    }

    @Override
    public void clearCart(int userId) {
        String sql = "DELETE FROM Cart WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка очистки корзины", e);
        }
    }

    @Override
    public int getCartCount(int userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM Cart WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка подсчёта корзины", e);
        }
        return 0;
    }
}
