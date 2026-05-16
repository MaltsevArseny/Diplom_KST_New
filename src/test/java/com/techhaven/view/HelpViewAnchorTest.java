package com.techhaven.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Регрессионный тест на согласованность TOC-ссылок в UserManual.md и
 * AdminManual.md с реальными генерируемыми якорями заголовков H2/H3.
 *
 * <p>Поломка такой согласованности — частый молчаливый баг: TOC написан
 * вручную в одном регистре/форме, заголовок добавляется/правится отдельно,
 * и ссылка перестаёт работать. Этот тест ловит расхождение до того, как
 * пользователь нажмёт «не работает».</p>
 */
class HelpViewAnchorTest {

    /** Якоря вида [Текст](#anchor) — захватываем имя якоря. */
    private static final Pattern TOC_LINK = Pattern.compile("\\(#([^)]+)\\)");
    /** Заголовки H2 (## ...) и H3 (### ...). */
    private static final Pattern HEADING = Pattern.compile("(?m)^(##|###) (.+)$");

    @Test
    @DisplayName("generateAnchor: эмодзи + variation selector корректно срезаются")
    void generateAnchorStripsEmoji() {
        // Базовые случаи
        assertEquals("запуск-приложения", HelpView.generateAnchor("🚀 Запуск приложения"));
        assertEquals("горячие-клавиши", HelpView.generateAnchor("⌨️ Горячие клавиши"));
        assertEquals("справка", HelpView.generateAnchor("❓ Справка"));
        assertEquals("частые-вопросы", HelpView.generateAnchor("🔧 Частые вопросы"));
        // Без эмодзи
        assertEquals("оглавление", HelpView.generateAnchor("Оглавление"));
    }

    @Test
    @DisplayName("UserManual.md: все TOC-ссылки попадают в существующий заголовок")
    void userManualTocLinksAreReachable() {
        verifyManualTocConsistent("UserManual.md");
    }

    @Test
    @DisplayName("AdminManual.md: все TOC-ссылки попадают в существующий заголовок")
    void adminManualTocLinksAreReachable() {
        verifyManualTocConsistent("AdminManual.md");
    }

    // ─── Регрессионные тесты на CRLF-баг ────────────────────────────────────
    // Admin/UserManual.md в Windows-репозитории сохраняются как CRLF. До фикса
    // splitLines() оставлял trailing «\r» в каждой строке, из-за чего
    // line.matches("^[-*] .*") возвращал false (точка regex без DOTALL не
    // матчит \r), и пункты TOC рендерились как plain text без кликабельности.

    @Test
    @DisplayName("splitLines: нормализует CRLF и одиночный CR в чистый LF")
    void splitLinesNormalizesLineEndings() {
        // CRLF
        String[] crlf = HelpView.splitLines("a\r\nb\r\nc");
        assertEquals(3, crlf.length);
        assertEquals("a", crlf[0]);
        assertEquals("b", crlf[1]);
        assertEquals("c", crlf[2]);
        // LF
        String[] lf = HelpView.splitLines("a\nb\nc");
        assertEquals(3, lf.length);
        assertEquals("b", lf[1]);
        // Перемешанный: \r на хвосте последней строки тоже надо снять
        String[] mixed = HelpView.splitLines("a\r\nb\nc\r");
        assertEquals(3, mixed.length);
        assertEquals("a", mixed[0]);
        assertEquals("b", mixed[1]);
        assertEquals("c", mixed[2], "Trailing \\r у последней строки должен сниматься");
    }

