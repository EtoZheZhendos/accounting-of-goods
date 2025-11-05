package com.store.inventory.repository;

import com.store.inventory.domain.Warehouse;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO для работы со складами
 * 
 * <p>Предоставляет методы доступа к данным складов.
 * Расширяет базовый GenericDao методами поиска по имени
 * и получения списка активных складов.</p>
 */
public class WarehouseDao extends GenericDao<Warehouse, Long> {

    /**
     * Создает экземпляр DAO для работы со складами
     */
    public WarehouseDao() {
        super(Warehouse.class);
    }

    /**
     * Находит склад по точному совпадению имени
     * 
     * @param name название склада
     * @return Optional с найденным складом или пустой Optional
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает список всех активных складов
     * 
     * <p>Результаты сортируются по названию склада.</p>
     * 
     * @return список активных складов
     * @throws RuntimeException если произошла ошибка при получении списка
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

