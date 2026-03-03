package com.techhaven.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techhaven.config.DatabaseManager;
import com.techhaven.config.SessionManager;
import com.techhaven.model.Order;
import com.techhaven.model.OrderItem;
import com.techhaven.model.OrderStatusHistory;
import com.techhaven.model.User;
import com.techhaven.repository.CartRepository;

class OrderServiceTest {

    private final OrderService orderService = new OrderService();

    @BeforeEach
    void loginTestUser() {
        User user = new User("TestBuyer", "testbuyer_order@digitalhub.local", "+70000000001", "hash", "USER");
        user.setId(1);
        SessionManager.getInstance().login(user);
        // Очищаем корзину для изоляции тестов
        new CartRepository().clearCart(1);
        // Восстанавливаем stock тестовых товаров (мог быть списан предыдущими прогонами)
        restoreStock(200, 201, 202, 203, 204);
    }

    /** Восстановить stock_quantity до 10 для указанных товаров */
    private void restoreStock(int... productIds) {
        String sql = "UPDATE Products SET stock_quantity = 10 WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int id : productIds) {
                ps.setInt(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось восстановить stock для тестов", e);
        }
    }

    @SuppressWarnings("unused")
    @AfterEach
    void logout() {
        SessionManager.getInstance().logout();
    }

    // === Валидация placeOrder ===

    @Test
    void placeOrderRequiresLogin() {
        SessionManager.getInstance().logout();
        String result = orderService.placeOrder("ул. Тестовая 1", "+71234567890", "09:00 - 12:00", null);
        assertEquals(OrderService.MSG_NOT_LOGGED_IN, result);
    }

    @Test
    void placeOrderRejectsEmptyAddress() {
        String result = orderService.placeOrder("", "+71234567890", "09:00 - 12:00", null);
        assertEquals(OrderService.MSG_EMPTY_ADDRESS, result);
    }

    @Test
    void placeOrderRejectsNullAddress() {
        String result = orderService.placeOrder(null, "+71234567890", "09:00 - 12:00", null);
        assertEquals(OrderService.MSG_EMPTY_ADDRESS, result);
    }

    @Test
    void placeOrderRejectsInvalidPhone() {
        String result = orderService.placeOrder("ул. Тестовая 1", "12345", "09:00 - 12:00", null);
        assertEquals(OrderService.MSG_INVALID_PHONE, result);
    }

    @Test
    void placeOrderRejectsNullPhone() {
        String result = orderService.placeOrder("ул. Тестовая 1", null, "09:00 - 12:00", null);
        assertEquals(OrderService.MSG_INVALID_PHONE, result);
    }

    @Test
    void placeOrderRejectsEmptyInterval() {
        String result = orderService.placeOrder("ул. Тестовая 1", "+71234567890", "", null);
        assertEquals(OrderService.MSG_EMPTY_INTERVAL, result);
    }

    @Test
    void placeOrderRejectsNullInterval() {
        String result = orderService.placeOrder("ул. Тестовая 1", "+71234567890", null, null);
        assertEquals(OrderService.MSG_EMPTY_INTERVAL, result);
    }

    @Test
    void placeOrderRejectsEmptyCart() {
        String result = orderService.placeOrder("ул. Тестовая 1", "+71234567890", "09:00 - 12:00", null);
        assertEquals(OrderService.MSG_EMPTY_CART, result);
    }

    @Test
    void placeOrderSuccessReturnsNull() {
        CartService cartService = new CartService();
        String addResult = cartService.addToCart(200, 1);
        assertNull(addResult, "Не удалось добавить товар в корзину: " + addResult);
        String result = orderService.placeOrder("ул. Тестовая 1", "+71234567890", "09:00 - 12:00", "Тестовый заказ");
        assertNull(result, "placeOrder должен вернуть null при успехе");
    }

    // === Чтение заказов ===

    @Test
    void getAllOrdersReturnsNonNull() {
        List<Order> orders = orderService.getAllOrders();
        assertNotNull(orders);
    }

    @Test
    void getMyOrdersReturnsNonNull() {
        List<Order> orders = orderService.getMyOrders();
        assertNotNull(orders);
    }

