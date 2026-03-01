package com.techhaven.model;

/**
 * Модель записи истории смены статусов заказа (аудит-лог).
 *
 * <p>Соответствует таблице {@code OrderStatusHistory} в БД.
 * Каждая смена статуса заказа фиксируется отдельной записью,
 * что позволяет отслеживать время каждого этапа обработки.</p>
 *
 * @see com.techhaven.repository.OrderRepository
 * @see com.techhaven.model.OrderStatus
 */
public class OrderStatusHistory {
    private int id;              // PK — уникальный идентификатор записи
    private int orderId;         // FK → Orders.id (какой заказ)
    private int statusId;        // FK → OrderStatuses.id (новый статус)
    private String status;       // Название статуса (из JOIN OrderStatuses.name)
    private String changedAt;    // Дата/время смены статуса (ISO-8601)
    private int changedBy;       // ID администратора, изменившего статус

    // Транзиентное поле для отображения
    private String changedByName; // Имя администратора (из JOIN Users.username)

    public OrderStatusHistory() {}

    public OrderStatusHistory(int orderId, int statusId, int changedBy) {
        this.orderId = orderId;
        this.statusId = statusId;
        this.changedBy = changedBy;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getChangedAt() { return changedAt; }
    public void setChangedAt(String changedAt) { this.changedAt = changedAt; }

    public int getChangedBy() { return changedBy; }
    public void setChangedBy(int changedBy) { this.changedBy = changedBy; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }
}
