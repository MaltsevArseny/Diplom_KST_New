package com.techhaven.model;

import java.time.LocalDateTime;

/**
 * Модель заказа покупателя.
 *
 * <p>Соответствует таблице {@code Orders} в БД.
 * Жизненный цикл статуса: Новый → В обработке → Подтверждён → Собран →
 * Отправлен → Доставлен → Завершён (или Отменён из любого нетерминального).</p>
 *
 * <p>Покупатель указывает желаемую дату/время доставки ({@code deliveryTimeInterval}),
 * администратор назначает фактическую ({@code plannedDeliveryDate} +
 * {@code plannedDeliveryInterval}).</p>
 *
 * @see com.techhaven.repository.OrderRepository
 * @see com.techhaven.model.OrderStatus
 */
public class Order {
    private int id;                        // PK — уникальный номер заказа
    private int userId;                    // FK → Users.id (кто оформил)
    private String orderDate;              // Дата/время оформления заказа (ISO-8601)
    private int statusId;                  // FK → OrderStatuses.id (текущий статус)
    private String status;                 // Название статуса (из JOIN OrderStatuses.name)
    private String deliveryAddress;        // Адрес доставки (указывает покупатель)
    private String contactPhone;           // Телефон для курьера
    private String deliveryTimeInterval;   // Желаемый интервал доставки (от покупателя)
    private String comment;                // Комментарий покупателя к заказу
    private String plannedDeliveryDate;    // Плановая дата доставки (назначает админ)
    private String plannedDeliveryInterval;// Плановый интервал доставки (назначает админ)
    private double totalAmount;            // Итого в рублях (₽)
    private LocalDateTime createdAt;       // Системная дата создания
    private LocalDateTime updatedAt;       // Системная дата последнего изменения

    // Транзиентное поле (не хранится в Orders, заполняется из JOIN Users.username)
    private String username;

    public Order() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getDeliveryTimeInterval() { return deliveryTimeInterval; }
    public void setDeliveryTimeInterval(String deliveryTimeInterval) { this.deliveryTimeInterval = deliveryTimeInterval; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getPlannedDeliveryDate() { return plannedDeliveryDate; }
    public void setPlannedDeliveryDate(String plannedDeliveryDate) { this.plannedDeliveryDate = plannedDeliveryDate; }

    public String getPlannedDeliveryInterval() { return plannedDeliveryInterval; }
    public void setPlannedDeliveryInterval(String plannedDeliveryInterval) { this.plannedDeliveryInterval = plannedDeliveryInterval; }

    public String getFormattedTotal() {
        return String.format("%,.0f ₽", totalAmount);
    }
}
