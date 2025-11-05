package com.store.inventory.repository;

import com.store.inventory.domain.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO для работы с историей операций с товарами
 * 
 * <p>Предоставляет методы доступа к данным истории операций.
 * Расширяет базовый GenericDao методами поиска по товарной позиции,
 * документу, типу операции, периоду и полке.</p>
 */
public class HistoryDao extends GenericDao<History, Long> {

    /**
     * Создает экземпляр DAO для работы с историей операций
     */
    public HistoryDao() {
        super(History.class);
    }

    /**
     * Возвращает историю операций для указанной товарной позиции
     * 
     * <p>Результаты сортируются по дате операции в обратном порядке.</p>
     * 
     * @param item товарная позиция
     * @return список записей истории товарной позиции
     * @throws RuntimeException если произошла ошибка при получении истории
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
     * Возвращает историю операций для указанного документа
     * 
     * <p>Результаты сортируются по дате операции в прямом порядке.</p>
     * 
     * @param document документ
     * @return список записей истории документа
     * @throws RuntimeException если произошла ошибка при получении истории
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
     * Возвращает историю операций указанного типа
     * 
     * <p>Результаты сортируются по дате операции в обратном порядке.</p>
     * 
     * @param operationType тип операции
     * @return список записей истории с указанным типом операции
     * @throws RuntimeException если произошла ошибка при получении истории
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
     * Возвращает историю операций за указанный период
     * 
     * <p>Включает операции с датой от startDate до endDate включительно.
     * Результаты сортируются по дате операции в обратном порядке.</p>
     * 
     * @param startDate начальная дата и время периода
     * @param endDate конечная дата и время периода
     * @return список записей истории за период
     * @throws RuntimeException если произошла ошибка при получении истории
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
     * Возвращает историю операций, связанных с указанной полкой
     * 
     * <p>Включает операции, где полка является источником (fromShelf)
     * или приемником (toShelf). Результаты сортируются по дате операции в обратном порядке.</p>
     * 
     * @param shelf полка
     * @return список записей истории, связанных с полкой
     * @throws RuntimeException если произошла ошибка при получении истории
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
