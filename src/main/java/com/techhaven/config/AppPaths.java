package com.techhaven.config;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.logging.Logger;

/**
 * Единая точка определения путей приложения.
 *
 * <p>Все runtime-ресурсы (БД, загруженные изображения товаров, локально
 * сохраняемые файлы) располагаются в <b>data-директории</b> приложения,
 * которая определяется по приоритету:</p>
 *
 * <ol>
 *   <li>System-property <code>-Dapp.data.dir=&lt;path&gt;</code> — явно заданная
 *       пользователем директория (абсолютный или относительный путь).</li>
 *   <li>Папка рядом с запускаемым JAR'ом — определяется через
 *       {@link CodeSource#getLocation()}. Это позволяет приложению работать
 *       из любой точки файловой системы без зависимости от рабочей директории.</li>
 *   <li>Fallback: текущая рабочая директория (<code>user.dir</code>) —
 *       используется при запуске из IDE или из директории проекта
 *       (когда CodeSource указывает на target/classes, не на JAR).</li>
 * </ol>
 *
 * <p>Для поиска <b>documentation-файлов</b> (UserManual.md, AdminManual.md)
 * используется {@link #findDocumentFile(String)} с цепочкой:</p>
 * <ol>
 *   <li>Data-директория (рядом с JAR / явно заданная).</li>
 *   <li>Родительская директория data-директории — для дистрибутивов,
 *       где JAR в <code>dist/</code>, а manuals в корне проекта.</li>
 *   <li>Classpath (<code>/help/</code>) — fallback на ресурсы, упакованные в JAR.</li>
 * </ol>
 */
public final class AppPaths {

    private static final Logger LOGGER = Logger.getLogger(AppPaths.class.getName());

    private static final String PROP_DATA_DIR = "app.data.dir";
    private static final String DEFAULT_DB_FILE = "digitalhub.db";
    private static final String DEFAULT_IMAGES_DIR = "product_images";

    /** Кэшированная data-директория. Определяется лениво при первом обращении. */
    private static volatile Path dataDir;

    private AppPaths() {}

    /**
     * Корневая директория для пользовательских данных приложения
     * (БД, загруженные изображения товаров). Создаётся при необходимости.
     */
    public static Path dataDir() {
        Path cached = dataDir;
        if (cached != null) return cached;
        synchronized (AppPaths.class) {
            if (dataDir != null) return dataDir;
            dataDir = resolveDataDir();
            try {
                Files.createDirectories(dataDir);
            } catch (Exception e) {
                LOGGER.warning(() -> "Не удалось создать data-директорию " + dataDir + ": " + e.getMessage());
            }
            return dataDir;
        }
    }

    /** Путь к файлу БД SQLite. */
    public static Path dbFile() {
        String override = System.getProperty("db.path");
        if (override != null && !override.isBlank()) {
            return Paths.get(override).toAbsolutePath().normalize();
        }
        return dataDir().resolve(DEFAULT_DB_FILE);
    }

    /** Директория для пользовательских изображений товаров. */
    public static Path productImagesDir() {
        Path dir = dataDir().resolve(DEFAULT_IMAGES_DIR);
        try {
            Files.createDirectories(dir);
        } catch (Exception ignored) {
            // best-effort
        }
        return dir;
    }

    /**
     * Найти файл документации (UserManual.md, AdminManual.md) по имени.
     *
     * <p>Поиск выполняется в следующем порядке:</p>
     * <ol>
     *   <li>В {@link #dataDir()}.</li>
     *   <li>В родительской директории data-директории (для layouts типа
     *       <code>project/dist/&lt;jar+db&gt;</code>, где manuals в
     *       <code>project/</code>).</li>
     *   <li>В текущей рабочей директории.</li>
     * </ol>
     *
     * @return найденный путь или {@code null} (caller должен fall back на classpath)
     */
    public static Path findDocumentFile(String filename) {
        Path[] candidates = {
            dataDir().resolve(filename),
            dataDir().getParent() != null ? dataDir().getParent().resolve(filename) : null,
            Paths.get(filename).toAbsolutePath()
        };
        for (Path p : candidates) {
            if (p != null && Files.exists(p) && Files.isReadable(p)) {
                return p;
            }
        }
        return null;
    }

    // ─── Internals ──────────────────────────────────────────────────────────

    private static Path resolveDataDir() {
        // 1. -Dapp.data.dir override
        String override = System.getProperty(PROP_DATA_DIR);
        if (override != null && !override.isBlank()) {
            return Paths.get(override).toAbsolutePath().normalize();
        }

        // 2. Папка рядом с JAR (если запущены из JAR'а)
        Path jarLocation = locateJarDirectory();
        if (jarLocation != null) {
            return jarLocation;
        }

        // 3. Fallback — рабочая директория
        return Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
    }

    /**
     * Определяет директорию, в которой лежит запускаемый JAR.
     * При запуске из IDE (CodeSource → target/classes) возвращает {@code null},
     * чтобы fallback'нуться на user.dir.
     */
    private static Path locateJarDirectory() {
        try {
            CodeSource src = AppPaths.class.getProtectionDomain().getCodeSource();
            if (src == null) return null;
            URL url = src.getLocation();
            if (url == null) return null;
            File f = new File(url.toURI());
            if (f.isFile() && f.getName().endsWith(".jar")) {
                return f.getParentFile().toPath().toAbsolutePath().normalize();
            }
            // CodeSource указывает на директорию (target/classes) — это запуск из IDE/тестов
            return null;
        } catch (URISyntaxException | IllegalArgumentException e) {
            LOGGER.fine(() -> "Не удалось определить путь к JAR: " + e.getMessage());
            return null;
        }
    }

    /** Сбросить кэш — для тестов. */
    static void resetForTests() {
        dataDir = null;
    }
}
