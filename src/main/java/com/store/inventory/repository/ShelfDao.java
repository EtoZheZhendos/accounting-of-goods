package com.store.inventory.repository;

import com.store.inventory.domain.Shelf;
import com.store.inventory.domain.Warehouse;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с полками складов
 * 
 * <p>Предоставляет методы доступа к данным полок.
 * Расширяет базовый GenericDao методами поиска по складу и коду,
 * а также получения списков активных полок.</p>
 */
public class ShelfDao extends GenericDao<Shelf, Long> {

    /**
     * Создает экземпляр DAO для работы с полками
     */
    public ShelfDao() {
        super(Shelf.class);
    }

    /**
     * Находит полку по складу и уникальному коду
     * 
     * <p>Код полки уникален в пределах одного склада.</p>
     * 
     * @param warehouse склад
     * @param code код полки
     * @return Optional с найденной полкой или пустой Optional
     * @throws RuntimeException если произошла ошибка при поиске
     */
    public Optional<Shelf> findByWarehouseAndCode(Warehouse warehouse, String code) {
        try (Session session = getSession()) {
            String hql = "FROM Shelf WHERE warehouse = :warehouse AND code = :code";
            Query<Shelf> query = session.createQuery(hql, Shelf.class);
            query.setParameter("warehouse", warehouse);
            query.setParameter("code", code);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Ошибка при поиске полки по складу и коду", e);
            throw new RuntimeException("Ошибка при поиске полки: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает список всех полок указанного склада
     * 
     * <p>Результаты сортируются по коду полки.</p>
     * 
     * @param warehouse склад
     * @return список всех полок склада
     * @throws RuntimeException если произошла ошибка при получении списка
     */
    public List<Shelf> findByWarehouse(Warehouse warehouse) {
        try (Session session = getSession()) {
            String hql = "FROM Shelf WHERE warehouse = :warehouse ORDER BY code";
            Query<Shelf> query = session.createQuery(hql, Shelf.class);
            query.setParameter("warehouse", warehouse);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении полок склада", e);
            throw new RuntimeException("Ошибка при получении списка: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает список активных полок указанного склада
     * 
     * <p>Результаты сортируются по коду полки.</p>
     * 
     * @param warehouse склад
     * @return список активных полок склада
     * @throws RuntimeException если произошла ошибка при получении списка
     */
    public List<Shelf> findActiveByWarehouse(Warehouse warehouse) {
        try (Session session = getSession()) {
            String hql = "FROM Shelf WHERE warehouse = :warehouse AND isActive = true ORDER BY code";
            Query<Shelf> query = session.createQuery(hql, Shelf.class);
            query.setParameter("warehouse", warehouse);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении активных полок склада", e);
            throw new RuntimeException("Ошибка при получении списка: " + e.getMessage(), e);
        }
    }
}
