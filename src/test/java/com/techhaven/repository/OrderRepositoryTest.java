package com.techhaven.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.techhaven.model.Order;
import com.techhaven.model.OrderItem;
import com.techhaven.model.OrderStatusHistory;

class OrderRepositoryTest {

    private final OrderRepository orderRepo = new OrderRepository();

    @Test
    void findAllReturnsNonNull() {
        List<Order> orders = orderRepo.findAll();
        assertNotNull(orders);
    }

    @Test
    void findByIdReturnsNullForNonExistent() {
        Order order = orderRepo.findById(-999);
        assertNull(order);
    }

    @Test
    void findByUserIdReturnsNonNull() {
        List<Order> orders = orderRepo.findByUserId(1);
        assertNotNull(orders);
    }

    @Test
    void findOrderItemsForNonExistentReturnsEmpty() {
        List<OrderItem> items = orderRepo.findOrderItems(-999);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void getStatusHistoryForNonExistentReturnsEmpty() {
        List<OrderStatusHistory> history = orderRepo.getStatusHistory(-999);
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void findUsedPhonesReturnsNonNull() {
        List<String> phones = orderRepo.findUsedPhones(1);
        assertNotNull(phones);
    }

    @Test
    void findLastByUserIdReturnsNullOrOrder() {
        // Может быть null если у пользователя нет заказов
        orderRepo.findLastByUserId(-999);
        // Просто проверяем что не бросает исключение
    }

    @Test
    void findAllReturnsSeedOrders() {
        List<Order> orders = orderRepo.findAll();
        assertFalse(orders.isEmpty(), "Должны быть seed-заказы");
    }

    @Test
    void seedOrdersHaveCreatedAtParsed() {
        // Регрессионный тест: mapOrder() должен корректно парсить created_at
        List<Order> orders = orderRepo.findAll();
        assertFalse(orders.isEmpty());
        long withCreatedAt = orders.stream()
            .filter(o -> o.getCreatedAt() != null)
            .count();
        assertTrue(withCreatedAt > 0,
            "Хотя бы один seed-заказ должен иметь заполненный createdAt");
    }
}
