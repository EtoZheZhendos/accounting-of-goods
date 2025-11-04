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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с поступлением товаров
 */
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

    private final DocumentDao documentDao = new DocumentDao();
    private final DocumentItemDao documentItemDao = new DocumentItemDao();
    private final ItemDao itemDao = new ItemDao();
    private final HistoryDao historyDao = new HistoryDao();

    /**
     * Создать документ поступления (черновик)
     */
    public Document createReceiptDocument(String documentNumber, LocalDate documentDate,
                                          Warehouse warehouse, String supplier, String createdBy) {
        Document document = new Document(
                DocumentType.RECEIPT,
                documentNumber,
                documentDate,
                warehouse,
                supplier,
                DocumentStatus.DRAFT,
                createdBy
        );

        return documentDao.save(document);
    }

    /**
     * Добавить строку в документ поступления
     */
    public DocumentItem addReceiptItem(Document document, Nomenclature nomenclature,
                                       BigDecimal quantity, BigDecimal purchasePrice,
                                       BigDecimal sellingPrice, Shelf shelf,
                                       String batchNumber, LocalDate manufactureDate,
                                       LocalDate expiryDate) {
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Можно добавлять строки только в черновик документа");
        }

        // Создаём строку документа
        DocumentItem documentItem = new DocumentItem(
                document,
                nomenclature,
                quantity,
                purchasePrice,
                shelf
        );

        documentItem = documentItemDao.save(documentItem);

        // Пересчитываем сумму документа
        document.recalculateTotalAmount();
        documentDao.save(document);

        return documentItem;
    }

    /**
     * Создать и провести документ поступления целиком (для UI)
     */
    public Document createAndConfirmReceiptDocument(String documentNumber, LocalDate documentDate,
                                                    Warehouse warehouse, String supplier, 
                                                    java.util.List<ReceiptItemData> items, 
                                                    String performedBy) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Присоединяем склад к сессии
            warehouse = session.merge(warehouse);

            // Создаём документ
            Document document = new Document(
                DocumentType.RECEIPT,
                documentNumber,
                documentDate,
                warehouse,
                supplier,
                DocumentStatus.DRAFT,
                performedBy
            );
            document = session.merge(document);

            // Добавляем строки
            for (ReceiptItemData itemData : items) {
                Nomenclature nomenclature = session.merge(itemData.nomenclature);
                Shelf shelf = session.merge(itemData.shelf);

                // Создаём строку документа
                DocumentItem documentItem = new DocumentItem(
                    document,
                    nomenclature,
                    itemData.quantity,
                    itemData.purchasePrice,
                    shelf
                );
                documentItem = session.merge(documentItem);

                // Создаём товарную позицию
                Item item = new Item(
                    nomenclature,
                    itemData.batchNumber,
                    itemData.quantity,
                    itemData.purchasePrice,
                    itemData.sellingPrice,
                    shelf,
                    ItemStatus.IN_STOCK
                );
                item.setManufactureDate(itemData.manufactureDate);
                item.setExpiryDate(itemData.expiryDate);
                item = session.merge(item);

                // Связываем строку документа с товарной позицией
                documentItem.setItem(item);
                session.merge(documentItem);

                // Записываем в историю
                History history = new History(
                    item,
                    document,
                    OperationType.RECEIPT,
                    itemData.quantity,
                    itemData.purchasePrice,
                    null,
                    shelf,
                    null,
                    ItemStatus.IN_STOCK,
                    performedBy,
                    "Поступление по документу " + documentNumber
                );
                session.merge(history);
            }

            // Вычисляем сумму вручную
            String sumHql = "SELECT COALESCE(SUM(di.quantity * di.price), 0) FROM DocumentItem di WHERE di.document = :document";
            BigDecimal totalAmount = session.createQuery(sumHql, BigDecimal.class)
                    .setParameter("document", document)
                    .uniqueResult();
            document.setTotalAmount(totalAmount);
            document.setStatus(DocumentStatus.CONFIRMED);
            document = session.merge(document);

            transaction.commit();
            logger.info("Документ поступления {} успешно создан и проведён", documentNumber);

            return document;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при создании документа поступления", e);
            throw new RuntimeException("Ошибка при создании документа: " + e.getMessage(), e);
        }
    }

    /**
     * Провести документ поступления
     * Создаёт товарные позиции и записывает историю
     */
    public void confirmReceiptDocument(Document document, String confirmedBy) {
        if (document.getDocumentType() != DocumentType.RECEIPT) {
            throw new IllegalArgumentException("Документ не является поступлением");
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

            // Создаём товарные позиции для каждой строки
            for (DocumentItem docItem : items) {
                // Создаём новую товарную позицию
                Item item = new Item(
                        docItem.getNomenclature(),
                        null, // batch number можно получить из docItem, если добавить поле
                        docItem.getQuantity(),
                        docItem.getPrice(),
                        docItem.getPrice(), // selling price = purchase price по умолчанию
                        docItem.getShelf(),
                        ItemStatus.IN_STOCK
                );

                item = session.merge(item);

                // Связываем строку документа с товарной позицией
                docItem.setItem(item);
                session.merge(docItem);

                // Записываем в историю
                History history = new History(
                        item,
                        document,
                        OperationType.RECEIPT,
                        docItem.getQuantity(),
                        docItem.getPrice(),
                        null,
                        docItem.getShelf(),
                        null,
                        ItemStatus.IN_STOCK,
                        confirmedBy,
                        "Поступление по документу " + document.getDocumentNumber()
                );

                session.merge(history);
            }

            // Меняем статус документа
            document.setStatus(DocumentStatus.CONFIRMED);
            session.merge(document);

            transaction.commit();
            logger.info("Документ поступления {} успешно проведён", document.getDocumentNumber());

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при проведении документа поступления", e);
            throw new RuntimeException("Ошибка при проведении документа: " + e.getMessage(), e);
        }
    }

    /**
     * Отменить проведение документа поступления
     */
    public void cancelReceiptDocument(Document document, String cancelledBy) {
        if (document.getStatus() != DocumentStatus.CONFIRMED) {
            throw new IllegalStateException("Можно отменить только проведённый документ");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Получаем строки документа
            List<DocumentItem> items = documentItemDao.findByDocument(document);

            // Для каждой строки обновляем статус товарной позиции
            for (DocumentItem docItem : items) {
                if (docItem.getItem() != null) {
                    Item item = docItem.getItem();

                    // Проверяем, не была ли позиция уже продана
                    if (item.getStatus() == ItemStatus.SOLD) {
                        throw new IllegalStateException(
                                "Нельзя отменить документ: товар уже продан (позиция #" + item.getId() + ")"
                        );
                    }

                    // Меняем статус или удаляем позицию
                    session.remove(item);

                    // Записываем в историю
                    History history = new History(
                            item,
                            document,
                            OperationType.WRITE_OFF,
                            docItem.getQuantity().negate(),
                            docItem.getPrice(),
                            docItem.getShelf(),
                            null,
                            ItemStatus.IN_STOCK,
                            null,
                            cancelledBy,
                            "Отмена документа поступления " + document.getDocumentNumber()
                    );

                    session.merge(history);
                }
            }

            // Меняем статус документа
            document.setStatus(DocumentStatus.CANCELLED);
            session.merge(document);

            transaction.commit();
            logger.info("Документ поступления {} отменён", document.getDocumentNumber());

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при отмене документа поступления", e);
            throw new RuntimeException("Ошибка при отмене документа: " + e.getMessage(), e);
        }
    }

    /**
     * Вспомогательный класс для передачи данных строки поступления
     */
    public static class ReceiptItemData {
        public Nomenclature nomenclature;
        public BigDecimal quantity;
        public BigDecimal purchasePrice;
        public BigDecimal sellingPrice;
        public Shelf shelf;
        public String batchNumber;
        public LocalDate manufactureDate;
        public LocalDate expiryDate;

        public ReceiptItemData(Nomenclature nomenclature, BigDecimal quantity, 
                              BigDecimal purchasePrice, BigDecimal sellingPrice,
                              Shelf shelf, String batchNumber, 
                              LocalDate manufactureDate, LocalDate expiryDate) {
            this.nomenclature = nomenclature;
            this.quantity = quantity;
            this.purchasePrice = purchasePrice;
            this.sellingPrice = sellingPrice;
            this.shelf = shelf;
            this.batchNumber = batchNumber;
            this.manufactureDate = manufactureDate;
            this.expiryDate = expiryDate;
        }
    }
}

