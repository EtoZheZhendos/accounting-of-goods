package com.store.inventory.repository;

import com.store.inventory.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Базовый DAO класс с общими CRUD операциями
 * @param <T> тип сущности
 * @param <ID> тип идентификатора
 */
public abstract class GenericDao<T, ID> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Class<T> entityClass;

    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Сохранить или обновить сущность
     */
    public T save(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
            logger.debug("Сущность сохранена: {}", entity);
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при сохранении сущности", e);
            throw new RuntimeException("Ошибка при сохранении: " + e.getMessage(), e);
        }
    }

    /**
     * Найти сущность по ID
     */
    public Optional<T> findById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.get(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            logger.error("Ошибка при поиске сущности по ID: {}", id, e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Получить все сущности
     */
    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM " + entityClass.getSimpleName();
            Query<T> query = session.createQuery(hql, entityClass);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении всех сущностей", e);
            throw new RuntimeException("Ошибка при получении списка: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить сущность
     */
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
            logger.debug("Сущность удалена: {}", entity);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при удалении сущности", e);
            throw new RuntimeException("Ошибка при удалении: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить сущность по ID
     */
    public void deleteById(ID id) {
        Optional<T> entity = findById(id);
        entity.ifPresent(this::delete);
    }

    /**
     * Проверить существование сущности по ID
     */
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    /**
     * Получить количество всех сущностей
     */
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(*) FROM " + entityClass.getSimpleName();
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Ошибка при подсчёте сущностей", e);
            throw new RuntimeException("Ошибка при подсчёте: " + e.getMessage(), e);
        }
    }

    /**
     * Получить Session для выполнения пользовательских запросов
     */
    protected Session getSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }
}

