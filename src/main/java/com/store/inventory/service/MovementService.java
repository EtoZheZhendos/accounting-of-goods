package com.store.inventory.service;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import com.store.inventory.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Сервис для работы с перемещением товаров
 */
public class MovementService {

    private static final Logger logger = LoggerFactory.getLogger(MovementService.class);

    private final DocumentDao documentDao = new DocumentDao();
    private final DocumentItemDao documentItemDao = new DocumentItemDao();
    private final ItemDao itemDao = new ItemDao();
    private final HistoryDao historyDao = new HistoryDao();

    /**
     * Создать документ перемещения (черновик)
     */
    public Document createMovementDocument(String documentNumber, LocalDate documentDate,
                                           Warehouse warehouse, String createdBy) {
        Document document = new Document(
                DocumentType.MOVEMENT,
                documentNumber,
                documentDate,
                warehouse,
                null,
                DocumentStatus.DRAFT,
                createdBy
        );

        return documentDao.save(document);
    }

    /**
     * Добавить строку перемещения
     */
    public DocumentItem addMovementItem(Document document, Item item, Shelf targetShelf) {
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Можно добавлять строки только в черновик документа");
        }

        if (item.getCurrentShelf() == null) {
            throw new IllegalStateException("У товара не указано текущее местоположение");
        }

        if (item.getCurrentShelf().equals(targetShelf)) {
            throw new IllegalStateException("Товар уже находится на целевой полке");
        }

        // Создаём строку документа
        DocumentItem documentItem = new DocumentItem(
                document,
                item.getNomenclature(),
                item.getQuantity(),
                BigDecimal.ZERO, // для перемещения цена не важна
                targetShelf
        );
        documentItem.setItem(item);

