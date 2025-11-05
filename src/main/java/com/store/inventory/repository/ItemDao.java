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
 * 
 * <p>Предоставляет методы доступа к данным товарных позиций.
 * Расширяет базовый GenericDao методами поиска по номенклатуре, статусу,
 * полке, партии, а также специализированными методами для получения остатков,
 * просроченных и скоро просрочивающихся товаров.</p>
 */
public class ItemDao extends GenericDao<Item, Long> {

    /**
     * Создает экземпляр DAO для работы с товарными позициями
     */
    public ItemDao() {
        super(Item.class);
    }

    /**
     * Возвращает список товарных позиций указанной номенклатуры
     * 
     * <p>Результаты сортируются по дате создания в обратном порядке.</p>
     * 
     * @param nomenclature номенклатура товара
     * @return список товарных позиций номенклатуры
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает список товарных позиций с указанным статусом
     * 
     * <p>Результаты сортируются по дате создания в обратном порядке.</p>
     * 
     * @param status статус товарной позиции
     * @return список товарных позиций с указанным статусом
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает список товарных позиций на указанной полке
     * 
     * <p>Результаты сортируются по названию номенклатуры.</p>
     * 
     * @param shelf полка
     * @return список товарных позиций на полке
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает список товарных позиций из указанной партии
     * 
     * <p>Результаты сортируются по дате создания в обратном порядке.</p>
     * 
     * @param batchNumber номер партии
     * @return список товарных позиций из партии
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает общее количество товара по номенклатуре и статусу
     * 
     * <p>Суммирует количество всех товарных позиций указанной номенклатуры
     * с указанным статусом. Если товаров нет, возвращает 0.</p>
     * 
     * @param nomenclature номенклатура товара
     * @param status статус товарной позиции
     * @return общее количество товара
     * @throws RuntimeException если произошла ошибка при подсчёте
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
     * Возвращает остатки товаров с разбивкой по складам
     * 
     * <p>Для каждой комбинации номенклатура-склад возвращает суммарное количество
     * товаров в статусе IN_STOCK. Результаты сортируются по артикулу номенклатуры
     * и названию склада.</p>
     * 
     * @return список массивов объектов [nomenclature, warehouse, quantity]
     * @throws RuntimeException если произошла ошибка при получении остатков
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
     * Возвращает список просроченных товаров
     * 
     * <p>Включает товары в статусе IN_STOCK, у которых срок годности истёк.
     * Результаты сортируются по дате истечения срока годности.</p>
     * 
     * @return список просроченных товаров
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает список товаров, срок годности которых скоро истекает
     * 
     * <p>Включает товары в статусе IN_STOCK, у которых срок годности истекает
     * в течение указанного количества дней. Результаты сортируются
     * по дате истечения срока годности.</p>
     * 
     * @param daysBeforeExpiry количество дней до истечения срока
     * @return список товаров с истекающим сроком годности
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает список доступных товаров по номенклатуре и складу
     * 
     * <p>Включает только товары в статусе IN_STOCK с количеством больше нуля,
     * расположенные на указанном складе. Использует EAGER fetch для номенклатуры,
     * полки и склада для предотвращения LazyInitializationException.
     * Результаты сортируются по дате создания (FIFO).</p>
     * 
     * @param nomenclature номенклатура товара
     * @param warehouse склад
     * @return список доступных товарных позиций
     * @throws RuntimeException если произошла ошибка при поиске
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
