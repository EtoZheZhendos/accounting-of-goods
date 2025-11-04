package com.store.inventory.util;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Сидер для заполнения базы тестовыми данными
 */
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final ManufacturerDao manufacturerDao = new ManufacturerDao();
    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final WarehouseDao warehouseDao = new WarehouseDao();
    private final ShelfDao shelfDao = new ShelfDao();
    private final ItemDao itemDao = new ItemDao();

    /**
     * Заполнить базу тестовыми данными
     */
    public void seed() {
        logger.info("Начало заполнения базы тестовыми данными...");

        try {
            // Проверяем, есть ли уже данные
            if (manufacturerDao.count() > 0) {
                logger.info("База уже содержит данные. Пропускаем заполнение.");
                return;
            }

            // 1. Создаём производителей
            logger.info("Создание производителей...");
            Manufacturer samsung = createManufacturer("Samsung Electronics", "Южная Корея", "www.samsung.com");
            Manufacturer apple = createManufacturer("Apple Inc.", "США", "www.apple.com");
            Manufacturer xiaomi = createManufacturer("Xiaomi Corporation", "Китай", "www.mi.com");
            Manufacturer huawei = createManufacturer("Huawei Technologies", "Китай", "www.huawei.com");
            Manufacturer rassvet = createManufacturer("ООО \"Рассвет\"", "Россия", "тел: +7-495-123-45-67");

            // 2. Создаём склады
            logger.info("Создание складов...");
            Warehouse mainWarehouse = createWarehouse("Центральный склад", "г. Москва, ул. Складская, д. 1", true);
            Warehouse warehouse2 = createWarehouse("Склад №2", "г. Москва, ул. Торговая, д. 15", true);

            // 3. Создаём полки для центрального склада
            logger.info("Создание полок...");
            Shelf shelfA11 = createShelf(mainWarehouse, "A-1-1", "Ряд A, Стеллаж 1, Полка 1", 100);
            Shelf shelfA12 = createShelf(mainWarehouse, "A-1-2", "Ряд A, Стеллаж 1, Полка 2", 100);
            Shelf shelfA21 = createShelf(mainWarehouse, "A-2-1", "Ряд A, Стеллаж 2, Полка 1", 150);
            Shelf shelfB11 = createShelf(mainWarehouse, "B-1-1", "Ряд B, Стеллаж 1, Полка 1", 100);
            Shelf shelfB12 = createShelf(mainWarehouse, "B-1-2", "Ряд B, Стеллаж 1, Полка 2", 100);

            // Полки для склада №2
            Shelf shelf2A11 = createShelf(warehouse2, "A-1-1", "Ряд A, Стеллаж 1, Полка 1", 80);
            Shelf shelf2A12 = createShelf(warehouse2, "A-1-2", "Ряд A, Стеллаж 1, Полка 2", 80);

            // 4. Создаём номенклатуру
            logger.info("Создание номенклатуры...");
            Nomenclature samsungS21 = createNomenclature("SM-G990", "Samsung Galaxy S21", 
                    "Смартфон Samsung Galaxy S21 128GB", "шт", samsung, 5);
            Nomenclature iphone13 = createNomenclature("IPHONE-13", "Apple iPhone 13", 
                    "Смартфон Apple iPhone 13 128GB", "шт", apple, 3);
            Nomenclature xiaomiMi11 = createNomenclature("MI-11", "Xiaomi Mi 11", 
                    "Смартфон Xiaomi Mi 11 256GB", "шт", xiaomi, 5);
            Nomenclature huaweiP40 = createNomenclature("HUAWEI-P40", "Huawei P40 Pro", 
                    "Смартфон Huawei P40 Pro 256GB", "шт", huawei, 3);
            Nomenclature cases = createNomenclature("CASE-001", "Чехол универсальный", 
                    "Защитный чехол для смартфонов", "шт", rassvet, 20);
            Nomenclature charger = createNomenclature("CHARGER-USB-C", "Зарядное устройство USB-C", 
                    "Быстрая зарядка 25W", "шт", samsung, 15);
            Nomenclature headphones = createNomenclature("HEADPHONES-001", "Наушники TWS", 
                    "Беспроводные наушники Bluetooth 5.0", "шт", xiaomi, 10);
            Nomenclature screenProtector = createNomenclature("SCREEN-PROT-001", "Защитное стекло", 
                    "Закалённое стекло для смартфонов", "шт", rassvet, 30);

            // 5. Создаём товарные позиции (Items)
            logger.info("Создание товарных позиций...");
            
            // Samsung Galaxy S21
            createItem(samsungS21, "BATCH-2024-001", new BigDecimal("10"), 
                    new BigDecimal("45000.00"), new BigDecimal("59990.00"), 
                    shelfA11, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(2), null);
            
            createItem(samsungS21, "BATCH-2024-002", new BigDecimal("8"), 
                    new BigDecimal("44000.00"), new BigDecimal("59990.00"), 
                    shelfA11, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(1), null);

            // iPhone 13
            createItem(iphone13, "BATCH-2024-003", new BigDecimal("7"), 
                    new BigDecimal("65000.00"), new BigDecimal("79990.00"), 
                    shelfA12, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(1), null);
            
            createItem(iphone13, "BATCH-2024-004", new BigDecimal("5"), 
                    new BigDecimal("64000.00"), new BigDecimal("79990.00"), 
                    shelfA12, ItemStatus.IN_STOCK, LocalDate.now().minusWeeks(2), null);

            // Xiaomi Mi 11
            createItem(xiaomiMi11, "BATCH-2024-005", new BigDecimal("15"), 
                    new BigDecimal("35000.00"), new BigDecimal("49990.00"), 
                    shelfA21, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(1), null);
            
            createItem(xiaomiMi11, "BATCH-2024-006", new BigDecimal("12"), 
                    new BigDecimal("34500.00"), new BigDecimal("49990.00"), 
                    shelfA21, ItemStatus.IN_STOCK, LocalDate.now().minusWeeks(3), null);

            // Huawei P40
            createItem(huaweiP40, "BATCH-2024-007", new BigDecimal("6"), 
                    new BigDecimal("55000.00"), new BigDecimal("69990.00"), 
                    shelfB11, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(2), null);
            
            createItem(huaweiP40, "BATCH-2024-008", new BigDecimal("4"), 
                    new BigDecimal("54000.00"), new BigDecimal("69990.00"), 
                    shelfB11, ItemStatus.IN_STOCK, LocalDate.now().minusWeeks(1), null);

            // Чехлы
            createItem(cases, "BATCH-2024-009", new BigDecimal("50"), 
                    new BigDecimal("150.00"), new BigDecimal("299.00"), 
                    shelfB12, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(1), null);
            
            createItem(cases, "BATCH-2024-010", new BigDecimal("45"), 
                    new BigDecimal("140.00"), new BigDecimal("299.00"), 
                    shelfB12, ItemStatus.IN_STOCK, LocalDate.now().minusWeeks(2), null);

            // Зарядные устройства
            createItem(charger, "BATCH-2024-011", new BigDecimal("30"), 
                    new BigDecimal("800.00"), new BigDecimal("1490.00"), 
                    shelf2A11, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(1), null);
            
            createItem(charger, "BATCH-2024-012", new BigDecimal("25"), 
                    new BigDecimal("780.00"), new BigDecimal("1490.00"), 
                    shelf2A11, ItemStatus.IN_STOCK, LocalDate.now().minusWeeks(1), null);

            // Наушники
            createItem(headphones, "BATCH-2024-013", new BigDecimal("20"), 
                    new BigDecimal("2000.00"), new BigDecimal("3990.00"), 
                    shelf2A12, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(1), null);
            
            createItem(headphones, "BATCH-2024-014", new BigDecimal("18"), 
                    new BigDecimal("1950.00"), new BigDecimal("3990.00"), 
                    shelf2A12, ItemStatus.IN_STOCK, LocalDate.now().minusWeeks(2), null);

            // Защитные стёкла
            createItem(screenProtector, "BATCH-2024-015", new BigDecimal("60"), 
                    new BigDecimal("100.00"), new BigDecimal("199.00"), 
                    shelfB12, ItemStatus.IN_STOCK, LocalDate.now().minusMonths(1), null);
            
            createItem(screenProtector, "BATCH-2024-016", new BigDecimal("55"), 
                    new BigDecimal("95.00"), new BigDecimal("199.00"), 
                    shelfB12, ItemStatus.IN_STOCK, LocalDate.now().minusWeeks(1), null);

            // Создаём несколько проданных позиций для истории
            createItem(iphone13, "BATCH-2024-017", new BigDecimal("0"), 
                    new BigDecimal("65000.00"), new BigDecimal("79990.00"), 
                    shelfA12, ItemStatus.SOLD, LocalDate.now().minusMonths(2), null);
            
            createItem(xiaomiMi11, "BATCH-2024-018", new BigDecimal("0"), 
                    new BigDecimal("35000.00"), new BigDecimal("49990.00"), 
                    shelfA21, ItemStatus.SOLD, LocalDate.now().minusWeeks(3), null);

            logger.info("База данных успешно заполнена тестовыми данными!");
            logger.info("Создано:");
            logger.info("  - Производителей: {}", manufacturerDao.count());
            logger.info("  - Складов: {}", warehouseDao.count());
            logger.info("  - Полок: {}", shelfDao.count());
            logger.info("  - Номенклатуры: {}", nomenclatureDao.count());
            logger.info("  - Товарных позиций: {}", itemDao.count());

        } catch (Exception e) {
            logger.error("Ошибка при заполнении базы данными", e);
            throw new RuntimeException("Не удалось заполнить базу данными: " + e.getMessage(), e);
        }
    }

    private Manufacturer createManufacturer(String name, String country, String contactInfo) {
        Manufacturer manufacturer = new Manufacturer(name, country, contactInfo);
        return manufacturerDao.save(manufacturer);
    }

    private Warehouse createWarehouse(String name, String address, Boolean isActive) {
        Warehouse warehouse = new Warehouse(name, address, isActive);
        return warehouseDao.save(warehouse);
    }

    private Shelf createShelf(Warehouse warehouse, String code, String description, Integer capacity) {
        Shelf shelf = new Shelf(warehouse, code, description, capacity, true);
        return shelfDao.save(shelf);
    }

    private Nomenclature createNomenclature(String article, String name, String description, 
                                           String unit, Manufacturer manufacturer, Integer minStockLevel) {
        Nomenclature nomenclature = new Nomenclature(article, name, description, unit, manufacturer, minStockLevel);
        return nomenclatureDao.save(nomenclature);
    }

    private Item createItem(Nomenclature nomenclature, String batchNumber, BigDecimal quantity,
                          BigDecimal purchasePrice, BigDecimal sellingPrice, Shelf shelf, 
                          ItemStatus status, LocalDate manufactureDate, LocalDate expiryDate) {
        Item item = new Item(nomenclature, batchNumber, quantity, purchasePrice, sellingPrice, shelf, status);
        item.setManufactureDate(manufactureDate);
        item.setExpiryDate(expiryDate);
        return itemDao.save(item);
    }
}

