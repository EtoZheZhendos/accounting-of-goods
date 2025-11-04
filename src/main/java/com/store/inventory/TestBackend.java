package com.store.inventory;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import com.store.inventory.service.*;
import com.store.inventory.util.DatabaseInitializer;
import com.store.inventory.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Тестовое приложение для проверки работы backend
 */
public class TestBackend {

    private static final Logger logger = LoggerFactory.getLogger(TestBackend.class);

    public static void main(String[] args) {
        logger.info("=== Запуск тестового приложения ===");

        try {
            // Инициализация базы данных
            HibernateUtil.getSessionFactory();
            logger.info("Hibernate SessionFactory инициализирована");

            // Инициализация тестовых данных (если нужно)
            // DatabaseInitializer.initializeDatabase();

            // Создание экземпляров DAO
            ManufacturerDao manufacturerDao = new ManufacturerDao();
            NomenclatureDao nomenclatureDao = new NomenclatureDao();
            WarehouseDao warehouseDao = new WarehouseDao();
            ShelfDao shelfDao = new ShelfDao();
            ItemDao itemDao = new ItemDao();

            // Создание экземпляров сервисов
            ReceiptService receiptService = new ReceiptService();
            SaleService saleService = new SaleService();
            MovementService movementService = new MovementService();
            ReportService reportService = new ReportService();

            // ===== ТЕСТ 1: Создание производителя =====
            logger.info("\n=== ТЕСТ 1: Создание производителя ===");
            Manufacturer manufacturer = new Manufacturer("ООО Тестовый производитель", "Россия", "тел: +7-123-456-78-90");
            manufacturer = manufacturerDao.save(manufacturer);
            logger.info("Создан производитель: {}", manufacturer.getName());

            // ===== ТЕСТ 2: Создание номенклатуры =====
            logger.info("\n=== ТЕСТ 2: Создание номенклатуры ===");
            Nomenclature nomenclature = new Nomenclature(
                    "TEST-001",
                    "Тестовый товар",
                    "Описание тестового товара",
                    "шт",
                    manufacturer,
                    10
            );
            nomenclature = nomenclatureDao.save(nomenclature);
            logger.info("Создана номенклатура: {} (артикул: {})", nomenclature.getName(), nomenclature.getArticle());

            // ===== ТЕСТ 3: Создание склада и полок =====
            logger.info("\n=== ТЕСТ 3: Создание склада и полок ===");
            Warehouse warehouse = new Warehouse("Тестовый склад", "г. Тест, ул. Тестовая, 1", true);
            warehouse = warehouseDao.save(warehouse);
            logger.info("Создан склад: {}", warehouse.getName());

            Shelf shelf1 = new Shelf(warehouse, "A-1-1", "Ряд A, Стеллаж 1, Полка 1", 100, true);
            shelf1 = shelfDao.save(shelf1);
            logger.info("Создана полка: {}", shelf1.getFullAddress());

            Shelf shelf2 = new Shelf(warehouse, "A-1-2", "Ряд A, Стеллаж 1, Полка 2", 100, true);
            shelf2 = shelfDao.save(shelf2);
            logger.info("Создана полка: {}", shelf2.getFullAddress());

            // ===== ТЕСТ 4: Создание документа поступления =====
            logger.info("\n=== ТЕСТ 4: Создание документа поступления ===");
            Document receiptDoc = receiptService.createReceiptDocument(
                    "ПС-00001",
                    LocalDate.now(),
                    warehouse,
                    "ООО Поставщик",
                    "Иванов И.И."
            );
            logger.info("Создан документ поступления: {}", receiptDoc.getDocumentNumber());

            // Добавляем строку в документ
            DocumentItem receiptItem = receiptService.addReceiptItem(
                    receiptDoc,
                    nomenclature,
                    new BigDecimal("100"),
                    new BigDecimal("150.00"),
                    new BigDecimal("200.00"),
                    shelf1,
                    "BATCH-001",
                    LocalDate.now(),
                    LocalDate.now().plusMonths(6)
            );
            logger.info("Добавлена строка: {} × {} = {}",
                    nomenclature.getName(),
                    receiptItem.getQuantity(),
                    receiptItem.getTotal());

            // Проводим документ
            receiptService.confirmReceiptDocument(receiptDoc, "Иванов И.И.");
            logger.info("Документ поступления {} проведён", receiptDoc.getDocumentNumber());

            // ===== ТЕСТ 5: Проверка остатков =====
            logger.info("\n=== ТЕСТ 5: Проверка остатков ===");
            Map<Nomenclature, BigDecimal> stockReport = reportService.getStockReport();
            stockReport.forEach((nom, qty) ->
                    logger.info("Номенклатура: {} - Остаток: {} {}", nom.getName(), qty, nom.getUnit())
            );

            // ===== ТЕСТ 6: Получение доступных товаров =====
            logger.info("\n=== ТЕСТ 6: Получение доступных товаров для продажи ===");
            List<Item> availableItems = saleService.getAvailableItems(nomenclature);
            logger.info("Доступно позиций для продажи: {}", availableItems.size());
            availableItems.forEach(item ->
                    logger.info("  - Позиция #{}: {} {} по цене {} руб.",
                            item.getId(),
                            item.getQuantity(),
                            item.getNomenclature().getUnit(),
                            item.getSellingPrice())
            );

            // ===== ТЕСТ 7: Создание документа продажи =====
            logger.info("\n=== ТЕСТ 7: Создание документа продажи ===");
            if (!availableItems.isEmpty()) {
                Item itemToSell = availableItems.get(0);

                Document saleDoc = saleService.createSaleDocument(
                        "РЛ-00001",
                        LocalDate.now(),
                        warehouse,
                        "Покупатель Петров П.П.",
                        "Иванов И.И."
                );
                logger.info("Создан документ реализации: {}", saleDoc.getDocumentNumber());

                // Добавляем товар для продажи
                DocumentItem saleItem = saleService.addSaleItem(
                        saleDoc,
                        itemToSell,
                        new BigDecimal("30"),
                        itemToSell.getSellingPrice()
                );
                logger.info("Добавлена строка: {} × {} = {}",
                        saleItem.getNomenclature().getName(),
                        saleItem.getQuantity(),
                        saleItem.getTotal());

                // Проводим документ
                saleService.confirmSaleDocument(saleDoc, "Иванов И.И.");
                logger.info("Документ реализации {} проведён", saleDoc.getDocumentNumber());
            }

            // ===== ТЕСТ 8: Проверка остатков после продажи =====
            logger.info("\n=== ТЕСТ 8: Проверка остатков после продажи ===");
            stockReport = reportService.getStockReport();
            stockReport.forEach((nom, qty) ->
                    logger.info("Номенклатура: {} - Остаток: {} {}", nom.getName(), qty, nom.getUnit())
            );

            // ===== ТЕСТ 9: Перемещение товара =====
            logger.info("\n=== ТЕСТ 9: Перемещение товара ===");
            availableItems = saleService.getAvailableItems(nomenclature);
            if (!availableItems.isEmpty()) {
                Item itemToMove = availableItems.get(0);
                logger.info("Перемещаем товар с полки {} на полку {}",
                        itemToMove.getCurrentShelf().getCode(),
                        shelf2.getCode());

                movementService.moveItemToShelf(itemToMove, shelf2, "Иванов И.И.");
                logger.info("Товар успешно перемещён");
            }

            // ===== ТЕСТ 10: Отчёты =====
            logger.info("\n=== ТЕСТ 10: Отчёты ===");

            // Отчёт по продажам
            Map<String, Object> salesReport = reportService.getSalesReport(
                    LocalDate.now().minusDays(7),
                    LocalDate.now()
            );
            logger.info("Отчёт по продажам:");
            logger.info("  Период: {} - {}", salesReport.get("startDate"), salesReport.get("endDate"));
            logger.info("  Документов: {}", salesReport.get("documentCount"));
            logger.info("  Сумма продаж: {} руб.", salesReport.get("totalSales"));

            // Сводка по складу
            Map<String, Object> warehouseSummary = reportService.getWarehouseSummary(warehouse);
            logger.info("\nСводка по складу '{}':", warehouse.getName());
            logger.info("  Всего позиций: {}", warehouseSummary.get("totalItems"));
            logger.info("  Общее количество: {}", warehouseSummary.get("totalQuantity"));
            logger.info("  Общая стоимость: {} руб.", warehouseSummary.get("totalValue"));

            // Товары с низким запасом
            List<Nomenclature> lowStockItems = reportService.getLowStockReport();
            if (!lowStockItems.isEmpty()) {
                logger.info("\nТовары с низким запасом:");
                lowStockItems.forEach(nom ->
                        logger.info("  - {} (минимум: {})", nom.getName(), nom.getMinStockLevel())
                );
            } else {
                logger.info("\nНет товаров с низким запасом");
            }

            logger.info("\n=== Все тесты успешно выполнены! ===");

        } catch (Exception e) {
            logger.error("Ошибка при выполнении тестов", e);
        } finally {
            // Закрываем SessionFactory
            HibernateUtil.shutdown();
            logger.info("Приложение завершено");
        }
    }
}

