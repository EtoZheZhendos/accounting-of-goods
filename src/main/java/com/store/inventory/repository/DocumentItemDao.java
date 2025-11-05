package com.store.inventory.repository;

import com.store.inventory.domain.Document;
import com.store.inventory.domain.DocumentItem;
import com.store.inventory.domain.Nomenclature;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO для работы со строками документов
 * 
 * <p>Предоставляет методы доступа к данным строк (позиций) документов.
 * Расширяет базовый GenericDao методами получения строк по документу
 * и по номенклатуре.</p>
 */
public class DocumentItemDao extends GenericDao<DocumentItem, Long> {

    /**
     * Создает экземпляр DAO для работы со строками документов
     */
    public DocumentItemDao() {
        super(DocumentItem.class);
    }

    /**
     * Возвращает список всех строк указанного документа
     * 
     * <p>Результаты сортируются по идентификатору строки.</p>
     * 
     * @param document документ
     * @return список строк документа
     * @throws RuntimeException если произошла ошибка при получении списка
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
     * Возвращает список строк документов для указанной номенклатуры
     * 
     * <p>Результаты сортируются по дате создания в обратном порядке.
     * Используется для анализа истории движения конкретной номенклатуры.</p>
     * 
     * @param nomenclature номенклатура товара
     * @return список строк с указанной номенклатурой
     * @throws RuntimeException если произошла ошибка при получении списка
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
