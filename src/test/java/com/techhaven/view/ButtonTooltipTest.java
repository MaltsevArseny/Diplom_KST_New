package com.techhaven.view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * Статический анализ: проверяет, что для каждого вызова {@code new Button(...)} 
 * в исходных файлах View есть соответствующий вызов {@code setTooltip(...)}.
 *
 * Тест сканирует файлы {@code src/main/java/com/techhaven/view/*.java} и
 * подкаталоги (component), ищет Button-переменные и проверяет наличие
 * {@code .setTooltip} для каждой из них.
 *
 * Некоторые кнопки исключаются (например, undo-кнопки, создаваемые динамически
 * в таймерных callback'ах, или кнопки-ссылки на товары).
 */
class ButtonTooltipTest {

    // Паттерн: объявление Button, например «Button applyBtn = new Button("..."»
    private static final Pattern BUTTON_DECL = Pattern.compile(
        "\\b(?:final\\s+)?Button\\s+(\\w+)\\s*=\\s*new\\s+Button\\b"
    );
    // Паттерн: вызов setTooltip для переменной
    private static final Pattern TOOLTIP_CALL = Pattern.compile(
        "(\\w+)\\.setTooltip\\s*\\("
    );

    // Переменные-кнопки, для которых допустимо отсутствие tooltip
    // (например динамические undo-кнопки, которые живут 5 секунд,
    //  или кнопки-ссылки на товары вроде «itemLink», «itemName»)
    private static final Set<String> EXCEPTIONS = Set.of(
        "undoBtn",   // временные кнопки отмены удаления
        "showMore",  // «Показать ещё» — смысл понятен из текста
        "moreBtn",   // кнопки пагинации
        "itemLink",  // ссылка-на-товар в списке заказов
        "okBtn",     // модальная кнопка «OK» в DialogHelper
        "btn"        // кнопка в createNavButton() — tooltip ставится после возврата метода
    );

    @Test
    void allButtonsMustHaveTooltip() throws IOException {
        Path viewDir = Paths.get("src/main/java/com/techhaven/view");
        assertTrue(Files.exists(viewDir), "Каталог view не найден: " + viewDir);

        List<String> violations = new ArrayList<>();

        try (var stream = Files.walk(viewDir)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                  .forEach(file -> {
                      try {
                          checkFile(file, violations);
                      } catch (IOException e) {
                          fail("Ошибка чтения файла " + file + ": " + e.getMessage());
                      }
                  });
        }

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Найдены кнопки без setTooltip (").append(violations.size()).append("):\n");
            for (String v : violations) {
                sb.append("  • ").append(v).append("\n");
            }
            fail(sb.toString());
        }
    }

    private void checkFile(Path file, List<String> violations) throws IOException {
        String content = Files.readString(file);
        String fileName = file.getFileName().toString();

        // Собираем имена Button-переменных
        Set<String> declaredButtons = new LinkedHashSet<>();
        Matcher declMatcher = BUTTON_DECL.matcher(content);
        while (declMatcher.find()) {
            declaredButtons.add(declMatcher.group(1));
        }

        // Собираем имена переменных, для которых вызван setTooltip
        Set<String> tooltipVars = new HashSet<>();
        Matcher tipMatcher = TOOLTIP_CALL.matcher(content);
        while (tipMatcher.find()) {
            tooltipVars.add(tipMatcher.group(1));
        }

        // Проверяем: каждая Button должна иметь setTooltip  
        for (String btn : declaredButtons) {
            if (EXCEPTIONS.contains(btn)) continue;
            if (!tooltipVars.contains(btn)) {
                violations.add(fileName + " → " + btn);
            }
        }
    }
}
