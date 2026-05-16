package com.techhaven.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techhaven.config.ThemeManager.Theme;

/**
 * Юнит-тесты ThemeManager: дефолт, переключение, persist (Preferences), listener.
 *
 * Persist реализован через java.util.prefs.Preferences — узел приложения
 * "com/techhaven", ключ "ui.theme". Тесты не оставляют побочных эффектов:
 * @BeforeEach очищает ключ, @AfterEach восстанавливает дефолт.
 */
class ThemeManagerTest {

    private static final Preferences PREFS = Preferences.userRoot().node("com/techhaven");
    private static final String KEY = "ui.theme";

    @BeforeEach
    void clear() {
        PREFS.remove(KEY);
        ThemeManager.getInstance().reloadFromPersist();
    }

    @AfterEach
    void cleanup() {
        PREFS.remove(KEY);
        ThemeManager.getInstance().reloadFromPersist();
    }

    @Test
    @DisplayName("По умолчанию активна тёмная тема")
    void defaultIsDark() {
        assertEquals(Theme.DARK, ThemeManager.getInstance().getCurrent());
    }

    @Test
    @DisplayName("setCurrent меняет активную тему")
    void setCurrentChangesTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.setCurrent(Theme.LIGHT);
        assertEquals(Theme.LIGHT, tm.getCurrent());
        tm.setCurrent(Theme.DARK);
        assertEquals(Theme.DARK, tm.getCurrent());
    }

    @Test
    @DisplayName("toggle переключает с DARK на LIGHT и обратно")
    void toggleSwitches() {
        ThemeManager tm = ThemeManager.getInstance();
        assertEquals(Theme.DARK, tm.getCurrent());
        tm.toggle();
        assertEquals(Theme.LIGHT, tm.getCurrent());
        tm.toggle();
        assertEquals(Theme.DARK, tm.getCurrent());
    }

    @Test
    @DisplayName("setCurrent сохраняет выбор в Preferences")
    void setCurrentPersists() {
        ThemeManager.getInstance().setCurrent(Theme.LIGHT);
        String stored = PREFS.get(KEY, null);
        assertEquals("LIGHT", stored);
    }

    @Test
    @DisplayName("reloadFromPersist подхватывает сохранённое значение")
    void reloadFromPersistReadsStoredValue() {
        PREFS.put(KEY, "LIGHT");
        ThemeManager.getInstance().reloadFromPersist();
        assertEquals(Theme.LIGHT, ThemeManager.getInstance().getCurrent());
    }

    @Test
    @DisplayName("Невалидное persist-значение даёт fallback на DARK")
    void invalidPersistValueFallsBackToDark() {
        PREFS.put(KEY, "MAUVE");
        ThemeManager.getInstance().reloadFromPersist();
        assertEquals(Theme.DARK, ThemeManager.getInstance().getCurrent());
    }

    @Test
    @DisplayName("Listener вызывается при смене темы и получает новое значение")
    void listenerFires() {
        ThemeManager tm = ThemeManager.getInstance();
        AtomicInteger calls = new AtomicInteger(0);
        AtomicReference<Theme> received = new AtomicReference<>();

        ThemeManager.ThemeListener l = theme -> {
            calls.incrementAndGet();
            received.set(theme);
        };
        tm.addListener(l);
        try {
            tm.setCurrent(Theme.LIGHT);
            assertEquals(1, calls.get(), "listener должен вызваться ровно один раз");
            assertEquals(Theme.LIGHT, received.get(), "listener получает новое значение темы");
        } finally {
            tm.removeListener(l);
        }
    }

    @Test
    @DisplayName("Listener НЕ вызывается, если setCurrent передаёт ту же тему")
    void listenerNotFiredOnSameValue() {
        ThemeManager tm = ThemeManager.getInstance();
        AtomicInteger calls = new AtomicInteger(0);
        ThemeManager.ThemeListener l = theme -> calls.incrementAndGet();
        tm.addListener(l);
        try {
            tm.setCurrent(Theme.DARK); // dark уже активна
            assertEquals(0, calls.get());
        } finally {
            tm.removeListener(l);
        }
    }

    @Test
    @DisplayName("removeListener отключает обратный вызов")
    void removeListenerStopsCallbacks() {
        ThemeManager tm = ThemeManager.getInstance();
        AtomicInteger calls = new AtomicInteger(0);
        ThemeManager.ThemeListener l = theme -> calls.incrementAndGet();
        tm.addListener(l);
        tm.removeListener(l);
        tm.setCurrent(Theme.LIGHT);
        assertEquals(0, calls.get());
    }

    @Test
    @DisplayName("getCssPath возвращает classpath-путь активной темы")
    void cssPathMatchesCurrentTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.setCurrent(Theme.DARK);
        assertEquals("/styles/dark-theme.css", tm.getCssPath());
        tm.setCurrent(Theme.LIGHT);
        assertEquals("/styles/light-theme.css", tm.getCssPath());
    }

    @Test
    @DisplayName("CSS-ресурсы dark и light доступны в classpath")
    void cssResourcesExist() {
        assertNotNull(getClass().getResource("/styles/dark-theme.css"),
            "dark-theme.css должна быть в classpath");
        assertNotNull(getClass().getResource("/styles/light-theme.css"),
            "light-theme.css должна быть в classpath");
    }

    @Test
    @DisplayName("Singleton возвращает один и тот же экземпляр")
    void singletonIdentity() {
        assertSame(ThemeManager.getInstance(), ThemeManager.getInstance());
    }

    @Test
    @DisplayName("Theme.opposite возвращает противоположную тему")
    void themeOpposite() {
        assertEquals(Theme.LIGHT, Theme.DARK.opposite());
        assertEquals(Theme.DARK, Theme.LIGHT.opposite());
        assertNotSame(Theme.DARK, Theme.LIGHT);
    }

    @Test
    @DisplayName("isDark / isLight согласованы с getCurrent")
    void isDarkIsLight() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.setCurrent(Theme.DARK);
        assertTrue(tm.isDark());
        assertFalse(tm.isLight());
        tm.setCurrent(Theme.LIGHT);
        assertTrue(tm.isLight());
        assertFalse(tm.isDark());
    }
}
