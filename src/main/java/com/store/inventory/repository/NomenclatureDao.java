package com.store.inventory.repository;

import com.store.inventory.domain.Manufacturer;
import com.store.inventory.domain.Nomenclature;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с номенклатурой товаров
 * 
 * <p>Предоставляет методы доступа к данным номенклатуры товаров.
 * Расширяет базовый GenericDao методами поиска по артикулу, производителю,
 * названию и проверки низкого уровня запасов.</p>
 */
public class NomenclatureDao extends GenericDao<Nomenclature, Long> {

    /**
     * Создает экземпляр DAO для работы с номенклатурой
     */
    public NomenclatureDao() {
        super(Nomenclature.class);
    }

    /**
     * Находит номенклатуру по уникальному артикулу
     * 
     * @param article артикул товара
     * @return Optional с найденной номенклатурой или пустой Optional
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает список номенклатуры указанного производителя
     * 
     * <p>Результаты сортируются по названию товара.</p>
     * 
     * @param manufacturer производитель
     * @return список номенклатуры производителя
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Выполняет поиск номенклатуры по частичному совпадению названия
     * 
     * <p>Поиск регистронезависимый. Результаты сортируются по названию.</p>
     * 
     * @param searchTerm поисковый запрос
     * @return список найденной номенклатуры
     * @throws RuntimeException если произошла ошибка при поиске
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
     * Возвращает номенклатуру, у которой текущий остаток ниже минимального уровня
     * 
     * <p>Для каждой номенклатуры подсчитывается сумма доступных товаров в статусе IN_STOCK
     * и сравнивается с минимальным уровнем запаса. Результаты сортируются по названию.</p>
     * 
     * @return список номенклатуры с низким уровнем запаса
     * @throws RuntimeException если произошла ошибка при получении списка
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

