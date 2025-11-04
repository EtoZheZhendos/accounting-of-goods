package com.store.inventory.repository;

import com.store.inventory.domain.Shelf;
import com.store.inventory.domain.Warehouse;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с полками
 */
public class ShelfDao extends GenericDao<Shelf, Long> {

    public ShelfDao() {
        super(Shelf.class);
    }

    /**
     * Найти полку по коду на складе
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
     * Получить все полки склада
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
     * Получить активные полки склада
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

