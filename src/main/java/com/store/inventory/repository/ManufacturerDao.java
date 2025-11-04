package com.store.inventory.repository;

import com.store.inventory.domain.Manufacturer;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;

/**
 * DAO для работы с производителями
 */
public class ManufacturerDao extends GenericDao<Manufacturer, Long> {

    public ManufacturerDao() {
        super(Manufacturer.class);
    }

    /**
     * Найти производителя по имени
     */
    public Optional<Manufacturer> findByName(String name) {
        try (Session session = getSession()) {
            String hql = "FROM Manufacturer WHERE name = :name";
            Query<Manufacturer> query = session.createQuery(hql, Manufacturer.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Ошибка при поиске производителя по имени: {}", name, e);
            throw new RuntimeException("Ошибка при поиске производителя: " + e.getMessage(), e);
        }
    }

    /**
     * Получить производителей по стране
     */
    public java.util.List<Manufacturer> findByCountry(String country) {
        try (Session session = getSession()) {
            String hql = "FROM Manufacturer WHERE country = :country ORDER BY name";
            Query<Manufacturer> query = session.createQuery(hql, Manufacturer.class);
            query.setParameter("country", country);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске производителей по стране: {}", country, e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }
}

