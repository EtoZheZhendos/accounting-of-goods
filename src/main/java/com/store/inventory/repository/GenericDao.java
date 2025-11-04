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
 * Базовый DAO (Data Access Object) класс с общими CRUD операциями
 * 
 * <p>Предоставляет стандартные методы для работы с базой данных:
 * создание, чтение, обновление и удаление сущностей.
 * Использует Hibernate для взаимодействия с БД.</p>
 * 
 * @param <T> тип сущности
 * @param <ID> тип идентификатора сущности
 */
public abstract class GenericDao<T, ID> {

    /** Логгер для записи событий и ошибок */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /** Класс сущности для работы с Hibernate */
    protected final Class<T> entityClass;

    /**
     * Создает экземпляр DAO для указанного класса сущности
     * 
     * @param entityClass класс сущности
     */
    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Сохраняет или обновляет сущность в базе данных
     * 
     * <p>Операция выполняется в транзакции. При ошибке происходит откат.</p>
     * 
     * @param entity сущность для сохранения
     * @return сохраненная сущность
     * @throws RuntimeException если произошла ошибка при сохранении
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
     * Находит сущность по идентификатору
     * 
     * @param id идентификатор сущности
     * @return Optional с найденной сущностью или пустой Optional
     * @throws RuntimeException если произошла ошибка при поиске
     */
    public Optional<T> findById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.get(entityClass, id);
            return Optional.ofnullable(entity);
        } catch (Exception e) {
            logger.error("Ошибка при поиске сущности по ID: {}", id, e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает список всех сущностей из базы данных
     * 
     * @return список всех сущностей
     * @throws RuntimeException если произошла ошибка при получении списка
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
     * Удаляет сущность из базы данных
     * 
     * <p>Операция выполняется в транзакции. При ошибке происходит откат.</p>
     * 
     * @param entity сущность для удаления
     * @throws RuntimeException если произошла ошибка при удалении
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
     * Удаляет сущность по идентификатору
     * 
     * <p>Сначала находит сущность по ID, затем удаляет её, если она существует.</p>
     * 
     * @param id идентификатор сущности для удаления
     */
    public void deleteById(ID id) {
        Optional<T> entity = findById(id);
        entity.ifPresent(this::delete);
    }

    /**
     * Проверяет существование сущности с указанным идентификатором
     * 
     * @param id идентификатор сущности
     * @return true если сущность существует, false в противном случае
     */
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    /**
     * Возвращает общее количество сущностей в базе данных
     * 
     * @return количество сущностей
     * @throws RuntimeException если произошла ошибка при подсчёте
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
     * Возвращает новую Hibernate сессию для выполнения пользовательских запросов
     * 
     * <p>Используется в наследниках для специфичных операций с БД.
     * Вызывающий код ответственен за закрытие сессии.</p>
     * 
     * @return новая Hibernate Session
     */
    protected Session getSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }
}

