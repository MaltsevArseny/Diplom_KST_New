package com.techhaven.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Централизованная конфигурация приложения.
 * Читает из app.properties (classpath), а при отсутствии — использует defaults.
 */
public final class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties PROPS = new Properties();

    // ─── Авторизация (защита от brute-force) ────────────────────────
    /** Макс. неудачных попыток входа до блокировки аккаунта */
    public static final int MAX_LOGIN_ATTEMPTS;
    /** Продолжительность блокировки в минутах */
    public static final int LOCK_DURATION_MINUTES;

    // ─── Безопасность (криптография) ─────────────────────────────────
    /** Кол-во итераций PBKDF2 (100k — компромисс скорости и защиты) */
    public static final int PBKDF2_ITERATIONS;
    /** Длина соли в байтах (16 байт = 128 бит) */
    public static final int SALT_LENGTH;
    /** Длина ключа AES в битах (256 — максимальная для AES) */
    public static final int KEY_LENGTH;

    // ─── UI (настройки интерфейса) ───────────────────────────────────
    /** Таймаут отмены удаления в секундах (паттерн Undo) */
    public static final int UNDO_TIMEOUT_SECONDS;
    /** Кол-во товаров на одной странице каталога */
    public static final int CATALOG_PAGE_SIZE;

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                PROPS.load(in);
                LOGGER.info("Конфигурация загружена из app.properties");
            } else {
                LOGGER.info("app.properties не найден, используются значения по умолчанию");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Ошибка загрузки app.properties", e);
        }

        MAX_LOGIN_ATTEMPTS  = getInt("auth.max_attempts", 5);
        LOCK_DURATION_MINUTES = getInt("auth.lock_minutes", 5);
        PBKDF2_ITERATIONS   = getInt("security.pbkdf2_iterations", 100000);
        SALT_LENGTH         = getInt("security.salt_length", 16);
        KEY_LENGTH          = getInt("security.key_length", 256);
        UNDO_TIMEOUT_SECONDS = getInt("ui.undo_timeout_seconds", 5);
        CATALOG_PAGE_SIZE   = getInt("ui.catalog_page_size", 40);
    }

    private AppConfig() {}

    private static int getInt(String key, int defaultValue) {
        String val = PROPS.getProperty(key);
        if (val != null) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException e) {
                LOGGER.log(java.util.logging.Level.WARNING, "Неверный формат для {0}: {1}", new Object[]{key, val});
            }
        }
        return defaultValue;
    }

    public static String get(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }
}