        return documentItemDao.save(documentItem);
    }

    /**
     * Провести документ перемещения
     */
    public void confirmMovementDocument(Document document, String confirmedBy) {
        if (document.getDocumentType() != DocumentType.MOVEMENT) {
            throw new IllegalArgumentException("Документ не является перемещением");
        }

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Можно провести только черновик документа");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Присоединяем документ к текущей сессии
            document = session.merge(document);

            // Получаем строки документа через текущую сессию
            String hql = "FROM DocumentItem WHERE document = :document";
            List<DocumentItem> items = session.createQuery(hql, DocumentItem.class)
                    .setParameter("document", document)
                    .list();

            if (items.isEmpty()) {
                throw new IllegalStateException("Нельзя провести пустой документ");
            }

            // Обрабатываем каждую строку
            for (DocumentItem docItem : items) {
                Item item = session.merge(docItem.getItem());

                if (item == null) {
                    throw new IllegalStateException("Товарная позиция не найдена");
                }

                Shelf fromShelf = item.getCurrentShelf();
                Shelf toShelf = docItem.getShelf();

                // Перемещаем товар
                item.setCurrentShelf(toShelf);
                session.merge(item);

                // Записываем в историю
                History history = new History(
                        item,
                        document,
                        OperationType.MOVEMENT,
                        null,
                        null,
                        fromShelf,
                        toShelf,
                        item.getStatus(),
                        item.getStatus(),
                        confirmedBy,
                        "Перемещение: " + fromShelf.getFullAddress() + " → " + toShelf.getFullAddress()
                );

                session.merge(history);
            }

            // Меняем статус документа
            document.setStatus(DocumentStatus.CONFIRMED);
            session.merge(document);

            transaction.commit();
            logger.info("Документ перемещения {} успешно проведён", document.getDocumentNumber());

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при проведении документа перемещения", e);
            throw new RuntimeException("Ошибка при проведении документа: " + e.getMessage(), e);
        }
    }

    /**
     * Переместить товар на другую полку (быстрое перемещение без документа)
     */
    public void moveItemToShelf(Item item, Shelf targetShelf, String movedBy) {
        if (item.getCurrentShelf() == null) {
            throw new IllegalStateException("У товара не указано текущее местоположение");
        }

        if (item.getCurrentShelf().equals(targetShelf)) {
            throw new IllegalStateException("Товар уже находится на целевой полке");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Shelf fromShelf = item.getCurrentShelf();

            // Перемещаем товар
            item.setCurrentShelf(targetShelf);
            session.merge(item);

            // Записываем в историю
            History history = new History(
                    item,
                    null,
                    OperationType.MOVEMENT,
                    null,
                    null,
                    fromShelf,
                    targetShelf,
                    item.getStatus(),
                    item.getStatus(),
                    movedBy,
                    "Быстрое перемещение: " + fromShelf.getFullAddress() + " → " + targetShelf.getFullAddress()
            );

            session.merge(history);

            transaction.commit();
            logger.info("Товар {} перемещён с {} на {}", item.getId(), fromShelf.getCode(), targetShelf.getCode());

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при перемещении товара", e);
            throw new RuntimeException("Ошибка при перемещении: " + e.getMessage(), e);
        }
    }

    /**
     * Переместить товар с созданием документа (упрощенный метод для UI)
     */
    public void moveItem(String documentNumber, java.time.LocalDate documentDate, 
                        Item item, java.math.BigDecimal quantity, 
                        Shelf targetShelf, String comment, String performedBy) {
        if (quantity.compareTo(item.getQuantity()) > 0) {
            throw new IllegalArgumentException("Недостаточно товара для перемещения");
        }

        Document document = createMovementDocument(
            documentNumber, 
            documentDate, 
            item.getCurrentShelf().getWarehouse(), 
            performedBy
        );

        addMovementItem(document, item, targetShelf);

        confirmMovementDocument(document, performedBy);
    }
    
    /**
     * Простое перемещение товара на другой склад
     */
    public void moveItem(Item item, Shelf targetShelf) {
        moveItemToShelf(item, targetShelf, "Система");
    }
    
    /**
     * Создать и провести документ перемещения в одной транзакции
     */
    public Document createAndConfirmMovementDocument(String documentNumber, LocalDate documentDate,
                                                     Warehouse sourceWarehouse, 
                                                     java.util.List<MovementItemData> items,
                                                     String performedBy) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            sourceWarehouse = session.merge(sourceWarehouse);

            Document document = new Document(
                DocumentType.MOVEMENT,
                documentNumber,
                documentDate,
                sourceWarehouse,
                null,
                DocumentStatus.DRAFT,
                performedBy
            );
            document = session.merge(document);

            for (MovementItemData itemData : items) {
                Item item = session.merge(itemData.item);
                Shelf targetShelf = session.merge(itemData.targetShelf);
                
                if (item.getCurrentShelf().equals(targetShelf)) {
                    logger.warn("Товар {} уже находится на полке {}", item.getId(), targetShelf.getId());
                    continue;
                }

                DocumentItem documentItem = new DocumentItem(
                    document,
                    item.getNomenclature(),
                    item.getQuantity(),
                    BigDecimal.ZERO,
                    targetShelf
                );
                documentItem.setItem(item);
                documentItem = session.merge(documentItem);

                Shelf fromShelf = item.getCurrentShelf();
                item.setCurrentShelf(targetShelf);
                item = session.merge(item);

                History history = new History(
                    item,
                    document,
                    OperationType.MOVEMENT,
                    null,
                    null,
                    fromShelf,
                    targetShelf,
                    item.getStatus(),
                    item.getStatus(),
                    performedBy,
                    "Перемещение: " + fromShelf.getFullAddress() + " → " + targetShelf.getFullAddress()
                );
                session.merge(history);
            }

            document.setStatus(DocumentStatus.CONFIRMED);
            document = session.merge(document);

            transaction.commit();
            logger.info("Документ перемещения {} успешно создан и проведён", documentNumber);

            return document;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при создании документа перемещения", e);
            throw new RuntimeException("Ошибка при создании документа: " + e.getMessage(), e);
        }
    }
    
    /**
     * Класс для передачи данных о перемещаемой позиции
     */
    public static class MovementItemData {
        public final Item item;
        public final Shelf targetShelf;
        
        public MovementItemData(Item item, Shelf targetShelf) {
            this.item = item;
            this.targetShelf = targetShelf;
        }
    }
}

