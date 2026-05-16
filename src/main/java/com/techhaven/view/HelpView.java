package com.techhaven.view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.techhaven.MainApp;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Компонент отображения справки из Markdown-файла.
 * Читает .md файл и рендерит его как стилизованный текст в диалоге.
 */
public class HelpView {

    private static final Logger LOGGER = Logger.getLogger(HelpView.class.getName());

    /** Карта якорь → Label заголовка для навигации по оглавлению */
    private static final Map<String, Node> anchorMap = new HashMap<>();

    /** Ссылка на ScrollPane для прокрутки к якорям */
    private static ScrollPane activeScrollPane;

    /**
     * Открыть справку из файла относительно рабочей директории или classpath.
     * @param filename  имя файла (например, "UserManual.md")
     * @param title     заголовок диалога
     */
    public static void show(String filename, String title) {
        anchorMap.clear();
        String content = readMarkdownFile(filename);

        Stage stage = DialogHelper.createStage(MainApp.getPrimaryStage(), true);

        VBox root = new VBox(0);
        root.setStyle(DialogHelper.cardStyle());
        root.setPrefWidth(860);
        root.setPrefHeight(680);

        // Заголовок диалога
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 24));
        header.setStyle(
            "-fx-background-color: -th-bg-secondary;" +
            "-fx-border-color: transparent transparent -th-border transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        Label titleLabel = new Label("❓  " + title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -th-accent-light;");

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("×");
        closeBtn.setTooltip(new javafx.scene.control.Tooltip("Закрыть справку"));
        closeBtn.getStyleClass().addAll("window-control-button", "window-close-button");
        closeBtn.setOnAction(e -> stage.close());

        header.getChildren().addAll(titleLabel, hSpacer, closeBtn);

        // Контент — рендер markdown как styled text
        VBox contentBox = new VBox(6);
        contentBox.setPadding(new Insets(20, 28, 24, 28));
        renderMarkdown(content, contentBox);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        activeScrollPane = scroll;

        root.getChildren().addAll(header, scroll);

        DialogHelper.applyTransparentScene(root, stage);
    }

    /**
     * Прокрутить ScrollPane к узлу, зарегистрированному по якорю.
     */
    private static void scrollToAnchor(String anchor) {
        Node target = anchorMap.get(anchor);
        if (target == null || activeScrollPane == null) return;
        // Отложим прокрутку, чтобы layout успел пересчитаться
        Platform.runLater(() -> {
            VBox content = (VBox) activeScrollPane.getContent();
            Bounds contentBounds = content.getBoundsInLocal();
            Bounds targetBounds = target.getBoundsInParent();
            double yTarget = targetBounds.getMinY();
            double contentHeight = contentBounds.getHeight() - activeScrollPane.getViewportBounds().getHeight();
            if (contentHeight > 0) {
                activeScrollPane.setVvalue(Math.max(0, Math.min(1, yTarget / contentHeight)));
            }
        });
    }

    /**
     * Генерирует якорь из текста заголовка (аналогично GitHub Markdown).
     * Пример: "🚀 Запуск приложения" → "запуск-приложения".
     *
     * <p>Package-private для модульного тестирования: TOC-ссылки из
     * Admin/UserManual.md должны попадать ровно в тот же якорь, который
     * генерируется для соответствующего H2/H3 заголовка.</p>
     */
    static String generateAnchor(String headerText) {
        String anchor = headerText.toLowerCase()
            .replaceAll("[^\\p{L}\\p{N} -]", "")   // убрать спецсимволы и emoji
            .trim()
            .replaceAll("\\s+", "-");                // пробелы → дефисы
        return anchor;
    }

    /**
     * Читает Markdown-файл.
     * Резолв пути через {@link com.techhaven.config.AppPaths#findDocumentFile(String)} —
     * проверяет data-директорию рядом с JAR, её родителя, и рабочую директорию.
     * Если файл не найден на диске — fallback на classpath (/help/).
     */
    private static String readMarkdownFile(String filename) {
        // 1. Поиск через AppPaths (без зависимости от CWD)
        Path p = com.techhaven.config.AppPaths.findDocumentFile(filename);
        if (p != null) {
            try {
                return Files.readString(p, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.warning(String.format("Не удалось прочитать файл: %s — %s", p, e.getMessage()));
            }
        }

        // 2. Classpath (resources/help/)
        String resourcePath = "/help/" + filename;
        try (InputStream is = HelpView.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            LOGGER.warning(String.format("Не удалось прочитать ресурс: %s", resourcePath));
        }

        return "# Справка недоступна\n\nФайл **" + filename + "** не найден.";
    }

    /** Паттерн markdown-ссылки [текст](#якорь) */
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[(.+?)\\]\\(#(.+?)\\)");

    /**
     * Делит markdown-содержимое на строки, нормализуя любые line endings
     * (LF / CRLF / CR) к чистому LF и удаляя trailing «\r», если он остался.
     *
     * <p>Package-private для прямого регрессионного тестирования: исторический
     * CRLF-баг привёл к тому, что TOC-ссылки в Admin/UserManual.md рендерились
     * как plain text вместо Hyperlink — {@code .matches("^[-*] .*")} ломалось
     * на trailing «\r». Тест на этом методе фиксирует контракт «после split
     * ни в одной строке не должно остаться \r».</p>
     */
    /**
     * Возвращает {@code true}, если строка должна рендериться как кликабельная
     * TOC-ссылка (маркер списка «- » или «* », внутри которой найдена
     * markdown-ссылка {@code [text](#anchor)}).
     *
     * <p>Прямой helper для регрессионного теста — историческая ошибка
     * (trailing «\r» из CRLF-файлов делал результат {@code false}, и пункт
     * TOC уходил в ветку «обычный параграф») должна ловиться этим тестом.</p>
     */
    static boolean isTocHyperlink(String line) {
        if (line == null) return false;
        if (!line.matches("^[-*] .*")) return false;
        return LINK_PATTERN.matcher(line.substring(2)).find();
    }

    static String[] splitLines(String content) {
        if (content == null) return new String[0];
        // \\r?\\n покрывает Windows CRLF и Unix LF. Одиночный CR (\\r) без LF
        // встречается крайне редко (старые Mac), но на всякий случай чистим
        // trailing \\r у каждой строки — split по \\r?\\n один \\r не отрежет,
        // если он стоит сам по себе перед концом файла.
        String[] raw = content.split("\\r?\\n", -1);
        for (int i = 0; i < raw.length; i++) {
            if (raw[i] != null && raw[i].endsWith("\r")) {
                raw[i] = raw[i].substring(0, raw[i].length() - 1);
            }
        }
        return raw;
    }

    /**
     * Простой Markdown → JavaFX Label рендер.
     * Поддерживает: # h1-h3, | таблицы, > цитаты, ``` блоки, --- разделители,
     * [text](#anchor) ссылки в оглавлении, обычный текст.
     *
     * <p><b>Важно про line endings:</b> Admin/UserManual.md в Windows-репозитории
     * хранятся как CRLF. Если split'ить по «\n», в каждой строке остаётся
     * trailing «\r», из-за чего {@code line.matches("^[-*] .*")} НЕ срабатывает
     * (точка в regex не матчит «\r»), и пункты TOC рендерились как plain text
     * без кликабельности. Поэтому split по «\\r?\\n» + finishing strip trailing
     * «\r» — обязательное условие корректного рендера TOC.</p>
     */
    private static void renderMarkdown(String content, VBox container) {
        String[] lines = splitLines(content);
        int i = 0;
        while (i < lines.length) {
            String line = lines[i];

            // Блок кода ```
            if (line.trim().startsWith("```")) {
                StringBuilder code = new StringBuilder();
                i++;
                while (i < lines.length && !lines[i].trim().startsWith("```")) {
                    code.append(lines[i]).append("\n");
                    i++;
                }
                Label codeLabel = new Label(code.toString().stripTrailing());
                codeLabel.setStyle(
                    "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                    "-fx-font-size: 12px; -fx-text-fill: -th-success;" +
                    "-fx-background-color: -th-bg-card; -fx-padding: 10 14;" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: -th-border; -fx-border-radius: 6; -fx-border-width: 1;"
                );
                codeLabel.setWrapText(true);
                codeLabel.setMaxWidth(800);
                VBox.setMargin(codeLabel, new Insets(2, 0, 6, 0));
                container.getChildren().add(codeLabel);
                i++;
                continue;
            }

            // Горизонтальный разделитель ---
            if (line.trim().matches("---+")) {
                Region sep = new Region();
                sep.setPrefHeight(1);
                sep.setMaxWidth(Double.MAX_VALUE);
                sep.setStyle("-fx-background-color: -th-border;");
                VBox.setMargin(sep, new Insets(8, 0, 8, 0));
                container.getChildren().add(sep);
                i++;
                continue;
            }

            // Заголовок H1
            if (line.startsWith("# ")) {
                String text = line.substring(2);
                Label lbl = new Label(text);
                lbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: -th-accent-light;");
                lbl.setWrapText(true);
                VBox.setMargin(lbl, new Insets(10, 0, 6, 0));
                anchorMap.put(generateAnchor(text), lbl);
                container.getChildren().add(lbl);
                i++;
                continue;
            }

            // Заголовок H2
            if (line.startsWith("## ")) {
                String text = line.substring(3);
                Label lbl = new Label(text);
                lbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: -th-text-primary;");
                lbl.setWrapText(true);
                VBox.setMargin(lbl, new Insets(14, 0, 4, 0));
                anchorMap.put(generateAnchor(text), lbl);
                container.getChildren().add(lbl);
                i++;
                continue;
            }

            // Заголовок H3
            if (line.startsWith("### ")) {
                String text = line.substring(4);
                Label lbl = new Label(text);
                lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -th-accent-light;");
                lbl.setWrapText(true);
                VBox.setMargin(lbl, new Insets(10, 0, 2, 0));
                anchorMap.put(generateAnchor(text), lbl);
                container.getChildren().add(lbl);
                i++;
                continue;
            }

            // Блок цитаты >
            if (line.startsWith("> ")) {
                Label lbl = new Label(stripInlineMarkdown(line.substring(2)));
                lbl.setStyle(
                    "-fx-text-fill: -th-text-secondary; -fx-font-style: italic; -fx-font-size: 13px;" +
                    "-fx-border-color: -th-accent; -fx-border-width: 0 0 0 3;" +
                    "-fx-padding: 2 0 2 12;"
                );
                lbl.setWrapText(true);
                lbl.setMaxWidth(780);
                VBox.setMargin(lbl, new Insets(2, 0, 4, 0));
                container.getChildren().add(lbl);
                i++;
                continue;
            }

            // Таблица |---|
            if (line.trim().startsWith("|")) {
                // Собираем все строки таблицы (данные + определяем кол-во колонок)
                java.util.List<String[]> dataRows = new java.util.ArrayList<>();
                java.util.List<Boolean> headerFlags = new java.util.ArrayList<>();
                boolean isHeader = true;
                int colCount = 0;
                while (i < lines.length && lines[i].trim().startsWith("|")) {
                    String rowLine = lines[i].trim();
                    // Пропустить разделительную строку |---|
                    if (rowLine.replace("|", "").replace("-", "").replace(" ", "").replace(":", "").isEmpty()) {
                        isHeader = false;
                        i++;
                        continue;
                    }
                    String[] cells = rowLine.split("\\|");
                    // cells[0] пустой (до первого |), реальные ячейки с индекса 1
                    String[] realCells = new String[cells.length - 1];
                    for (int ci = 1; ci < cells.length; ci++) {
                        realCells[ci - 1] = cells[ci].trim();
                    }
                    if (realCells.length > colCount) colCount = realCells.length;
                    dataRows.add(realCells);
                    headerFlags.add(isHeader);
                    i++;
                }

                if (colCount == 0 || dataRows.isEmpty()) continue;

                // Строим таблицу как VBox строк (гарантирует корректные фоны)
                VBox tableBox = new VBox();
                tableBox.setStyle(
                    "-fx-background-color: -th-bg-card; -fx-background-radius: 8;" +
                    "-fx-border-color: -th-border; -fx-border-radius: 8; -fx-border-width: 1;"
                );
                tableBox.setMaxWidth(790);

                final int cols = colCount;
                int dataIdx = 0; // счётчик строк данных (без заголовка)

                for (int r = 0; r < dataRows.size(); r++) {
                    String[] rowCells = dataRows.get(r);
                    boolean isHdr = headerFlags.get(r);

                    HBox rowBox = new HBox();
                    rowBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    if (isHdr) {
                        rowBox.setStyle("-fx-background-color: -th-bg-secondary;" +
                            "-fx-border-color: transparent transparent -th-border transparent;" +
                            "-fx-border-width: 0 0 1 0;" +
                            (r == 0 ? "-fx-background-radius: 7 7 0 0;" : ""));
                    } else {
                        String bgColor = (dataIdx % 2 == 0) ? "-th-bg-card" : "-th-bg-hover";
                        String radius = (r == dataRows.size() - 1) ? "-fx-background-radius: 0 0 7 7;" : "";
                        rowBox.setStyle("-fx-background-color: " + bgColor + ";" + radius);
                        dataIdx++;
                    }

                    for (int c = 0; c < cols; c++) {
                        String cellText = (c < rowCells.length) ? rowCells[c] : "";
                        Label cell = new Label(stripInlineMarkdown(cellText));
                        cell.setWrapText(true);
                        cell.setPadding(new Insets(7, 12, 7, 12));
                        // Равная ширина колонок через binding
                        cell.prefWidthProperty().bind(tableBox.widthProperty().divide(cols));
                        cell.setMaxWidth(Double.MAX_VALUE);
                        if (isHdr) {
                            cell.setStyle("-fx-text-fill: -th-accent; -fx-font-weight: bold; -fx-font-size: 12px;");
                        } else {
                            cell.setStyle("-fx-text-fill: -th-text-primary; -fx-font-size: 12px;");
                        }
                        rowBox.getChildren().add(cell);
                    }

                    tableBox.getChildren().add(rowBox);
                }

                VBox.setMargin(tableBox, new Insets(4, 0, 10, 0));
                container.getChildren().add(tableBox);
                continue;
            }

            // Список (- или *) — с поддержкой кликабельных ссылок
            if (line.matches("^[-*] .*")) {
                String itemText = line.substring(2);
                Matcher linkMatcher = LINK_PATTERN.matcher(itemText);
                if (linkMatcher.find()) {
                    // Элемент содержит ссылку [текст](#якорь) — сделать кликабельным
                    String linkText = linkMatcher.group(1);
                    String anchor = linkMatcher.group(2);
                    Hyperlink link = new Hyperlink("  •  " + linkText);
                    // Подчёркивание включено сразу, чтобы пользователь видел: это ссылка
                    // (не просто фиолетовый текст). Курсор-рука усиливает affordance.
                    String baseStyle =
                        "-fx-text-fill: -th-accent; -fx-font-size: 13px; -fx-underline: true;" +
                        "-fx-border-color: transparent; -fx-padding: 1 0 1 8; -fx-cursor: hand;";
                    String hoverStyle =
                        "-fx-text-fill: -th-accent-hover; -fx-font-size: 13px; -fx-underline: true;" +
                        "-fx-border-color: transparent; -fx-padding: 1 0 1 8; -fx-cursor: hand;";
                    link.setStyle(baseStyle);
                    link.setOnMouseEntered(e -> link.setStyle(hoverStyle));
                    link.setOnMouseExited(e -> link.setStyle(baseStyle));
                    link.setOnAction(e -> scrollToAnchor(anchor));
                    link.setMaxWidth(780);
                    container.getChildren().add(link);
                } else {
                    String text = "  •  " + stripInlineMarkdown(itemText);
                    Label lbl = new Label(text);
                    lbl.setStyle("-fx-text-fill: -th-text-primary; -fx-font-size: 13px;");
                    lbl.setWrapText(true);
                    lbl.setMaxWidth(780);
                    VBox.setMargin(lbl, new Insets(1, 0, 1, 8));
                    container.getChildren().add(lbl);
                }
                i++;
                continue;
            }

            // Пустая строка
            if (line.trim().isEmpty()) {
                Region gap = new Region();
                gap.setPrefHeight(4);
                container.getChildren().add(gap);
                i++;
                continue;
            }

            // Обычный параграф
            Label lbl = new Label(stripInlineMarkdown(line));
            lbl.setStyle("-fx-text-fill: -th-text-primary; -fx-font-size: 13px;");
            lbl.setWrapText(true);
            lbl.setMaxWidth(800);
            container.getChildren().add(lbl);
            i++;
        }
    }

    /**
     * Убирает Markdown-разметку из строки для отображения в Label.
     * **bold** → bold, `code` → code, [text](url) → text
     */
    private static String stripInlineMarkdown(String text) {
        if (text == null) return "";
        // **bold** или __bold__
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        text = text.replaceAll("__(.+?)__", "$1");
        // *italic* или _italic_
        text = text.replaceAll("\\*(.+?)\\*", "$1");
        text = text.replaceAll("_(.+?)_", "$1");
        // `inline code`
        text = text.replaceAll("`(.+?)`", "$1");
        // [link text](url)
        text = text.replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1");
        return text;
    }
}
