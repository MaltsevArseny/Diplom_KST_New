package com.techhaven.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

/**
 * Управление темами оформления (тёмная / светлая).
 *
 * <p>Хранит активную тему, сохраняет выбор в {@link Preferences}
 * (узел "com/techhaven", ключ "ui.theme") и оповещает слушателей
 * о смене темы.</p>
 *
 * <p>Применение CSS к сцене — через {@code ThemeToggle.applyTo(Scene)} в
 * слое view (вынесено из этого класса, чтобы он не зависел от JavaFX runtime
 * и был покрыт unit-тестами в headless-среде).</p>
 *
 * <p>Дефолтная тема — {@link Theme#DARK}: сохраняется поведение приложения
 * до введения light-темы.</p>
 */
public final class ThemeManager {

    /** Доступные темы оформления. */
    public enum Theme {
        DARK("/styles/dark-theme.css"),
        LIGHT("/styles/light-theme.css");

        private final String cssPath;

        Theme(String cssPath) {
            this.cssPath = cssPath;
        }

        public String cssPath() {
            return cssPath;
        }

        public Theme opposite() {
            return this == DARK ? LIGHT : DARK;
        }
    }

    /** Слушатель смены темы. */
    @FunctionalInterface
    public interface ThemeListener {
        void onThemeChanged(Theme newTheme);
    }

    // Порядок объявлений важен: PREFS / KEY / DEFAULT инициализируются ДО INSTANCE,
    // потому что конструктор ThemeManager сразу вызывает readFromPersist(), который
    // читает PREFS. Static-fields инициализируются сверху вниз.
    private static final Preferences PREFS = Preferences.userRoot().node("com/techhaven");
    private static final String KEY = "ui.theme";
    private static final Theme DEFAULT = Theme.DARK;
    private static final ThemeManager INSTANCE = new ThemeManager();

    private Theme current;
    private final List<ThemeListener> listeners = new CopyOnWriteArrayList<>();

    private ThemeManager() {
        this.current = readFromPersist();
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public Theme getCurrent() {
        return current;
    }

    public boolean isDark() {
        return current == Theme.DARK;
    }

    public boolean isLight() {
        return current == Theme.LIGHT;
    }

    /** Classpath-путь к CSS активной темы. */
    public String getCssPath() {
        return current.cssPath();
    }

    /**
     * Установить тему. Если значение совпадает с текущим — no-op,
     * слушатели НЕ вызываются.
     */
    public void setCurrent(Theme theme) {
        if (theme == null || theme == current) return;
        current = theme;
        PREFS.put(KEY, theme.name());
        for (ThemeListener l : listeners) {
            l.onThemeChanged(theme);
        }
    }

    /** Переключить тему на противоположную. */
    public void toggle() {
        setCurrent(current.opposite());
    }

    /** Перечитать значение из Preferences (для тестов / внешней инвалидации). */
    public void reloadFromPersist() {
        this.current = readFromPersist();
    }

    public void addListener(ThemeListener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(ThemeListener listener) {
        listeners.remove(listener);
    }

    private Theme readFromPersist() {
        String stored = PREFS.get(KEY, null);
        if (stored == null) return DEFAULT;
        try {
            return Theme.valueOf(stored);
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
}
