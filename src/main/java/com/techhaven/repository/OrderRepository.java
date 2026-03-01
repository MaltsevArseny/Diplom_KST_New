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
import com.techhaven.model.Order;
import com.techhaven.model.OrderItem;
import com.techhaven.model.OrderStatusHistory;

/**
 * Реализация доступа к таблице Orders и OrderItems в SQLite.
 * Все SELECT используют JOIN OrderStatuses для получения имени статуса.
 * PII-данные (телефон, адрес) хранятся в зашифрованном виде (AES-256).
 */
public class OrderRepository implements IOrderRepository {
    private static final Logger LOGGER = Logger.getLogger(OrderRepository.class.getName());
    private final DatabaseManager db = DatabaseManager.getInstance();

    private static final String SELECT_BASE =
        "SELECT o.*, os.name AS status, u.username FROM Orders o " +
        "JOIN OrderStatuses os ON o.status_id = os.id " +
        "JOIN Users u ON o.user_id = u.id";

    @Override
    public Order create(Order order) {
        String sql = """
                INSERT INTO Orders (user_id, order_date, status_id, delivery_address, contact_phone,
                                   delivery_time_interval, comment, total_amount, created_at, updated_at)
                VALUES (?, datetime('now','localtime'), (SELECT id FROM OrderStatuses WHERE name = ?), ?, ?, ?, ?, ?, datetime('now','localtime'), datetime('now','localtime'))""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setString(2, order.getStatus() != null ? order.getStatus() : "Новый");
            ps.setString(3, com.techhaven.security.SecurityManager.getInstance().encrypt(order.getDeliveryAddress()));
            ps.setString(4, com.techhaven.security.SecurityManager.getInstance().encrypt(order.getContactPhone()));
            ps.setString(5, order.getDeliveryTimeInterval());
            ps.setString(6, order.getComment());
            ps.setDouble(7, order.getTotalAmount());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) order.setId(keys.getInt(1));
            return order;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка создания заказа", e);
        }
        return null;
    }

    @Override
    public void createOrderItem(OrderItem item) {
        String sql = """
                INSERT INTO OrderItems (order_id, product_id, quantity, price_at_order, created_at)
                VALUES (?, ?, ?, ?, datetime('now','localtime'))""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getPriceAtOrder());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка добавления позиции заказа", e);
        }
    }

    @Override
    public List<Order> findByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE o.user_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) orders.add(mapOrder(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения заказов пользователя", e);
        }
        return orders;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY o.created_at DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) orders.add(mapOrder(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения всех заказов", e);
        }
        return orders;
    }

    @Override
    public Order findById(int id) {
        String sql = SELECT_BASE + " WHERE o.id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapOrder(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка поиска заказа", e);
        }
        return null;
    }

    @Override
    public List<OrderItem> findOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = """
                SELECT oi.*, p.name AS product_name FROM OrderItems oi
                JOIN Products p ON oi.product_id = p.id
                WHERE oi.order_id = ?""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPriceAtOrder(rs.getDouble("price_at_order"));
                item.setProductName(rs.getString("product_name"));
                items.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения позиций заказа", e);
        }
        return items;
    }

    @Override
    public void updateStatus(int orderId, String status) {
        String sql = "UPDATE Orders SET status_id = (SELECT id FROM OrderStatuses WHERE name = ?), updated_at = datetime('now','localtime') WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления статуса заказа", e);
        }
    }

    @Override
    public void updatePlannedDelivery(int orderId, String date, String interval) {
        String sql = "UPDATE Orders SET planned_delivery_date = ?, planned_delivery_interval = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.setString(2, interval);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления планируемой доставки", e);
        }
    }

    @Override
    public void addStatusHistory(int orderId, String status, int changedBy) {
        String sql = """
                INSERT INTO OrderStatusHistory (order_id, status_id, changed_at, changed_by)
                VALUES (?, (SELECT id FROM OrderStatuses WHERE name = ?), datetime('now','localtime'), ?)""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, status);
            ps.setInt(3, changedBy);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка записи истории статуса", e);
        }
    }

    @Override
    public List<OrderStatusHistory> getStatusHistory(int orderId) {
        List<OrderStatusHistory> history = new ArrayList<>();
        String sql = """
                SELECT osh.*, os.name AS status, u.username AS changed_by_name FROM OrderStatusHistory osh
                JOIN OrderStatuses os ON osh.status_id = os.id
                LEFT JOIN Users u ON osh.changed_by = u.id
                WHERE osh.order_id = ? ORDER BY osh.changed_at DESC""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderStatusHistory h = new OrderStatusHistory();
                h.setId(rs.getInt("id"));
                h.setOrderId(rs.getInt("order_id"));
                h.setStatusId(rs.getInt("status_id"));
                h.setStatus(rs.getString("status"));
                h.setChangedAt(rs.getString("changed_at"));
                h.setChangedBy(rs.getInt("changed_by"));
                h.setChangedByName(rs.getString("changed_by_name"));
                history.add(h);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения истории статусов", e);
        }
        return history;
    }

    @Override
    public List<String> findUsedPhones(int userId) {
        List<String> phones = new ArrayList<>();
        String sql = """
                SELECT contact_phone FROM Orders
                WHERE user_id = ? AND contact_phone IS NOT NULL
                ORDER BY created_at DESC""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String encryptedPhone = rs.getString("contact_phone");
                if (encryptedPhone != null && !encryptedPhone.isEmpty()) {
                    String phone = com.techhaven.security.SecurityManager.getInstance().decrypt(encryptedPhone);
                    if (phone != null && !phone.isEmpty() && !phones.contains(phone)) {
                        phones.add(phone);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения телефонов", e);
        }
        return phones;
    }

    @Override
    public Order findLastByUserId(int userId) {
        String sql = SELECT_BASE + " WHERE o.user_id = ? ORDER BY o.created_at DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapOrder(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения последнего заказа", e);
        }
        return null;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setOrderDate(rs.getString("order_date"));
        o.setStatusId(rs.getInt("status_id"));
        o.setStatus(rs.getString("status"));
        o.setDeliveryAddress(com.techhaven.security.SecurityManager.getInstance().decrypt(rs.getString("delivery_address")));
        try { o.setContactPhone(com.techhaven.security.SecurityManager.getInstance().decrypt(rs.getString("contact_phone"))); } catch (SQLException ignored) {}
        try { o.setDeliveryTimeInterval(rs.getString("delivery_time_interval")); } catch (SQLException ignored) {}
        try { o.setComment(rs.getString("comment")); } catch (SQLException ignored) {}
        try { o.setPlannedDeliveryDate(rs.getString("planned_delivery_date")); } catch (SQLException ignored) {}
        try { o.setPlannedDeliveryInterval(rs.getString("planned_delivery_interval")); } catch (SQLException ignored) {}
        o.setTotalAmount(rs.getDouble("total_amount"));
        try {
            String ca = rs.getString("created_at");
            if (ca != null && !ca.isEmpty()) o.setCreatedAt(parseDateTime(ca));
        } catch (SQLException | java.time.format.DateTimeParseException ignored) {}
        try {
            String ua = rs.getString("updated_at");
            if (ua != null && !ua.isEmpty()) o.setUpdatedAt(parseDateTime(ua));
        } catch (SQLException | java.time.format.DateTimeParseException ignored) {}
        try { o.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        return o;
    }

    private java.time.LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String norm = raw.replace("T", " ").replaceAll("\\.\\d+$", "");
        try {
            if (norm.length() >= 19) {
                return java.time.LocalDateTime.parse(norm.substring(0, 19),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (norm.length() >= 16) {
                return java.time.LocalDateTime.parse(norm.substring(0, 16),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
