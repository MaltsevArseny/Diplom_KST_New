package com.techhaven.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Простая шина событий для слабой связности компонентов (Observer pattern).
 * <p>Publish/Subscribe: компоненты подписываются на типы событий и получают
 * уведомления при публикации, без прямых зависимостей между ними.</p>
 *
 * <p>Использование:</p>
 * <pre>
 *   EventBus.subscribe("cart.updated", data -> refreshCartBadge());
 *   EventBus.publish("cart.updated", null);
 * </pre>
 */
public final class EventBus {
    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
    private static final Map<String, List<Consumer<Object>>> LISTENERS = new ConcurrentHashMap<>();

    private EventBus() {}

    /**
     * Подписаться на событие.
     * @param event название (например "cart.updated", "favorite.changed")
     * @param handler обработчик, получающий данные события (может быть null)
     */
    public static void subscribe(String event, Consumer<Object> handler) {
        LISTENERS.computeIfAbsent(event, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /**
     * Отписаться от события.
     */
    public static void unsubscribe(String event, Consumer<Object> handler) {
        List<Consumer<Object>> handlers = LISTENERS.get(event);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    /**
     * Опубликовать событие — все подписчики получат уведомление.
     * @param event название
     * @param data  данные (может быть null)
     */
    public static void publish(String event, Object data) {
        List<Consumer<Object>> handlers = LISTENERS.get(event);
        if (handlers != null) {
            for (Consumer<Object> handler : handlers) {
                try {
                    handler.accept(data);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Ошибка обработки события: " + event, e);
                }
            }
        }
    }

    /**
     * Очистить все подписки (для тестов).
     */
    public static void clearAll() {
        LISTENERS.clear();
    }
}
