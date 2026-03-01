package com.techhaven;

/**
 * Лаунчер для запуска JavaFX-приложения из fat JAR.
 * JavaFX требует, чтобы main-класс НЕ наследовал Application,
 * иначе модульная система блокирует запуск из classpath.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