    @Test
    void getOrderItemsForInvalidOrderReturnsEmpty() {
        List<OrderItem> items = orderService.getOrderItems(-999);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void getOrderByIdReturnsNullForInvalid() {
        assertNull(orderService.getOrderById(-999));
    }

    @Test
    void getIncompleteOrdersCountNonNegative() {
        int count = orderService.getIncompleteOrdersCount();
        assertTrue(count >= 0);
    }

    @Test
    void getUsedPhonesReturnsNonNull() {
        List<String> phones = orderService.getUsedPhones();
        assertNotNull(phones);
    }

    @Test
    void getLastOrderReturnsNullOrOrder() {
        orderService.getLastOrder();
        // Может быть null если заказов нет — просто проверяем что не бросает исключение
    }

    /**
     * Создаёт свежий заказ со статусом «Новый» для изоляции тестов
     * статусных переходов. Возвращает ID нового заказа, или -1 если не удалось.
     */
    private int placeNewOrder(int productId) {
        CartService cartService = new CartService();
        new CartRepository().clearCart(SessionManager.getInstance().getCurrentUserId());
        String addResult = cartService.addToCart(productId, 1);
        assertNull(addResult, "Не удалось добавить товар в корзину: " + addResult);
        List<Order> ordersBefore = orderService.getMyOrders();
        int maxIdBefore = ordersBefore.stream().mapToInt(Order::getId).max().orElse(0);
        String result = orderService.placeOrder("ул. Тестовая 99", "+70000000099", "09:00 — 12:00", "Для теста");
        assertNull(result, "Не удалось оформить заказ: " + result);
        List<Order> ordersAfter = orderService.getMyOrders();
        int maxIdAfter = ordersAfter.stream().mapToInt(Order::getId).max().orElse(0);
        assertTrue(maxIdAfter > maxIdBefore, "Не удалось создать заказ");
        return maxIdAfter;
    }

    // === Обновление статуса: валидация последовательности ===

    @Test
    @DisplayName("Допустимый переход: Новый → В обработке успешен")
    void validStatusTransitionReturnsNull() {
        int orderId = placeNewOrder(201);
        assertTrue(orderId > 0, "Не удалось создать тестовый заказ");

        String result = orderService.updateStatus(orderId, "В обработке");
        assertNull(result, "Ожидается null (успех) при допустимом переходе Новый → В обработке");
    }

    @Test
    @DisplayName("Недопустимый переход: пропуск статуса (Новый → Доставлен) возвращает ошибку")
    void invalidSkipTransitionReturnsError() {
        int orderId = placeNewOrder(202);
        assertTrue(orderId > 0, "Не удалось создать тестовый заказ");

        String result = orderService.updateStatus(orderId, "Доставлен");
        assertNotNull(result, "Пропуск статуса должен вернуть ошибку");
        assertTrue(result.contains(OrderService.MSG_INVALID_TRANSITION),
            "Ошибка должна содержать сообщение о недопустимом переходе");
    }

    @Test
    @DisplayName("Отмена заказа допустима из незавершённого статуса")
    void cancelFromNonTerminalIsAllowed() {
        int orderId = placeNewOrder(203);
        assertTrue(orderId > 0, "Не удалось создать тестовый заказ");

        String result = orderService.updateStatus(orderId, "Отменен");
        assertNull(result, "Отмена из незавершённого статуса должна быть успешной");
    }

    @Test
    @DisplayName("Отмена завершённого заказа запрещена")
    void cancelFromCompletedIsForbidden() {
        // Создаём заказ и проводим его по всей цепочке до "Завершён"
        int orderId = placeNewOrder(204);
        assertTrue(orderId > 0, "Не удалось создать тестовый заказ");
        // Полная цепочка: Новый → В обработке → Подтверждён → Собран → Отправлен → Доставлен → Завершён
        assertNull(orderService.updateStatus(orderId, "В обработке"));
        assertNull(orderService.updateStatus(orderId, "Подтверждён"));
        assertNull(orderService.updateStatus(orderId, "Собран"));
        assertNull(orderService.updateStatus(orderId, "Отправлен"));
        assertNull(orderService.updateStatus(orderId, "Доставлен"));
        assertNull(orderService.updateStatus(orderId, "Завершён"));

        String result = orderService.updateStatus(orderId, "Отменен");
        assertNotNull(result, "Отмена завершённого заказа должна вернуть ошибку");
    }

    @Test
    @DisplayName("updateStatus для несуществующего заказа возвращает ошибку")
    void updateStatusForNonExistentReturnsError() {
        loginTestUser();
        String result = orderService.updateStatus(-999, "В обработке");
        assertNotNull(result, "Обновление несуществующего заказа должно вернуть ошибку");
    }

    @Test
    @DisplayName("updatePlannedDelivery устанавливает дату и интервал доставки")
    void updatePlannedDeliveryForSeedOrder() {
        List<Order> orders = orderService.getAllOrders();
        org.junit.jupiter.api.Assumptions.assumeFalse(orders.isEmpty(), "Нет seed-заказов");
        Order first = orders.get(0);
        String date = "2026-03-20";
        String interval = "14:00-16:00";
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> orderService.updatePlannedDelivery(first.getId(), date, interval)
        );
        // Проверяем, что дата и интервал сохранились
        Order updated = orderService.getOrderById(first.getId());
        assertNotNull(updated, "Заказ должен существовать после обновления");
        assertEquals(date, updated.getPlannedDeliveryDate(), "Дата планируемой доставки должна обновиться");
        assertEquals(interval, updated.getPlannedDeliveryInterval(), "Интервал планируемой доставки должен обновиться");
    }

