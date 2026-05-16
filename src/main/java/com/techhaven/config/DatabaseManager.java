package com.techhaven.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Менеджер базы данных SQLite (Singleton).
 *
 * <p>При первом вызове {@link #getInstance()} выполняется полная инициализация:
 * создание таблиц (schema.sql), сидирование справочников (Categories, OrderStatuses),
 * товаров (500 шт.), пользователей (11) и тестовых заказов.</p>
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    /** URL подключения к БД. Путь резолвится через {@link AppPaths#dbFile()} —
     *  по умолчанию data-директория рядом с JAR'ом. Можно переопределить
     *  через -Ddb.path=&lt;absolute-path&gt; либо -Dapp.data.dir=&lt;dir&gt;. */
    private static final String DB_URL = "jdbc:sqlite:" +
            AppPaths.dbFile().toString().replace('\\', '/');
    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Получает новое соединение к SQLite.
     * Автоматически включает поддержку внешних ключей (PRAGMA foreign_keys = ON).
     */
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка подключения к БД", e);
            throw new RuntimeException("Не удалось подключиться к базе данных", e);
        }
    }

    /**
     * Полная инициализация БД: схема → справочники → товары → пользователи → заказы.
     */
    private void initializeDatabase() {
        try {
            // Этап 1: Создание таблиц из schema.sql
            String schema = loadResource("/schema.sql");
            // Strip UTF-8 BOM if present
            if (schema.charAt(0) == '﻿') schema = schema.substring(1);
            // Remove -- comments before splitting so comment-only fragments are skipped cleanly
            schema = schema.replaceAll("--[^\n]*", "");
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String sql : schema.split(";")) {
                    String t = sql.trim();
                    if (!t.isEmpty()) stmt.execute(t);
                }
            }

            // Этап 2: Сидирование справочников (идемпотентно через INSERT OR IGNORE)
            seedCategories();
            seedOrderStatuses();

            // Этап 3: Сидирование товаров (500 штук)
            int productCount;
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Products")) {
                productCount = rs.next() ? rs.getInt(1) : 0;
            }
            LOGGER.log(Level.INFO, "Товаров в БД: {0}", productCount);
            if (productCount < 500) {
                seedAllProducts();
                try (Connection conn = getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Products")) {
                    int after = rs.next() ? rs.getInt(1) : 0;
                    LOGGER.log(Level.INFO, "После сидирования: {0} товаров", after);
                }
            }

            // Этап 4: Пользователи и заказы
            seedUsersAndOrders();

            LOGGER.info("База данных инициализирована успешно");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка инициализации БД", e);
        }
    }

    // ── Справочник категорий ─────────────────────────────────────────────────
    private void seedCategories() {
        String[] cats = {
            "Процессоры", "Видеокарты", "Оперативная память", "Накопители",
            "Материнские платы", "Блоки питания", "Охлаждение", "Корпуса",
            "Мониторы", "Периферия", "Сетевое оборудование"
        };
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR IGNORE INTO Categories (name) VALUES (?)")) {
            for (String c : cats) { ps.setString(1, c); ps.addBatch(); }
            ps.executeBatch();
            LOGGER.info("Справочник категорий сидирован");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "seedCategories", e);
        }
    }

    // ── Справочник статусов заказов ──────────────────────────────────────────
    private void seedOrderStatuses() {
        String[][] statuses = {
            {"Новый", "1"}, {"В обработке", "2"}, {"Подтверждён", "3"},
            {"Собран", "4"}, {"Отправлен", "5"}, {"Доставлен", "6"},
            {"Завершён", "7"}, {"Отменён", "8"}
        };
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR IGNORE INTO OrderStatuses (name, sort_order) VALUES (?, ?)")) {
            for (String[] s : statuses) {
                ps.setString(1, s[0]);
                ps.setInt(2, Integer.parseInt(s[1]));
                ps.addBatch();
            }
            ps.executeBatch();
            LOGGER.info("Справочник статусов сидирован");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "seedOrderStatuses", e);
        }
    }

    // ── Пользователи и заказы ───────────────────────────────────────────────
    private void seedUsersAndOrders() {
        com.techhaven.security.SecurityManager sec = com.techhaven.security.SecurityManager.getInstance();

        // 8 обычных + 3 админа = 11 пользователей
        Object[][] users = {
            {"Александр Ковалев",  "user1@techhaven.ru", "+79111111101", "User1Pass!", "USER"},
            {"Мария Сидорова",     "user2@techhaven.ru", "+79111111102", "User2Pass!", "USER"},
            {"Дмитрий Волков",     "user3@techhaven.ru", "+79111111103", "User3Pass!", "USER"},
            {"Елена Новикова",     "user4@techhaven.ru", "+79111111104", "User4Pass!", "USER"},
            {"Сергей Морозов",     "user5@techhaven.ru", "+79111111105", "User5Pass!", "USER"},
            {"Ольга Попова",       "user6@techhaven.ru", "+79111111106", "User6Pass!", "USER"},
            {"Николай Соколов",    "user7@techhaven.ru", "+79111111107", "User7Pass!", "USER"},
            {"Татьяна Лебедева",   "user8@techhaven.ru", "+79111111108", "User8Pass!", "USER"},
            {"Андрей Захаров",     "admin1@techhaven.ru", "+79111111109", "Admin1Pass!", "ADMIN"},
            {"Наталья Козлова",    "admin2@techhaven.ru", "+79111111110", "Admin2Pass!", "ADMIN"},
            {"Игорь Смирнов",      "admin3@techhaven.ru", "+79111111111", "Admin3Pass!", "ADMIN"},
        };

        String insertUser = "INSERT OR IGNORE INTO Users (username,email,phone,password_hash,role,created_at) VALUES (?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(insertUser)) {
            for (Object[] u : users) {
                ps.setString(1, (String) u[0]);
                ps.setString(2, (String) u[1]);
                ps.setString(3, sec.encrypt((String) u[2]));
                ps.setString(4, sec.hashPassword((String) u[3]));
                ps.setString(5, (String) u[4]);
                ps.setString(6, "2024-12-01 10:00:00");
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) { LOGGER.log(Level.WARNING, "seedUsers", e); }

        // Проверяем, есть ли уже seed-заказы
        int orderCount = 0;
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Orders");
            if (rs.next()) orderCount = rs.getInt(1);
        } catch (Exception e) { LOGGER.log(Level.WARNING, "check orders", e); }
        if (orderCount > 0) return;

        // Собираем ID USER-пользователей
        List<Integer> userIds = new ArrayList<>();
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id FROM Users WHERE role='USER' ORDER BY id");
            while (rs.next()) userIds.add(rs.getInt(1));
        } catch (Exception e) { LOGGER.log(Level.WARNING, "fetch userIds", e); }
        if (userIds.isEmpty()) { LOGGER.warning("Нет USER-пользователей"); return; }

        // Собираем ID товаров и цены
        List<Integer> productIds = new ArrayList<>();
        List<Double> productPrices = new ArrayList<>();
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id, price FROM Products ORDER BY id");
            while (rs.next()) { productIds.add(rs.getInt(1)); productPrices.add(rs.getDouble(2)); }
        } catch (Exception e) { LOGGER.log(Level.WARNING, "fetch products", e); }
        if (productIds.isEmpty()) { LOGGER.warning("Нет товаров для seed"); return; }

        // Получаем ID статусов (по sort_order)
        int[] statusIds = new int[8]; // indexed 0..7
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id, sort_order FROM OrderStatuses ORDER BY sort_order");
            while (rs.next()) {
                int idx = rs.getInt("sort_order") - 1;
                if (idx >= 0 && idx < 8) statusIds[idx] = rs.getInt("id");
            }
        } catch (Exception e) { LOGGER.log(Level.WARNING, "fetch statuses", e); }

        String[] addresses = {
            "г. Москва, ул. Тверская, д. 15, кв. 42",
            "г. Санкт-Петербург, Невский пр-т, д. 88, кв. 7",
            "г. Новосибирск, ул. Ленина, д. 23, кв. 105",
            "г. Екатеринбург, ул. Малышева, д. 51, кв. 19",
            "г. Казань, ул. Баумана, д. 36, кв. 8",
            "г. Нижний Новгород, ул. Большая Покровская, д. 12, кв. 31",
            "г. Самара, ул. Молодогвардейская, д. 66, кв. 14",
            "г. Ростов-на-Дону, ул. Большая Садовая, д. 73, кв. 22",
            "г. Краснодар, ул. Красная, д. 145, кв. 3",
            "г. Воронеж, пр-т Революции, д. 34, кв. 61"
        };
        String[] phones = {
            "+7 (916) 123-45-67", "+7 (926) 234-56-78", "+7 (903) 345-67-89",
            "+7 (965) 456-78-90", "+7 (912) 567-89-01", "+7 (951) 678-90-12",
            "+7 (495) 789-01-23", "+7 (925) 890-12-34", "+7 (905) 901-23-45",
            "+7 (913) 012-34-56"
        };
        String[] timeSlots = {"09:00 — 12:00","10:00 — 14:00","12:00 — 16:00","14:00 — 18:00","16:00 — 20:00"};

        // Распределение заказов: user1=5, user2=10, user3=15, user4=20, user5=30, user6=40, user7=50 = 170 заказов
        int[] ordersPerUser = {5, 10, 15, 20, 30, 40, 50};
        Random rnd = new Random(42);

        // Период: 01.01.2025 — 01.03.2026 = 425 дней
        java.time.LocalDate startDate = java.time.LocalDate.of(2025, 1, 1);
        java.time.LocalDate endDate = java.time.LocalDate.of(2026, 3, 1);
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

        String insertOrder = "INSERT INTO Orders (user_id, status_id, total_amount, order_date, " +
            "delivery_address, contact_phone, delivery_time_interval, " +
            "planned_delivery_date, planned_delivery_interval, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItem = "INSERT INTO OrderItems (order_id,product_id,quantity,price_at_order) VALUES (?,?,?,?)";
        String insertHist = "INSERT INTO OrderStatusHistory (order_id, status_id, changed_at, changed_by) VALUES (?, ?, ?, ?)";

        // Целевые статусы для заказов (распределение): 0=Новый,1=В обработке,..,5=Доставлен,6=Завершён,7=Отменён
        // Примерно: 10% новые, 10% в обработке, 10% подтверждены, 10% собраны, 10% отправлены, 25% доставлены, 20% завершены, 5% отменены
        int[] targetDistribution = {0,0, 1,1, 2,2, 3,3, 4,4, 5,5,5,5,5, 6,6,6,6, 7};

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psO = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psI = conn.prepareStatement(insertItem);
                 PreparedStatement psH = conn.prepareStatement(insertHist)) {

                int adminId = 0;
                try (Statement st = conn.createStatement()) {
                    ResultSet rs = st.executeQuery("SELECT id FROM Users WHERE role='ADMIN' LIMIT 1");
                    if (rs.next()) adminId = rs.getInt(1);
                }

                for (int u = 0; u < Math.min(ordersPerUser.length, userIds.size()); u++) {
                    int userId = userIds.get(u);
                    int numOrders = ordersPerUser[u];

                    for (int i = 0; i < numOrders; i++) {
                        // Равномерно распределяем по интервалу
                        int dayOffset = (int) ((long) i * totalDays / numOrders) + rnd.nextInt(5);
                        java.time.LocalDate d = startDate.plusDays(Math.min(dayOffset, totalDays - 1));
                        String dateStr = d + " " + String.format("%02d:%02d:%02d", 8 + rnd.nextInt(12), rnd.nextInt(60), rnd.nextInt(60));

                        // Выбираем целевой статус
                        int targetStatusIdx = targetDistribution[rnd.nextInt(targetDistribution.length)];
                        // Для заказов из будущих дат (> сегодня) ставим ранние статусы
                        if (d.isAfter(java.time.LocalDate.of(2026, 2, 15))) {
                            targetStatusIdx = Math.min(targetStatusIdx, 1); // макс В обработке
                        }

                        // 1–4 позиции в заказе
                        int itemCount = 1 + rnd.nextInt(4);
                        double total = 0;
                        int[] chosenProd = new int[itemCount];
                        int[] chosenQty = new int[itemCount];
                        double[] chosenPrice = new double[itemCount];
                        for (int j = 0; j < itemCount; j++) {
                            int pi = rnd.nextInt(productIds.size());
                            chosenProd[j] = productIds.get(pi);
                            chosenQty[j] = 1 + rnd.nextInt(3);
                            chosenPrice[j] = productPrices.get(pi);
                            total += chosenQty[j] * chosenPrice[j];
                        }

                        // Плановая доставка
                        java.time.LocalDate deliveryDate = d.plusDays(2 + rnd.nextInt(5));
                        String plannedDate = targetStatusIdx >= 3 ? deliveryDate.toString() : null;
                        String plannedInterval = targetStatusIdx >= 3 ? timeSlots[rnd.nextInt(timeSlots.length)] : null;

                        psO.setInt(1, userId);
                        psO.setInt(2, statusIds[targetStatusIdx]);
                        psO.setDouble(3, total);
                        psO.setString(4, dateStr);
                        psO.setString(5, sec.encrypt(addresses[rnd.nextInt(addresses.length)]));
                        psO.setString(6, sec.encrypt(phones[rnd.nextInt(phones.length)]));
                        psO.setString(7, deliveryDate + " " + timeSlots[rnd.nextInt(timeSlots.length)]);
                        psO.setString(8, plannedDate);
                        psO.setString(9, plannedInterval);
                        psO.setString(10, dateStr);
                        psO.setString(11, dateStr);
                        psO.executeUpdate();

                        ResultSet gk = psO.getGeneratedKeys();
                        if (!gk.next()) continue;
                        int orderId = gk.getInt(1);

                        for (int j = 0; j < itemCount; j++) {
                            psI.setInt(1, orderId);
                            psI.setInt(2, chosenProd[j]);
                            psI.setInt(3, chosenQty[j]);
                            psI.setDouble(4, chosenPrice[j]);
                            psI.addBatch();
                        }
                        psI.executeBatch();

                        // История статусов: от «Новый» до целевого
                        int histEnd = targetStatusIdx == 7 ? rnd.nextInt(5) : targetStatusIdx; // Отменён — с произвольного
                        for (int k = 0; k <= histEnd; k++) {
                            psH.setInt(1, orderId);
                            psH.setInt(2, statusIds[k]);
                            psH.setString(3, d.plusDays(k) + " " + String.format("%02d:00:00", 8 + k * 2));
                            psH.setInt(4, adminId > 0 ? adminId : 1);
                            psH.addBatch();
                        }
                        if (targetStatusIdx == 7) { // Отменён
                            psH.setInt(1, orderId);
                            psH.setInt(2, statusIds[7]);
                            psH.setString(3, d.plusDays(histEnd + 1) + " 10:00:00");
                            psH.setInt(4, adminId > 0 ? adminId : 1);
                            psH.addBatch();
                        }
                        psH.executeBatch();
                    }
                }
                conn.commit();
                LOGGER.info("Seed-заказы успешно сохранены");
            } catch (Exception ex) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "seedOrders rollback", ex);
            }
        } catch (Exception e) { LOGGER.log(Level.WARNING, "seedUsersAndOrders", e); }

        // Гарантируем наличие товаров с остатком = 0
        try (Connection conn = getConnection(); Statement upd = conn.createStatement()) {
            upd.executeUpdate(
                "UPDATE Products SET stock_quantity = 0 WHERE name IN (" +
                "'Gigabyte RTX 4080 16GB', 'ASUS ROG X670E Hero', 'MSI Suprim X RTX 4090')");
        } catch (Exception e) { LOGGER.log(Level.WARNING, "zero stock", e); }
    }

    // ── Сидирование 500 товаров ─────────────────────────────────────────────
    private void seedAllProducts() {
        // Формат: {name, description, categoryName, price, stock, specs}
        Object[][] p = seedProductData();

        String sql = "INSERT OR IGNORE INTO Products (name, description, category_id, price, stock_quantity, specifications) " +
                     "VALUES (?, ?, (SELECT id FROM Categories WHERE name = ?), ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            int batch = 0;
            int total = 0;
            for (Object[] row : p) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]); // category name → subquery
                ps.setDouble(4, ((Number) row[3]).doubleValue());
                ps.setInt(5, ((Number) row[4]).intValue());
                ps.setString(6, (String) row[5]);
                ps.addBatch();
                batch++;
                if (batch == 50) {
                    ps.executeBatch();
                    conn.commit();
                    total += batch;
                    batch = 0;
                    LOGGER.log(Level.INFO, "Вставлено {0} товаров...", total);
                }
            }
            if (batch > 0) {
                ps.executeBatch();
                conn.commit();
                total += batch;
            }
            LOGGER.log(Level.INFO, "seedAllProducts завершён: {0} товаров", total);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Ошибка seedAllProducts", e);
        }
    }

    /**
     * Возвращает массив данных 500 товаров.
     * Каждая строка: {name, description, categoryName, price, stock, specs}.
     */
    private Object[][] seedProductData() {
        return SeedProducts.getData();
    }

    /**
     * Загружает содержимое ресурса из classpath.
     */
    private String loadResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Ресурс не найден: " + path);
            return new BufferedReader(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить ресурс: " + path, e);
        }
    }
}