    @Test
    @DisplayName("isTocHyperlink: TOC-строка из manual'а классифицируется как Hyperlink (даже с \\r)")
    void isTocHyperlinkClassifiesCorrectly() {
        // Чистая строка без line endings — базовый случай
        assertTrue(HelpView.isTocHyperlink("- [Запуск приложения](#запуск-приложения)"),
            "Базовая TOC-строка должна быть Hyperlink");
        // То же со звёздочкой вместо тире
        assertTrue(HelpView.isTocHyperlink("* [Каталог товаров](#каталог-товаров)"));
        // НЕ TOC — обычный bullet без link
        assertTrue(!HelpView.isTocHyperlink("- Просто текст без ссылки"));
        // НЕ TOC — заголовок
        assertTrue(!HelpView.isTocHyperlink("## Заголовок"));
        // НЕ TOC — null
        assertTrue(!HelpView.isTocHyperlink(null));
    }

    @Test
    @DisplayName("После splitLines в каждой строке TOC manual'а isTocHyperlink=true")
    void allTocLinesInBothManualsAreHyperlinksAfterSplit() {
        for (String filename : new String[]{"UserManual.md", "AdminManual.md"}) {
            Path p = findManual(filename);
            org.junit.jupiter.api.Assumptions.assumeTrue(p != null,
                filename + " не найден — пропуск");
            String content;
            try {
                content = Files.readString(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String[] lines = HelpView.splitLines(content);
            // Подсчитываем сколько строк парсер РЕАЛЬНО классифицирует как
            // Hyperlink. Должно быть >= числа TOC-ссылок в файле.
            int tocLines = 0;
            for (String l : lines) {
                if (HelpView.isTocHyperlink(l)) tocLines++;
            }
            // Сверяем с количеством markdown-ссылок [(#…)] в файле
            int markdownLinks = 0;
            Matcher m = TOC_LINK.matcher(content);
            while (m.find()) markdownLinks++;
            assertEquals(markdownLinks, tocLines,
                filename + ": число распознанных TOC-Hyperlink строк (" + tocLines
                    + ") должно совпадать с числом markdown-ссылок (#anchor) в файле ("
                    + markdownLinks + "). Если меньше — баг парсинга (например, CRLF в строке).");
        }
    }

    /**
     * Читает .md файл и проверяет, что каждый якорь, упомянутый в TOC,
     * совпадает с {@link HelpView#generateAnchor} какого-либо H2/H3 заголовка
     * из того же файла.
     */
    private static void verifyManualTocConsistent(String filename) {
        Path p = findManual(filename);
        org.junit.jupiter.api.Assumptions.assumeTrue(p != null,
            "Файл " + filename + " не найден — пропуск (релевантно только в чек-аутах с manuals)");

        String content;
        try {
            content = Files.readString(p);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать " + p, e);
        }

        // 1. Собираем все TOC-якоря
        Set<String> tocAnchors = new LinkedHashSet<>();
        Matcher m = TOC_LINK.matcher(content);
        while (m.find()) tocAnchors.add(m.group(1));
        assertTrue(!tocAnchors.isEmpty(),
            "В " + filename + " не найдено TOC-ссылок — тест бессмысленен");

        // 2. Собираем все якоря, сгенерированные из реальных заголовков
        Set<String> headingAnchors = new LinkedHashSet<>();
        Matcher h = HEADING.matcher(content);
        while (h.find()) {
            headingAnchors.add(HelpView.generateAnchor(h.group(2).trim()));
        }

        // 3. Каждый TOC-якорь должен присутствовать среди heading-якорей
        StringBuilder missing = new StringBuilder();
        for (String a : tocAnchors) {
            if (!headingAnchors.contains(a)) {
                missing.append("\n  - ").append(a);
            }
        }
        assertTrue(missing.length() == 0,
            "TOC-якоря из " + filename + " не имеют целевого заголовка:" + missing
                + "\nДоступные heading-якоря: " + headingAnchors);
    }

    /** Ищет файл manuala — в текущей рабочей директории или родителе (на случай target/test). */
    private static Path findManual(String name) {
        Path[] candidates = {
            Paths.get(name),
            Paths.get("..", name),
            Paths.get(System.getProperty("user.dir"), name),
        };
        for (Path c : candidates) {
            if (Files.exists(c)) return c;
        }
        return null;
    }
}
