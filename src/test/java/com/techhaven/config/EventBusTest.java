package com.techhaven.config;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventBusTest {

    @BeforeEach
    void cleanup() {
        EventBus.clearAll();
    }

    @Test
    @DisplayName("subscribe + publish доставляет событие")
    void subscribeAndPublish() {
        AtomicInteger counter = new AtomicInteger(0);
        EventBus.subscribe("test.event", data -> counter.incrementAndGet());

        EventBus.publish("test.event", null);
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Несколько подписчиков получают событие")
    void multipleSubscribers() {
        AtomicInteger counter = new AtomicInteger(0);
        EventBus.subscribe("multi", data -> counter.incrementAndGet());
        EventBus.subscribe("multi", data -> counter.incrementAndGet());

        EventBus.publish("multi", null);
        assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("publish передаёт данные подписчику")
    void publishWithData() {
        AtomicReference<Object> received = new AtomicReference<>();
        EventBus.subscribe("data.event", received::set);

        EventBus.publish("data.event", "hello");
        assertEquals("hello", received.get());
    }

    @Test
    @DisplayName("unsubscribe удаляет подписчика")
    void unsubscribe() {
        AtomicInteger counter = new AtomicInteger(0);
        java.util.function.Consumer<Object> handler = data -> counter.incrementAndGet();
        EventBus.subscribe("unsub", handler);

        EventBus.publish("unsub", null);
        assertEquals(1, counter.get());

        EventBus.unsubscribe("unsub", handler);
        EventBus.publish("unsub", null);
        assertEquals(1, counter.get()); // не увеличился
    }

    @Test
    @DisplayName("publish несуществующего события не вызывает ошибку")
    void publishNonexistent() {
        assertDoesNotThrow(() -> EventBus.publish("no.such.event", null));
    }

    @Test
    @DisplayName("clearAll очищает все подписки")
    void clearAll() {
        AtomicInteger counter = new AtomicInteger(0);
        EventBus.subscribe("clear.test", data -> counter.incrementAndGet());
        EventBus.clearAll();
        EventBus.publish("clear.test", null);
        assertEquals(0, counter.get());
    }

    @Test
    @DisplayName("Ошибка в обработчике не ломает остальных подписчиков")
    void errorInHandler() {
        AtomicInteger counter = new AtomicInteger(0);
        EventBus.subscribe("error.test", data -> { throw new RuntimeException("boom"); });
        EventBus.subscribe("error.test", data -> counter.incrementAndGet());

        assertDoesNotThrow(() -> EventBus.publish("error.test", null));
        assertEquals(1, counter.get()); // второй обработчик всё равно вызван
    }
}
