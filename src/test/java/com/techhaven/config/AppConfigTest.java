package com.techhaven.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    @Test
    @DisplayName("Значения по умолчанию для авторизации")
    void authDefaults() {
        assertEquals(5, AppConfig.MAX_LOGIN_ATTEMPTS);
        assertEquals(5, AppConfig.LOCK_DURATION_MINUTES);
    }

    @Test
    @DisplayName("Значения по умолчанию для безопасности")
    void securityDefaults() {
        assertEquals(100000, AppConfig.PBKDF2_ITERATIONS);
        assertEquals(16, AppConfig.SALT_LENGTH);
        assertEquals(256, AppConfig.KEY_LENGTH);
    }

    @Test
    @DisplayName("Значения по умолчанию для UI")
    void uiDefaults() {
        assertEquals(5, AppConfig.UNDO_TIMEOUT_SECONDS);
        assertEquals(40, AppConfig.CATALOG_PAGE_SIZE);
    }

    @Test
    @DisplayName("get возвращает defaultValue если ключ не задан")
    void getWithDefault() {
        String result = AppConfig.get("nonexistent.key", "fallback");
        assertEquals("fallback", result);
    }
}
