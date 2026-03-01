package com.techhaven;

import java.util.logging.Logger;

import com.techhaven.config.DatabaseManager;
import com.techhaven.view.LoginView;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static Stage primaryStage;
    private static Image appIcon;

    @Override
    public void start(Stage stage) {
        // Инициализация БД
        DatabaseManager.getInstance();

        // Загрузка иконки — пробуем несколько источников
        appIcon = loadAppIcon();

        // Окно входа — без рамки (UNDECORATED)
        primaryStage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        if (appIcon != null) {
            stage.getIcons().add(appIcon);
        }

        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 500, 620);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

        stage.setScene(scene);
        stage.setResizable(false);
        centerOnScreen(stage, 500, 620);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Сцена для авторизации/регистрации (окно без рамки)
     */
    public static void setScene(javafx.scene.Parent root, double width, double height, boolean resizable) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(MainApp.class.getResource("/styles/dark-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(resizable);
        centerOnScreen(primaryStage, width, height);
    }

    /**
     * Переход к основному приложению — пересоздание окна с рамкой (DECORATED)
     */
    public static void setScene(javafx.scene.Parent root) {
        // Закрываем окно без рамки
        primaryStage.close();

        // Создаём новое окно без рамки (серый заголовок Windows убран)
        Stage newStage = new Stage(StageStyle.UNDECORATED);
        newStage.setMinWidth(1280);
        newStage.setMinHeight(720);
        newStage.setResizable(true);

        if (appIcon != null) {
            newStage.getIcons().add(appIcon);
        }

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(MainApp.class.getResource("/styles/dark-theme.css").toExternalForm());
        newStage.setScene(scene);
        centerOnScreen(newStage, 1280, 720);

        primaryStage = newStage;
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Центрирование окна на экране
     */
    private static void centerOnScreen(Stage stage, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - width) / 2);
        stage.setY((screenBounds.getHeight() - height) / 2);
    }

    /**
     * Возврат к экрану входа (UNDECORATED окно, как при первом запуске)
     */
    public static void showLogin() {
        primaryStage.close();

        Stage loginStage = new Stage(StageStyle.UNDECORATED);
        if (appIcon != null) {
            loginStage.getIcons().add(appIcon);
        }

        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 500, 620);
        scene.getStylesheets().add(MainApp.class.getResource("/styles/dark-theme.css").toExternalForm());

        loginStage.setScene(scene);
        loginStage.setResizable(false);
        centerOnScreen(loginStage, 500, 620);

        primaryStage = loginStage;
        primaryStage.show();
    }

    /**
     * Получить иконку приложения для использования в диалогах и формах входа
     */
    public static Image getAppIcon() {
        return appIcon;
    }

    /**
     * Загружает логотип из classpath (resources/images/logo.jpg).
     */
    private Image loadAppIcon() {
        try {
            var stream = getClass().getResourceAsStream("/images/logo.jpg");
            if (stream != null) {
                Image img = new Image(stream);
                if (!img.isError()) return img;
            }
        } catch (Exception e) {
            LOGGER.warning(() -> "Не удалось загрузить логотип: " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
