package com.store.inventory.repository;

import com.store.inventory.domain.Document;
import com.store.inventory.domain.DocumentItem;
import com.store.inventory.domain.Nomenclature;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO для работы со строками документов
 */
public class DocumentItemDao extends GenericDao<DocumentItem, Long> {

    public DocumentItemDao() {
        super(DocumentItem.class);
    }

    /**
     * Получить все строки документа
     */
    public List<DocumentItem> findByDocument(Document document) {
        try (Session session = getSession()) {
            String hql = "FROM DocumentItem WHERE document = :document ORDER BY id";
            Query<DocumentItem> query = session.createQuery(hql, DocumentItem.class);
            query.setParameter("document", document);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении строк документа", e);
            throw new RuntimeException("Ошибка при получении строк: " + e.getMessage(), e);
        }
    }

    /**
     * Получить строки по номенклатуре
     */
    public List<DocumentItem> findByNomenclature(Nomenclature nomenclature) {
        try (Session session = getSession()) {
            String hql = "FROM DocumentItem WHERE nomenclature = :nomenclature ORDER BY createdAt DESC";
            Query<DocumentItem> query = session.createQuery(hql, DocumentItem.class);
            query.setParameter("nomenclature", nomenclature);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении строк по номенклатуре", e);
            throw new RuntimeException("Ошибка при получении строк: " + e.getMessage(), e);
        }
    }
}

