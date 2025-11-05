package com.store.inventory.repository;

import com.store.inventory.domain.Document;
import com.store.inventory.domain.DocumentStatus;
import com.store.inventory.domain.DocumentType;
import com.store.inventory.domain.Warehouse;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с документами движения товаров
 * 
 * <p>Предоставляет методы доступа к данным документов.
 * Расширяет базовый GenericDao методами поиска по номеру, типу, статусу,
 * дате, складу и различным комбинациям этих параметров.</p>
 */
public class DocumentDao extends GenericDao<Document, Long> {

    /**
     * Создает экземпляр DAO для работы с документами
     */
    public DocumentDao() {
        super(Document.class);
    }

    /**
     * Находит документ по уникальному номеру
     * 
     * @param documentNumber номер документа
     * @return Optional с найденным документом или пустой Optional
     * @throws RuntimeException если произошла ошибка при поиске
     */
    public Optional<Document> findByDocumentNumber(String documentNumber) {
        try (Session session = getSession()) {
            String hql = "FROM Document WHERE documentNumber = :documentNumber";
            Query<Document> query = session.createQuery(hql, Document.class);
            query.setParameter("documentNumber", documentNumber);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Ошибка при поиске документа по номеру: {}", documentNumber, e);
            throw new RuntimeException("Ошибка при поиске документа: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает список документов указанного типа
     * 
     * <p>Результаты сортируются по дате и номеру документа в обратном порядке.</p>
     * 
     * @param documentType тип документа
     * @return список документов указанного типа
     * @throws RuntimeException если произошла ошибка при поиске
     */
    public List<Document> findByType(DocumentType documentType) {
        try (Session session = getSession()) {
            String hql = "FROM Document WHERE documentType = :type ORDER BY documentDate DESC, documentNumber DESC";
            Query<Document> query = session.createQuery(hql, Document.class);
            query.setParameter("type", documentType);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске документов по типу", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает список документов с указанным статусом
     * 
     * <p>Результаты сортируются по дате документа в обратном порядке.</p>
     * 
     * @param status статус документа
     * @return список документов с указанным статусом
     * @throws RuntimeException если произошла ошибка при поиске
     */
    public List<Document> findByStatus(DocumentStatus status) {
        try (Session session = getSession()) {
            String hql = "FROM Document WHERE status = :status ORDER BY documentDate DESC";
            Query<Document> query = session.createQuery(hql, Document.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске документов по статусу", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает список документов за указанный период
     * 
     * <p>Включает документы с датой от startDate до endDate включительно.
     * Результаты сортируются по дате документа в обратном порядке.</p>
     * 
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @return список документов за период
     * @throws RuntimeException если произошла ошибка при поиске
     */
    public List<Document> findByDateRange(LocalDate startDate, LocalDate endDate) {
        try (Session session = getSession()) {
            String hql = "FROM Document WHERE documentDate BETWEEN :startDate AND :endDate ORDER BY documentDate DESC";
            Query<Document> query = session.createQuery(hql, Document.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске документов за период", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает список документов, связанных с указанным складом
     * 
     * <p>Результаты сортируются по дате документа в обратном порядке.</p>
     * 
     * @param warehouse склад
     * @return список документов склада
     * @throws RuntimeException если произошла ошибка при поиске
     */
    public List<Document> findByWarehouse(Warehouse warehouse) {
        try (Session session = getSession()) {
            String hql = "FROM Document WHERE warehouse = :warehouse ORDER BY documentDate DESC";
            Query<Document> query = session.createQuery(hql, Document.class);
            query.setParameter("warehouse", warehouse);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске документов по складу", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает список документов указанного типа и статуса
     * 
     * <p>Результаты сортируются по дате документа в обратном порядке.</p>
     * 
     * @param type тип документа
     * @param status статус документа
     * @return список документов с указанным типом и статусом
     * @throws RuntimeException если произошла ошибка при поиске
     */
    public List<Document> findByTypeAndStatus(DocumentType type, DocumentStatus status) {
        try (Session session = getSession()) {
            String hql = "FROM Document WHERE documentType = :type AND status = :status ORDER BY documentDate DESC";
            Query<Document> query = session.createQuery(hql, Document.class);
            query.setParameter("type", type);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске документов по типу и статусу", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }
}
