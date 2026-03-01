package com.techhaven.model;

/**
 * Статусы заказа в порядке жизненного цикла.
 */
public enum OrderStatus {
    NEW("Новый"),
    PROCESSING("В обработке"),
    CONFIRMED("Подтверждён"),
    ASSEMBLED("Собран"),
    SHIPPED("Отправлен"),
    DELIVERED("Доставлен"),
    COMPLETED("Завершён"),
    CANCELLED("Отменён");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Парсинг из строки БД (по displayName, case-insensitive).
     */
    public static OrderStatus fromString(String s) {
        if (s == null) return NEW;
        // Нормализуем: "Завершен" → "Завершён" для обратной совместимости
        String normalized = s.trim()
            .replace("Завершен", "Завершён")
            .replace("Отменен", "Отменён")
            .replace("Подтвержден", "Подтверждён");
        for (OrderStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(normalized)) return status;
        }
        return NEW;
    }

    /**
     * Проверяет, является ли заказ завершённым (финальный статус).
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
