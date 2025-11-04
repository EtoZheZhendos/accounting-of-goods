package com.store.inventory.repository;

import com.store.inventory.domain.Item;
import com.store.inventory.domain.ItemStatus;
import com.store.inventory.domain.Nomenclature;
import com.store.inventory.domain.Shelf;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * DAO для работы с товарными позициями
 */
public class ItemDao extends GenericDao<Item, Long> {

    public ItemDao() {
        super(Item.class);
    }

    /**
     * Найти товары по номенклатуре
     */
    public List<Item> findByNomenclature(Nomenclature nomenclature) {
        try (Session session = getSession()) {
            String hql = "FROM Item WHERE nomenclature = :nomenclature ORDER BY createdAt DESC";
            Query<Item> query = session.createQuery(hql, Item.class);
            query.setParameter("nomenclature", nomenclature);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске товаров по номенклатуре", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Найти товары по статусу
     */
    public List<Item> findByStatus(ItemStatus status) {
        try (Session session = getSession()) {
            String hql = "FROM Item WHERE status = :status ORDER BY createdAt DESC";
            Query<Item> query = session.createQuery(hql, Item.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске товаров по статусу", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Найти товары на полке
     */
    public List<Item> findByShelf(Shelf shelf) {
        try (Session session = getSession()) {
            String hql = "FROM Item WHERE currentShelf = :shelf ORDER BY nomenclature.name";
            Query<Item> query = session.createQuery(hql, Item.class);
            query.setParameter("shelf", shelf);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске товаров на полке", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Найти товары по номеру партии
     */
    public List<Item> findByBatchNumber(String batchNumber) {
        try (Session session = getSession()) {
            String hql = "FROM Item WHERE batchNumber = :batchNumber ORDER BY createdAt DESC";
            Query<Item> query = session.createQuery(hql, Item.class);
            query.setParameter("batchNumber", batchNumber);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске товаров по номеру партии: {}", batchNumber, e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Получить общее количество товара по номенклатуре и статусу
     */
    public BigDecimal getTotalQuantityByNomenclatureAndStatus(Nomenclature nomenclature, ItemStatus status) {
        try (Session session = getSession()) {
            String hql = "SELECT COALESCE(SUM(i.quantity), 0) FROM Item i WHERE i.nomenclature = :nomenclature AND i.status = :status";
            Query<BigDecimal> query = session.createQuery(hql, BigDecimal.class);
            query.setParameter("nomenclature", nomenclature);
            query.setParameter("status", status);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Ошибка при подсчёте общего количества товара", e);
            throw new RuntimeException("Ошибка при подсчёте: " + e.getMessage(), e);
        }
    }
    
    /**
     * Получить остатки товаров с разбивкой по складам
     * @return список объектов [nomenclature, warehouse, quantity]
     */
    public List<Object[]> getStockByWarehouse() {
        try (Session session = getSession()) {
            String hql = """
                SELECT i.nomenclature, s.warehouse, SUM(i.quantity)
                FROM Item i
                JOIN i.currentShelf s
                JOIN s.warehouse
                WHERE i.status = 'IN_STOCK' AND i.quantity > 0
                GROUP BY i.nomenclature, s.warehouse
                ORDER BY i.nomenclature.article, s.warehouse.name
                """;
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении остатков по складам", e);
            throw new RuntimeException("Ошибка при получении остатков: " + e.getMessage(), e);
        }
    }

    /**
     * Получить просроченные товары
     */
    public List<Item> findExpiredItems() {
        try (Session session = getSession()) {
            String hql = "FROM Item WHERE expiryDate < CURRENT_DATE AND status = 'IN_STOCK' ORDER BY expiryDate";
            Query<Item> query = session.createQuery(hql, Item.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске просроченных товаров", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Получить товары, срок годности которых истекает скоро
     */
    public List<Item> findExpiringItems(int daysBeforeExpiry) {
        try (Session session = getSession()) {
            String hql = """
                FROM Item
                WHERE expiryDate IS NOT NULL
                AND expiryDate > CURRENT_DATE
                AND expiryDate <= (CURRENT_DATE + :days)
                AND status = 'IN_STOCK'
                ORDER BY expiryDate
                """;
            Query<Item> query = session.createQuery(hql, Item.class);
            query.setParameter("days", daysBeforeExpiry);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске товаров с истекающим сроком", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Найти доступные товары по номенклатуре и складу
     */
    public List<Item> findAvailableByNomenclatureAndWarehouse(Nomenclature nomenclature, com.store.inventory.domain.Warehouse warehouse) {
        try (Session session = getSession()) {
            String hql = """
                SELECT i FROM Item i
                LEFT JOIN FETCH i.nomenclature
                LEFT JOIN FETCH i.currentShelf s
                LEFT JOIN FETCH s.warehouse
                WHERE i.nomenclature = :nomenclature
                AND s.warehouse = :warehouse
                AND i.status = 'IN_STOCK'
                AND i.quantity > 0
                ORDER BY i.createdAt
                """;
            Query<Item> query = session.createQuery(hql, Item.class);
            query.setParameter("nomenclature", nomenclature);
            query.setParameter("warehouse", warehouse);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске товаров по номенклатуре и складу", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }
}

