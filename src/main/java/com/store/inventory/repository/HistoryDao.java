package com.store.inventory.repository;

import com.store.inventory.domain.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO для работы с историей операций
 */
public class HistoryDao extends GenericDao<History, Long> {

    public HistoryDao() {
        super(History.class);
    }

    /**
     * Получить историю по товарной позиции
     */
    public List<History> findByItem(Item item) {
        try (Session session = getSession()) {
            String hql = "FROM History WHERE item = :item ORDER BY operationDate DESC";
            Query<History> query = session.createQuery(hql, History.class);
            query.setParameter("item", item);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении истории товарной позиции", e);
            throw new RuntimeException("Ошибка при получении истории: " + e.getMessage(), e);
        }
    }

    /**
     * Получить историю по документу
     */
    public List<History> findByDocument(Document document) {
        try (Session session = getSession()) {
            String hql = "FROM History WHERE document = :document ORDER BY operationDate";
            Query<History> query = session.createQuery(hql, History.class);
            query.setParameter("document", document);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении истории документа", e);
            throw new RuntimeException("Ошибка при получении истории: " + e.getMessage(), e);
        }
    }

    /**
     * Получить историю по типу операции
     */
    public List<History> findByOperationType(OperationType operationType) {
        try (Session session = getSession()) {
            String hql = "FROM History WHERE operationType = :type ORDER BY operationDate DESC";
            Query<History> query = session.createQuery(hql, History.class);
            query.setParameter("type", operationType);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении истории по типу операции", e);
            throw new RuntimeException("Ошибка при получении истории: " + e.getMessage(), e);
        }
    }

    /**
     * Получить историю за период
     */
    public List<History> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = getSession()) {
            String hql = "FROM History WHERE operationDate BETWEEN :startDate AND :endDate ORDER BY operationDate DESC";
            Query<History> query = session.createQuery(hql, History.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении истории за период", e);
            throw new RuntimeException("Ошибка при получении истории: " + e.getMessage(), e);
        }
    }

    /**
     * Получить историю перемещений с/на полку
     */
    public List<History> findByShelf(Shelf shelf) {
        try (Session session = getSession()) {
            String hql = "FROM History WHERE fromShelf = :shelf OR toShelf = :shelf ORDER BY operationDate DESC";
            Query<History> query = session.createQuery(hql, History.class);
            query.setParameter("shelf", shelf);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении истории по полке", e);
            throw new RuntimeException("Ошибка при получении истории: " + e.getMessage(), e);
        }
    }
}

