package com.techhaven.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.techhaven.config.DatabaseManager;
import com.techhaven.model.Product;

/**
 * Реализация доступа к таблице Products в SQLite.
 * Все SELECT-запросы используют JOIN с Categories для получения имени категории.
 * INSERT/UPDATE используют category_id через подзапрос.
 */
public class ProductRepository implements IProductRepository {
    private static final Logger LOGGER = Logger.getLogger(ProductRepository.class.getName());
    private final DatabaseManager db = DatabaseManager.getInstance();

    private static final String SELECT_BASE =
        "SELECT p.*, c.name AS category FROM Products p JOIN Categories c ON p.category_id = c.id";

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY p.name";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) products.add(mapProduct(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения товаров", e);
        }
        return products;
    }

    @Override
    public List<Product> findPaged(int offset, int limit) {
        List<Product> products = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY p.name LIMIT ? OFFSET ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) products.add(mapProduct(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка пагинации товаров", e);
        }
        return products;
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM Products";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка подсчёта товаров", e);
        }
        return 0;
    }

    @Override
    public List<Product> search(String query, String category, Double minPrice, Double maxPrice) {
        List<Product> products = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY p.name";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            String q = (query != null) ? query.trim().toLowerCase() : null;
            boolean hasQuery = q != null && !q.isEmpty();

            while (rs.next()) {
                Product p = mapProduct(rs);

                if (category != null && !category.trim().isEmpty() && !"Все категории".equals(category)) {
                    if (!category.equals(p.getCategory())) continue;
                }
                if (minPrice != null && p.getPrice() < minPrice) continue;
                if (maxPrice != null && p.getPrice() > maxPrice) continue;
                if (hasQuery) {
                    String name = p.getName() != null ? p.getName().toLowerCase() : "";
                    String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
                    String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
                    if (!(name.contains(q) || desc.contains(q) || cat.contains(q))) continue;
                }
                products.add(p);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка поиска товаров", e);
        }
        return products;
    }

    @Override
    public Product findById(int id) {
        String sql = SELECT_BASE + " WHERE p.id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapProduct(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка поиска товара по id", e);
        }
        return null;
    }

    @Override
    public List<String> findAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM Categories ORDER BY name";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) categories.add(rs.getString("name"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения категорий", e);
        }
        return categories;
    }

    @Override
    public Product create(Product product) {
        String sql = """
                INSERT INTO Products (name, description, category_id, price, stock_quantity,
                                     specifications, image_path, created_at, updated_at)
                VALUES (?, ?, (SELECT id FROM Categories WHERE name = ?), ?, ?, ?, ?, datetime('now','localtime'), datetime('now','localtime'))""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getCategory());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setString(6, product.getSpecifications());
            ps.setString(7, product.getImagePath());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) product.setId(keys.getInt(1));
            return product;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка создания товара", e);
        }
        return null;
    }

    @Override
    public boolean update(Product product) {
        String sql = """
                UPDATE Products SET name=?, description=?,
                       category_id=(SELECT id FROM Categories WHERE name=?),
                       price=?, stock_quantity=?, specifications=?, image_path=?,
                       updated_at=datetime('now','localtime')
                WHERE id=?""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setString(3, product.getCategory());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setString(6, product.getSpecifications());
            ps.setString(7, product.getImagePath());
            ps.setInt(8, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления товара", e);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Products WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка удаления товара", e);
        }
        return false;
    }

    @Override
    public void updateStock(int productId, int quantity) {
        String sql = """
                UPDATE Products SET stock_quantity = stock_quantity - ?,
                       updated_at = datetime('now','localtime')
                WHERE id = ?""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления остатков", e);
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getDouble("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setSpecifications(rs.getString("specifications"));
        p.setImagePath(rs.getString("image_path"));
        return p;
    }
}
