package com.store.inventory.repository;

import com.store.inventory.domain.Warehouse;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO для работы со складами
 */
public class WarehouseDao extends GenericDao<Warehouse, Long> {

    public WarehouseDao() {
        super(Warehouse.class);
    }

    /**
     * Найти склад по имени
     */
    public Optional<Warehouse> findByName(String name) {
        try (Session session = getSession()) {
            String hql = "FROM Warehouse WHERE name = :name";
            Query<Warehouse> query = session.createQuery(hql, Warehouse.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Ошибка при поиске склада по имени: {}", name, e);
            throw new RuntimeException("Ошибка при поиске склада: " + e.getMessage(), e);
        }
    }

    /**
     * Получить все активные склады
     */
    public List<Warehouse> findAllActive() {
        try (Session session = getSession()) {
            String hql = "FROM Warehouse WHERE isActive = true ORDER BY name";
            Query<Warehouse> query = session.createQuery(hql, Warehouse.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении активных складов", e);
            throw new RuntimeException("Ошибка при получении списка: " + e.getMessage(), e);
        }
    }
}

