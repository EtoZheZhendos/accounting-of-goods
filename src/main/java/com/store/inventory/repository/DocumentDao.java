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
 * DAO для работы с документами
 */
public class DocumentDao extends GenericDao<Document, Long> {

    public DocumentDao() {
        super(Document.class);
    }

    /**
     * Найти документ по номеру
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
     * Найти документы по типу
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
     * Найти документы по статусу
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
     * Найти документы за период
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
     * Найти документы по складу
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
     * Найти документы по типу и статусу
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

