package com.techhaven.repository;

import java.util.List;

import com.techhaven.model.Order;
import com.techhaven.model.OrderItem;
import com.techhaven.model.OrderStatusHistory;

/**
 * Интерфейс репозитория заказов.
 * Определяет контракт для создания, чтения и обновления заказов,
 * их позиций и истории статусов.
 *
 * <p>PII-данные (адрес, телефон) хранятся в зашифрованном виде
 * и расшифровываются при чтении через {@link com.techhaven.security.SecurityManager}.</p>
 */
public interface IOrderRepository {
    /** Создать новый заказ. */
    Order create(Order order);

    /** Создать позицию заказа. */
    void createOrderItem(OrderItem item);

    /** Получить заказы пользователя. */
    List<Order> findByUserId(int userId);

    /** Получить все заказы (для администратора). */
    List<Order> findAll();

    /** Найти заказ по ID. */
    Order findById(int id);

    /** Получить позиции заказа. */
    List<OrderItem> findOrderItems(int orderId);

    /** Обновить статус заказа. */
    void updateStatus(int orderId, String status);

    /** Обновить планируемую дату и интервал доставки. */
    void updatePlannedDelivery(int orderId, String date, String interval);

    /** Добавить запись в историю статусов. */
    void addStatusHistory(int orderId, String status, int changedBy);

    /** Получить историю изменений статуса. */
    List<OrderStatusHistory> getStatusHistory(int orderId);

    /** Получить ранее использованные телефоны пользователя. */
    List<String> findUsedPhones(int userId);

    /** Получить последний заказ пользователя (для автозаполнения). */
    Order findLastByUserId(int userId);
}
