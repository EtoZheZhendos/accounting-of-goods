package com.store.inventory.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Утилита для работы с Hibernate SessionFactory
 * Реализует паттерн Singleton
 */
public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    private HibernateUtil() {
        // Приватный конструктор для Singleton
    }

    /**
     * Получить экземпляр SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    try {
                        logger.info("Инициализация Hibernate SessionFactory...");
                        
                        // Создание директории для базы данных, если её нет
                        File dataDir = new File("./data");
                        if (!dataDir.exists()) {
                            dataDir.mkdirs();
                            logger.info("Создана директория для базы данных: {}", dataDir.getAbsolutePath());
                        }

                        // Создание SessionFactory из hibernate.cfg.xml
                        Configuration configuration = new Configuration();
                        configuration.configure("hibernate.cfg.xml");
                        
                        sessionFactory = configuration.buildSessionFactory();
                        
                        logger.info("Hibernate SessionFactory успешно инициализирована");
                    } catch (Exception e) {
                        logger.error("Ошибка инициализации SessionFactory", e);
                        throw new ExceptionInInitializerError("Не удалось создать SessionFactory: " + e.getMessage());
                    }
                }
            }
        }
        return sessionFactory;
    }

    /**
     * Закрыть SessionFactory
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Закрытие Hibernate SessionFactory...");
            sessionFactory.close();
            logger.info("Hibernate SessionFactory закрыта");
        }
    }

    /**
     * Проверить, открыта ли SessionFactory
     */
    public static boolean isOpen() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }
}

