package com.store.inventory.util;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Утилита для инициализации базы данных SQL-скриптами
 */
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    /**
     * Выполняет SQL-скрипт из ресурсов
     */
    public static void executeSqlScript(String scriptPath) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Читаем SQL-скрипт из ресурсов
            InputStream inputStream = DatabaseInitializer.class
                    .getClassLoader()
                    .getResourceAsStream(scriptPath);

            if (inputStream == null) {
                logger.warn("SQL-скрипт не найден: {}", scriptPath);
                return;
            }

            String sql = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Разделяем скрипт на отдельные команды
            String[] commands = sql.split(";");

            for (String command : commands) {
                String trimmedCommand = command.trim();
                if (!trimmedCommand.isEmpty() && !trimmedCommand.startsWith("--")) {
                    session.createNativeQuery(trimmedCommand, Object.class).executeUpdate();
                }
            }

            transaction.commit();
            logger.info("SQL-скрипт успешно выполнен: {}", scriptPath);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при выполнении SQL-скрипта: " + scriptPath, e);
        }
    }

    /**
     * Инициализирует базу данных начальными данными
     */
    public static void initializeDatabase() {
        logger.info("Начало инициализации базы данных...");
        
        // Выполняем миграции
        executeSqlScript("db/migration/V1__initial_schema.sql");
        executeSqlScript("db/migration/V2__initial_data.sql");
        
        logger.info("Инициализация базы данных завершена");
    }
}

