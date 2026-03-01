package com.techhaven.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatabaseManagerTest {

    @Test
    void singletonAlwaysReturnsSameInstance() {
        DatabaseManager a = DatabaseManager.getInstance();
        DatabaseManager b = DatabaseManager.getInstance();
        assertSame(a, b);
    }

    @Test
    void getConnectionReturnsNonNull() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    void usersTableExists() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Users'")) {
            assertTrue(rs.next(), "Users table should exist");
        }
    }

    @Test
    void productsTableExists() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Products'")) {
            assertTrue(rs.next(), "Products table should exist");
        }
    }

    @Test
    void ordersTableExists() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Orders'")) {
            assertTrue(rs.next(), "Orders table should exist");
        }
    }

    @Test
    @DisplayName("Продуктов в БД >= 300 (после исправления дубликатов)")
    void productsAreSeedded() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Products")) {
            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertTrue(count >= 300, "Ожидается >= 300 товаров после фикса дубликатов, получено " + count);
        }
    }

    @Test
    @DisplayName("Все названия товаров уникальны")
    void productNamesAreUnique() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COUNT(*) AS total, COUNT(DISTINCT name) AS uniq FROM Products")) {
            assertTrue(rs.next());
            int total = rs.getInt("total");
            int uniq  = rs.getInt("uniq");
            assertEquals(total, uniq, "Все названия товаров должны быть уникальны");
        }
    }

    @Test
    void usersAreSeedded() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users")) {
            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertTrue(count >= 1, "Expected at least 1 user, got " + count);
        }
    }

    @Test
    @DisplayName("Таблица OrderStatusHistory существует")
    void orderStatusHistoryTableExists() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name FROM sqlite_master WHERE type='table' AND name='OrderStatusHistory'")) {
            assertTrue(rs.next(), "OrderStatusHistory table should exist");
        }
    }

    @Test
    @DisplayName("Seed-заказы содержат данные")
    void ordersAreSeedded() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Orders")) {
            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertTrue(count >= 50, "Ожидается >= 50 seed-заказов, получено " + count);
        }
    }

    @Test
    @DisplayName("Seed-заказы покрывают все категории товаров")
    void seedOrdersCoverAllCategories() throws Exception {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            // Все категории из справочника
            ResultSet rs1 = stmt.executeQuery(
                "SELECT COUNT(*) FROM Categories");
            assertTrue(rs1.next());
            int totalCategories = rs1.getInt(1);
            assertTrue(totalCategories >= 5,
                "Ожидается >= 5 категорий товаров, получено " + totalCategories);

            // Категории, представленные в заказах
            ResultSet rs2 = stmt.executeQuery(
                "SELECT COUNT(DISTINCT p.category_id) FROM OrderItems oi" +
                " JOIN Products p ON p.id = oi.product_id" +
                " WHERE p.category_id IS NOT NULL");
            assertTrue(rs2.next());
            int orderedCategories = rs2.getInt(1);

            assertEquals(totalCategories, orderedCategories,
                "Seed-заказы должны покрывать все " + totalCategories +
                " категорий, но покрывают только " + orderedCategories);
        }
    }
}
