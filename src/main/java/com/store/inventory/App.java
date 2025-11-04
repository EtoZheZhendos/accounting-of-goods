package com.store.inventory;

import com.store.inventory.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный класс JavaFX приложения
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Запуск приложения...");

            // Инициализация Hibernate
            HibernateUtil.getSessionFactory();
            logger.info("База данных инициализирована");

            // Загрузка главного окна
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainWindow.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("Учёт товаров в магазине");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

            logger.info("Приложение успешно запущено");

        } catch (Exception e) {
            logger.error("Ошибка при запуске приложения", e);
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        logger.info("Завершение работы приложения...");
        HibernateUtil.shutdown();
        logger.info("Приложение закрыто");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

