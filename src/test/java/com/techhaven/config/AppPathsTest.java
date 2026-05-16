package com.techhaven.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Юнит-тесты {@link AppPaths}: проверка резолва путей независимо от рабочей директории,
 * приоритета system-property override, поиска документации.
 */
class AppPathsTest {

    @TempDir Path tempDir;

    private String origDataDir;
    private String origDbPath;

    @BeforeEach
    void saveProps() {
        origDataDir = System.getProperty("app.data.dir");
        origDbPath = System.getProperty("db.path");
        // Чистим — surefire глобально ставит db.path=target/test.db через systemPropertyVariables,
        // что мешает тестировать default-поведение AppPaths.dbFile().
        System.clearProperty("app.data.dir");
        System.clearProperty("db.path");
        AppPaths.resetForTests();
    }

    @AfterEach
    void restoreProps() {
        if (origDataDir == null) System.clearProperty("app.data.dir");
        else System.setProperty("app.data.dir", origDataDir);
        if (origDbPath == null) System.clearProperty("db.path");
        else System.setProperty("db.path", origDbPath);
        AppPaths.resetForTests();
    }

    @Test
    @DisplayName("-Dapp.data.dir переопределяет default data-директорию")
    void dataDirOverrideViaSystemProperty() {
        System.setProperty("app.data.dir", tempDir.toString());
        Path result = AppPaths.dataDir();
        assertEquals(tempDir.toAbsolutePath().normalize(), result);
    }

    @Test
    @DisplayName("dataDir создаёт директорию, если она не существует")
    void dataDirCreatesDirectory() throws IOException {
        Path subdir = tempDir.resolve("new-data-" + UUID.randomUUID());
        System.setProperty("app.data.dir", subdir.toString());
        assertTrue(!Files.exists(subdir));
        Path result = AppPaths.dataDir();
        assertTrue(Files.exists(result), "Директория должна быть создана");
        assertTrue(Files.isDirectory(result));
    }

    @Test
    @DisplayName("dbFile() = dataDir/digitalhub.db по умолчанию")
    void dbFileDefault() {
        System.setProperty("app.data.dir", tempDir.toString());
        Path db = AppPaths.dbFile();
        assertEquals(tempDir.resolve("digitalhub.db").toAbsolutePath().normalize(), db);
    }

    @Test
    @DisplayName("-Ddb.path переопределяет dbFile независимо от data dir")
    void dbPathOverride() {
        System.setProperty("app.data.dir", tempDir.toString());
        Path customDb = tempDir.resolve("custom.db");
        System.setProperty("db.path", customDb.toString());
        Path db = AppPaths.dbFile();
        assertEquals(customDb.toAbsolutePath().normalize(), db);
    }

    @Test
    @DisplayName("productImagesDir() резолвится в dataDir/product_images и создаётся")
    void productImagesDir() {
        System.setProperty("app.data.dir", tempDir.toString());
        Path imgs = AppPaths.productImagesDir();
        assertEquals(tempDir.resolve("product_images").toAbsolutePath().normalize(), imgs);
        assertTrue(Files.exists(imgs), "Директория product_images должна быть создана");
    }

    @Test
    @DisplayName("findDocumentFile находит файл в data-директории")
    void findDocumentInDataDir() throws IOException {
        System.setProperty("app.data.dir", tempDir.toString());
        Path doc = tempDir.resolve("TestManual.md");
        Files.writeString(doc, "# Test");
        Path found = AppPaths.findDocumentFile("TestManual.md");
        assertNotNull(found);
        assertEquals(doc.toAbsolutePath().normalize(), found.toAbsolutePath().normalize());
    }

    @Test
    @DisplayName("findDocumentFile находит файл в родительской директории")
    void findDocumentInParent() throws IOException {
        Path child = Files.createDirectories(tempDir.resolve("child"));
        System.setProperty("app.data.dir", child.toString());
        Path doc = tempDir.resolve("ParentManual.md");
        Files.writeString(doc, "# Parent");
        Path found = AppPaths.findDocumentFile("ParentManual.md");
        assertNotNull(found);
        assertEquals(doc.toAbsolutePath().normalize(), found.toAbsolutePath().normalize());
    }

    @Test
    @DisplayName("findDocumentFile возвращает null если файл не найден")
    void findDocumentNotFound() {
        System.setProperty("app.data.dir", tempDir.toString());
        Path found = AppPaths.findDocumentFile("NonExistent.md");
        assertNull(found);
    }

    @Test
    @DisplayName("dataDir() возвращает абсолютный нормализованный путь")
    void dataDirIsAbsolute() {
        System.setProperty("app.data.dir", tempDir.toString());
        Path result = AppPaths.dataDir();
        assertTrue(result.isAbsolute(), "Путь должен быть абсолютным");
    }

    @Test
    @DisplayName("Повторный вызов dataDir() возвращает закэшированное значение")
    void dataDirIsCached() {
        System.setProperty("app.data.dir", tempDir.toString());
        Path first = AppPaths.dataDir();
        Path second = AppPaths.dataDir();
        assertEquals(first, second);
    }
}
