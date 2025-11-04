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
 * Сервис для работы с реализацией (продажей) товаров
 */
public class SaleService {

    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);

    private final DocumentDao documentDao = new DocumentDao();
    private final DocumentItemDao documentItemDao = new DocumentItemDao();
    private final ItemDao itemDao = new ItemDao();
    private final HistoryDao historyDao = new HistoryDao();

    /**
     * Создать документ реализации (черновик)
     */
    public Document createSaleDocument(String documentNumber, LocalDate documentDate,
                                       Warehouse warehouse, String customer, String createdBy) {
        Document document = new Document(
                DocumentType.SALE,
                documentNumber,
                documentDate,
                warehouse,
                customer,
                DocumentStatus.DRAFT,
                createdBy
        );

        return documentDao.save(document);
    }

    /**
     * Добавить строку в документ реализации
     */
    public DocumentItem addSaleItem(Document document, Item item, BigDecimal quantity,
                                    BigDecimal sellingPrice) {
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Можно добавлять строки только в черновик документа");
        }

        if (item.getStatus() != ItemStatus.IN_STOCK) {
            throw new IllegalStateException("Товар недоступен для продажи (статус: " + item.getStatus() + ")");
        }

        if (item.getQuantity().compareTo(quantity) < 0) {
            throw new IllegalStateException("Недостаточное количество товара на складе");
        }

        // Создаём строку документа
        DocumentItem documentItem = new DocumentItem(
                document,
                item.getNomenclature(),
                quantity,
                sellingPrice,
                item.getCurrentShelf()
        );
        documentItem.setItem(item);

        documentItem = documentItemDao.save(documentItem);

        // Пересчитываем сумму документа
        document.recalculateTotalAmount();
        documentDao.save(document);

        return documentItem;
    }

    /**
     * Провести документ реализации
     * Обновляет статус товарных позиций и записывает историю
     */
    public void confirmSaleDocument(Document document, String confirmedBy) {
        if (document.getDocumentType() != DocumentType.SALE) {
            throw new IllegalArgumentException("Документ не является реализацией");
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
                    throw new IllegalStateException("Товарная позиция не найдена для строки документа");
                }

                // Проверяем доступность товара
                if (item.getStatus() != ItemStatus.IN_STOCK) {
                    throw new IllegalStateException(
                            "Товар недоступен для продажи (позиция #" + item.getId() + ", статус: " + item.getStatus() + ")"
                    );
                }

                // Обновляем количество или статус
                BigDecimal remainingQuantity = item.getQuantity().subtract(docItem.getQuantity());

                if (remainingQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalStateException(
                            "Недостаточное количество товара (позиция #" + item.getId() + ")"
                    );
                }

                if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    // Товар полностью продан
                    item.setStatus(ItemStatus.SOLD);
                    item.setQuantity(BigDecimal.ZERO);
                } else {
                    // Частичная продажа - уменьшаем количество
                    item.setQuantity(remainingQuantity);
                }

                session.merge(item);

                // Записываем в историю
                History history = new History(
                        item,
                        document,
                        OperationType.SALE,
                        docItem.getQuantity().negate(),
                        docItem.getPrice(),
                        item.getCurrentShelf(),
                        null,
                        ItemStatus.IN_STOCK,
                        item.getStatus(),
                        confirmedBy,
                        "Продажа по документу " + document.getDocumentNumber()
                );

                session.merge(history);
            }

            // Меняем статус документа
            document.setStatus(DocumentStatus.CONFIRMED);
            session.merge(document);

            transaction.commit();
            logger.info("Документ реализации {} успешно проведён", document.getDocumentNumber());

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при проведении документа реализации", e);
            throw new RuntimeException("Ошибка при проведении документа: " + e.getMessage(), e);
        }
    }

    /**
     * Получить доступные для продажи товары по номенклатуре
     */
    public List<Item> getAvailableItems(Nomenclature nomenclature) {
        return itemDao.findByNomenclature(nomenclature).stream()
                .filter(item -> item.getStatus() == ItemStatus.IN_STOCK)
                .filter(item -> item.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    /**
     * Получить общее доступное количество по номенклатуре
     */
    public BigDecimal getAvailableQuantity(Nomenclature nomenclature) {
        return itemDao.getTotalQuantityByNomenclatureAndStatus(nomenclature, ItemStatus.IN_STOCK);
    }

    /**
     * Создать и провести документ реализации целиком (для UI)
     */
    public Document createAndConfirmSaleDocument(String documentNumber, LocalDate documentDate,
                                                 Warehouse warehouse, String customer,
                                                 java.util.List<SaleItemData> items,
                                                 String performedBy) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Присоединяем склад к сессии
            warehouse = session.merge(warehouse);

            // Создаём документ
            Document document = new Document(
                DocumentType.SALE,
                documentNumber,
                documentDate,
                warehouse,
                customer,
                DocumentStatus.DRAFT,
                performedBy
            );
            document = session.merge(document);

            // Добавляем строки и проводим
            for (SaleItemData itemData : items) {
                Item item = session.merge(itemData.item);

                // Проверяем доступность товара
                if (item.getStatus() != ItemStatus.IN_STOCK) {
                    throw new IllegalStateException(
                        "Товар недоступен для продажи (позиция #" + item.getId() + ")"
                    );
                }

                // Проверяем количество
                if (itemData.quantity.compareTo(item.getQuantity()) > 0) {
                    throw new IllegalStateException(
                        "Недостаточно товара. Доступно: " + item.getQuantity()
                    );
                }

                // Создаём строку документа
                DocumentItem documentItem = new DocumentItem(
                    document,
                    item.getNomenclature(),
                    itemData.quantity,
                    itemData.salePrice,
                    item.getCurrentShelf()
                );
                documentItem.setItem(item);
                documentItem = session.merge(documentItem);

                // Обновляем количество товара
                BigDecimal remainingQuantity = item.getQuantity().subtract(itemData.quantity);
                
                if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    item.setStatus(ItemStatus.SOLD);
                }
                item.setQuantity(remainingQuantity);
                session.merge(item);

                // Записываем в историю
                History history = new History(
                    item,
                    document,
                    OperationType.SALE,
                    itemData.quantity.negate(),
                    itemData.salePrice,
                    item.getCurrentShelf(),
                    null,
                    ItemStatus.IN_STOCK,
                    item.getStatus(),
                    performedBy,
                    "Продажа по документу " + documentNumber
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
            logger.info("Документ реализации {} успешно создан и проведён", documentNumber);

            return document;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка при создании документа реализации", e);
            throw new RuntimeException("Ошибка при создании документа: " + e.getMessage(), e);
        }
    }

    /**
     * Вспомогательный класс для передачи данных строки реализации
     */
    public static class SaleItemData {
        public Item item;
        public BigDecimal quantity;
        public BigDecimal salePrice;

        public SaleItemData(Item item, BigDecimal quantity, BigDecimal salePrice) {
            this.item = item;
            this.quantity = quantity;
            this.salePrice = salePrice;
        }
    }
}

