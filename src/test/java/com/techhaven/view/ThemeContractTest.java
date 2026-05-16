package com.techhaven.view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Статический UI-contract: проверяет соответствие view-кода и CSS-палитры.
 *
 * <p>Это не TestFX (JavaFX runtime не запускается), а серия статических
 * аудитов, гарантирующих что:</p>
 * <ul>
 *   <li>Обе темы (dark, light) определяют один и тот же набор {@code -th-*} переменных.</li>
 *   <li>В Java-коде view/ нет hardcoded {@code white} / {@code #ffffff} (требование sand-палитры).</li>
 *   <li>Все ключевые CSS-классы из THEME_PALETTE.md существуют в обеих темах.</li>
 *   <li>{@code light-theme.css} не использует чисто-белый ни в фонах, ни в шрифтах.</li>
 * </ul>
 */
class ThemeContractTest {

    private static final Path DARK_CSS = Paths.get("src/main/resources/styles/dark-theme.css");
    private static final Path LIGHT_CSS = Paths.get("src/main/resources/styles/light-theme.css");
    private static final Path VIEW_DIR = Paths.get("src/main/java/com/techhaven/view");

    /** Извлекает имена -th-* переменных, объявленных в файле (определения через `:`). */
    private static Set<String> extractThemeVars(Path css) throws IOException {
        String content = Files.readString(css);
        Set<String> vars = new TreeSet<>();
        // Сканируем строки на наличие "  -th-something: value;" — это объявление переменной.
        // Использование `-th-X` справа от `:` (значение) НЕ матчится из-за привязки к началу строки.
        Pattern p = Pattern.compile("^\\s*(-th-[a-z-]+)\\s*:", Pattern.MULTILINE);
        Matcher m = p.matcher(content);
        while (m.find()) vars.add(m.group(1));
        return vars;
    }

    @Test
    @DisplayName("Обе темы определяют один и тот же набор -th-* переменных")
    void bothThemesDefineSameVariables() throws IOException {
        Set<String> darkVars = extractThemeVars(DARK_CSS);
        Set<String> lightVars = extractThemeVars(LIGHT_CSS);

        Set<String> onlyInDark = new TreeSet<>(darkVars);
        onlyInDark.removeAll(lightVars);
        Set<String> onlyInLight = new TreeSet<>(lightVars);
        onlyInLight.removeAll(darkVars);

        StringBuilder err = new StringBuilder();
        if (!onlyInDark.isEmpty()) {
            err.append("Переменные есть в dark, но отсутствуют в light: ").append(onlyInDark).append('\n');
        }
        if (!onlyInLight.isEmpty()) {
            err.append("Переменные есть в light, но отсутствуют в dark: ").append(onlyInLight).append('\n');
        }
        if (err.length() > 0) fail(err.toString());

        assertEquals(darkVars, lightVars,
            "Темы должны иметь одинаковый набор -th-* переменных");
    }

    @Test
    @DisplayName("Обязательные -th-* переменные присутствуют в обеих темах")
    void requiredVariablesPresent() throws IOException {
        Set<String> required = Set.of(
            "-th-bg-primary", "-th-bg-secondary", "-th-bg-card", "-th-bg-hover",
            "-th-accent", "-th-accent-hover", "-th-accent-light",
            "-th-success", "-th-warning", "-th-danger",
            "-th-text-primary", "-th-text-secondary", "-th-text-muted",
            "-th-border", "-th-border-focus", "-th-shadow", "-th-cream"
        );
        Set<String> darkVars = extractThemeVars(DARK_CSS);
        Set<String> lightVars = extractThemeVars(LIGHT_CSS);
        for (String v : required) {
            assertTrue(darkVars.contains(v), "В dark-theme.css отсутствует " + v);
            assertTrue(lightVars.contains(v), "В light-theme.css отсутствует " + v);
        }
    }

    @Test
    @DisplayName("В Java view/* нет inline 'white' или '#ffffff'")
    void noWhiteInJavaViews() throws IOException {
        List<String> violations = new ArrayList<>();
        Pattern white = Pattern.compile(
            "-fx-(text-fill|background-color)\\s*:\\s*(white\\b|#[fF]{3}([fF]{3})?\\b)"
        );

        try (var stream = Files.walk(VIEW_DIR)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                  .forEach(file -> {
                      try {
                          String content = Files.readString(file);
                          String[] lines = content.split("\n");
                          for (int i = 0; i < lines.length; i++) {
                              if (white.matcher(lines[i]).find()) {
                                  violations.add(file.getFileName() + ":" + (i + 1) + " — " + lines[i].trim());
                              }
                          }
                      } catch (IOException e) {
                          fail("Ошибка чтения " + file + ": " + e.getMessage());
                      }
                  });
        }

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Найдены hardcoded white в Java view (");
            sb.append(violations.size()).append("):\n");
            for (String v : violations) sb.append("  • ").append(v).append('\n');
            sb.append("\nЗамените на -th-cream (для текста на акцентах) или другую -th-* переменную.");
            fail(sb.toString());
        }
    }

    @Test
    @DisplayName("light-theme.css не содержит white / #ffffff в CSS-значениях (sand-палитра)")
    void lightThemeHasNoWhite() throws IOException {
        String content = Files.readString(LIGHT_CSS);
        // Удаляем многострочные /* ... */ комментарии целиком, чтобы они не давали false-positive
        String stripped = content.replaceAll("(?s)/\\*.*?\\*/", "");
        List<String> violations = new ArrayList<>();
        String[] lines = stripped.split("\n");
        // Ищем 'white' либо #fff/#ffffff ТОЛЬКО в контексте значения CSS (после `:`)
        Pattern white = Pattern.compile(
            "-fx-[a-z-]+\\s*:\\s*[^;]*\\b(white|WHITE|#[fF]{6}|#[fF]{3})\\b"
        );
        for (int i = 0; i < lines.length; i++) {
            if (white.matcher(lines[i]).find()) {
                violations.add("light-theme.css:" + (i + 1) + " — " + lines[i].trim());
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("В light-theme.css найден чисто-белый цвет (");
            sb.append(violations.size()).append("):\n");
            for (String v : violations) sb.append("  • ").append(v).append('\n');
            sb.append("\nПесочная палитра запрещает white. Используйте -th-cream (#faf3e0).");
            fail(sb.toString());
        }
    }

    @Test
    @DisplayName("Ключевые CSS-классы присутствуют в обеих темах")
    void requiredCssClasses() throws IOException {
        // Классы, на которые ссылается Java-код через getStyleClass().add(...)
        Set<String> required = Set.of(
            ".app-root",
            ".window-control-button",
            ".window-close-button",
            ".help-button",
            ".theme-toggle-button",
            ".dialog-close-button",
            ".role-badge-admin",
            ".sidebar-menu-title",
            ".nav-badge",
            ".undo-button",
            ".qty-plus-button",
            ".nav-bar",
            ".sidebar",
            ".sidebar-item",
            ".btn-primary",
            ".btn-success",
            ".btn-danger",
            ".badge"
        );
        String dark = Files.readString(DARK_CSS);
        String light = Files.readString(LIGHT_CSS);

        List<String> missingDark = new ArrayList<>();
        List<String> missingLight = new ArrayList<>();
        for (String cls : required) {
            // Селектор должен встречаться (с {, : или , после)
            Pattern p = Pattern.compile(Pattern.quote(cls) + "\\s*[{:,]");
            if (!p.matcher(dark).find()) missingDark.add(cls);
            if (!p.matcher(light).find()) missingLight.add(cls);
        }
        StringBuilder err = new StringBuilder();
        if (!missingDark.isEmpty()) err.append("В dark-theme.css отсутствуют классы: ").append(missingDark).append('\n');
        if (!missingLight.isEmpty()) err.append("В light-theme.css отсутствуют классы: ").append(missingLight).append('\n');
        if (err.length() > 0) fail(err.toString());
    }

    @Test
    @DisplayName("light-theme.css использует песочные оттенки для основных фонов")
    void lightThemeUsesSandPalette() throws IOException {
        String content = Files.readString(LIGHT_CSS);
        // Проверяем точные песочные значения в .root
        assertTrue(content.contains("-th-bg-primary: #f5ede0"),
            "Фон primary в light должен быть песочным #f5ede0");
        assertTrue(content.contains("-th-bg-secondary: #ede0c8"),
            "Фон secondary в light должен быть #ede0c8");
        assertTrue(content.contains("-th-bg-card: #f0e6d2"),
            "Фон карточек в light должен быть #f0e6d2");
        assertTrue(content.contains("-th-text-primary: #111827")
                   || content.contains("-th-text-primary: #3d2f1f"),
            "Текст primary в light должен быть тёмно-коричневым или близким");
        assertTrue(content.contains("-th-cream: #faf3e0"),
            "Cream в light должен быть #faf3e0");
    }
}
