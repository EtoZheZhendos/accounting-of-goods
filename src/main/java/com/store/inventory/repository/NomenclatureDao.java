package com.store.inventory.repository;

import com.store.inventory.domain.Manufacturer;
import com.store.inventory.domain.Nomenclature;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с номенклатурой
 */
public class NomenclatureDao extends GenericDao<Nomenclature, Long> {

    public NomenclatureDao() {
        super(Nomenclature.class);
    }

    /**
     * Найти номенклатуру по артикулу
     */
    public Optional<Nomenclature> findByArticle(String article) {
        try (Session session = getSession()) {
            String hql = "FROM Nomenclature WHERE article = :article";
            Query<Nomenclature> query = session.createQuery(hql, Nomenclature.class);
            query.setParameter("article", article);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Ошибка при поиске номенклатуры по артикулу: {}", article, e);
            throw new RuntimeException("Ошибка при поиске номенклатуры: " + e.getMessage(), e);
        }
    }

    /**
     * Найти номенклатуру по производителю
     */
    public List<Nomenclature> findByManufacturer(Manufacturer manufacturer) {
        try (Session session = getSession()) {
            String hql = "FROM Nomenclature WHERE manufacturer = :manufacturer ORDER BY name";
            Query<Nomenclature> query = session.createQuery(hql, Nomenclature.class);
            query.setParameter("manufacturer", manufacturer);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске номенклатуры по производителю", e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Поиск номенклатуры по названию (частичное совпадение)
     */
    public List<Nomenclature> searchByName(String searchTerm) {
        try (Session session = getSession()) {
            String hql = "FROM Nomenclature WHERE LOWER(name) LIKE LOWER(:searchTerm) ORDER BY name";
            Query<Nomenclature> query = session.createQuery(hql, Nomenclature.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при поиске номенклатуры по названию: {}", searchTerm, e);
            throw new RuntimeException("Ошибка при поиске: " + e.getMessage(), e);
        }
    }

    /**
     * Получить номенклатуру с низким уровнем запаса
     */
    public List<Nomenclature> findLowStockItems() {
        try (Session session = getSession()) {
            String hql = """
                FROM Nomenclature n
                WHERE (SELECT COALESCE(SUM(i.quantity), 0)
                       FROM Item i
                       WHERE i.nomenclature = n AND i.status = 'IN_STOCK') < n.minStockLevel
                ORDER BY n.name
                """;
            Query<Nomenclature> query = session.createQuery(hql, Nomenclature.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Ошибка при получении номенклатуры с низким запасом", e);
            throw new RuntimeException("Ошибка при получении списка: " + e.getMessage(), e);
        }
    }
}