    @Test
    @DisplayName("getStatusHistory возвращает пустой список для несуществующего заказа")
    void getStatusHistoryForInvalidOrder() {
        List<OrderStatusHistory> history = orderService.getStatusHistory(-999);
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("getAllOrders содержит seed-заказы")
    void getAllOrdersContainsSeedOrders() {
        List<Order> orders = orderService.getAllOrders();
        assertNotNull(orders);
        assertFalse(orders.isEmpty(), "Должны быть seed-заказы");
    }

    @Test
    @DisplayName("Seed-заказы имеют заполненный статус")
    void seedOrdersHaveStatus() {
        List<Order> orders = orderService.getAllOrders();
        org.junit.jupiter.api.Assumptions.assumeFalse(orders.isEmpty(), "Нет seed-заказов");
        for (Order o : orders) {
            assertNotNull(o.getStatus(), "Статус должен быть заполнен для заказа #" + o.getId());
            assertFalse(o.getStatus().isBlank(), "Статус не должен быть пустым для заказа #" + o.getId());
        }
    }

    @Test
    @DisplayName("Seed-заказы имеют непустой адрес доставки")
    void seedOrdersHaveDeliveryAddress() {
        List<Order> orders = orderService.getAllOrders();
        org.junit.jupiter.api.Assumptions.assumeFalse(orders.isEmpty(), "Нет seed-заказов");
        for (Order o : orders) {
            assertNotNull(o.getDeliveryAddress(), "Адрес должен быть заполнен для заказа #" + o.getId());
            assertFalse(o.getDeliveryAddress().isBlank(), "Адрес не должен быть пустым для заказа #" + o.getId());
        }
    }

    // === Тесты истории статусов и планируемых дат доставки ===

    @Test
    @DisplayName("Seed-заказы имеют непустую историю статусов")
    void seedOrdersHaveStatusHistory() {
        List<Order> orders = orderService.getAllOrders();
        org.junit.jupiter.api.Assumptions.assumeFalse(orders.isEmpty(), "Нет seed-заказов");
        for (Order o : orders) {
            List<OrderStatusHistory> history = orderService.getStatusHistory(o.getId());
            assertNotNull(history, "История не должна быть null для заказа #" + o.getId());
            assertFalse(history.isEmpty(),
                "Заказ #" + o.getId() + " (статус=" + o.getStatus() + ") должен иметь историю статусов");
        }
    }

    @Test
    @DisplayName("В истории статусов нет записей «впереди» текущего статуса заказа")
    void statusHistoryDoesNotContainFutureStatuses() {
        List<String> statusOrder = List.of("Новый", "В обработке", "Подтверждён",
            "Собран", "Отправлен", "Доставлен", "Завершен");
        List<Order> orders = orderService.getAllOrders();
        org.junit.jupiter.api.Assumptions.assumeFalse(orders.isEmpty(), "Нет seed-заказов");

        for (Order o : orders) {
            int currentIdx = statusOrder.indexOf(o.getStatus());
            if (currentIdx < 0) continue; // Отменен или неизвестный — пропускаем

            List<OrderStatusHistory> history = orderService.getStatusHistory(o.getId());
            for (OrderStatusHistory h : history) {
                int hIdx = statusOrder.indexOf(h.getStatus());
                assertTrue(hIdx <= currentIdx,
                    "Заказ #" + o.getId() + " (статус=" + o.getStatus() +
                    ") содержит в истории будущий статус «" + h.getStatus() + "»");
            }
        }
    }

    @Test
    @DisplayName("Заказы со статусом Собран и далее имеют планируемую дату доставки")
    void ordersFromSobranHavePlannedDeliveryDate() {
        List<String> statusesNeedDelivery = List.of("Собран", "Отправлен", "Доставлен", "Завершен");
        List<Order> orders = orderService.getAllOrders();
        org.junit.jupiter.api.Assumptions.assumeFalse(orders.isEmpty(), "Нет seed-заказов");

        for (Order o : orders) {
            if (statusesNeedDelivery.contains(o.getStatus())) {
                assertNotNull(o.getPlannedDeliveryDate(),
                    "Заказ #" + o.getId() + " (статус=" + o.getStatus() +
                    ") должен иметь planned_delivery_date");
                assertFalse(o.getPlannedDeliveryDate().isBlank(),
                    "planned_delivery_date не должна быть пустой для заказа #" + o.getId());
            }
        }
    }

    @Test
    @DisplayName("Записи истории статусов имеют заполненную дату-время изменения")
    void statusHistoryEntriesHaveTimestamp() {
        List<Order> orders = orderService.getAllOrders();
        org.junit.jupiter.api.Assumptions.assumeFalse(orders.isEmpty(), "Нет seed-заказов");

        for (Order o : orders) {
            List<OrderStatusHistory> history = orderService.getStatusHistory(o.getId());
            for (OrderStatusHistory h : history) {
                assertNotNull(h.getChangedAt(),
                    "Запись истории статуса «" + h.getStatus() +
                    "» для заказа #" + o.getId() + " должна иметь changedAt");
                assertFalse(h.getChangedAt().isBlank(),
                    "changedAt не должен быть пустым для статуса «" + h.getStatus() +
                    "» заказа #" + o.getId());
            }
        }
    }
}
