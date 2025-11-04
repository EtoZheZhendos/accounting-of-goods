package com.store.inventory.service;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для формирования отчётов
 */
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ItemDao itemDao = new ItemDao();
    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final DocumentDao documentDao = new DocumentDao();
    private final HistoryDao historyDao = new HistoryDao();

    /**
     * Получить остатки товаров на складе (общие, без разбивки по складам)
     */
    public Map<Nomenclature, BigDecimal> getStockReport() {
        Map<Nomenclature, BigDecimal> stockReport = new HashMap<>();

        List<Nomenclature> allNomenclature = nomenclatureDao.findAll();

        for (Nomenclature nomenclature : allNomenclature) {
            BigDecimal totalQuantity = itemDao.getTotalQuantityByNomenclatureAndStatus(
                    nomenclature,
                    ItemStatus.IN_STOCK
            );
            stockReport.put(nomenclature, totalQuantity);
        }

        return stockReport;
    }
    
    /**
     * Получить остатки товаров с разбивкой по складам
     * @return список объектов [nomenclature, warehouse, quantity]
     */
    public List<Object[]> getStockReportByWarehouse() {
        return itemDao.getStockByWarehouse();
    }

    /**
     * Получить отчёт по товарам с низким запасом
     */
    public List<Nomenclature> getLowStockReport() {
        return nomenclatureDao.findLowStockItems();
    }

    /**
     * Получить отчёт по просроченным товарам
     */
    public List<Item> getExpiredItemsReport() {
        return itemDao.findExpiredItems();
    }

    /**
     * Получить отчёт по товарам с истекающим сроком годности
     */
    public List<Item> getExpiringItemsReport(int daysBeforeExpiry) {
        return itemDao.findExpiringItems(daysBeforeExpiry);
    }

    /**
     * Получить отчёт по продажам за период
     */
    public Map<String, Object> getSalesReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // Получаем все документы продаж за период
        List<Document> salesDocuments = documentDao.findByDateRange(startDate, endDate).stream()
                .filter(doc -> doc.getDocumentType() == DocumentType.SALE)
                .filter(doc -> doc.getStatus() == DocumentStatus.CONFIRMED)
                .toList();

        // Общая сумма продаж
        BigDecimal totalSales = salesDocuments.stream()
                .map(Document::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Количество документов
        int documentCount = salesDocuments.size();

        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalSales", totalSales);
        report.put("documentCount", documentCount);
        report.put("documents", salesDocuments);

        return report;
    }

    /**
     * Получить отчёт по поступлениям за период
     */
    public Map<String, Object> getReceiptReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        List<Document> receiptDocuments = documentDao.findByDateRange(startDate, endDate).stream()
                .filter(doc -> doc.getDocumentType() == DocumentType.RECEIPT)
                .filter(doc -> doc.getStatus() == DocumentStatus.CONFIRMED)
                .toList();

        BigDecimal totalReceipts = receiptDocuments.stream()
                .map(Document::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int documentCount = receiptDocuments.size();

        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalReceipts", totalReceipts);
        report.put("documentCount", documentCount);
        report.put("documents", receiptDocuments);

        return report;
    }

    /**
     * Получить историю операций за период
     */
    public List<History> getOperationHistoryReport(LocalDateTime startDate, LocalDateTime endDate) {
        return historyDao.findByDateRange(startDate, endDate);
    }

    /**
     * Получить отчёт по товарам на полке
     */
    public Map<String, Object> getShelfReport(Shelf shelf) {
        Map<String, Object> report = new HashMap<>();

        List<Item> items = itemDao.findByShelf(shelf);

        BigDecimal totalValue = items.stream()
                .map(Item::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int itemCount = items.size();

        report.put("shelf", shelf);
        report.put("items", items);
        report.put("itemCount", itemCount);
        report.put("totalValue", totalValue);

        return report;
    }

    /**
     * Получить сводку по складу
     */
    public Map<String, Object> getWarehouseSummary(Warehouse warehouse) {
        Map<String, Object> summary = new HashMap<>();

        // Получаем все полки склада
        List<Item> allItems = itemDao.findAll().stream()
                .filter(item -> item.getCurrentShelf() != null)
                .filter(item -> item.getCurrentShelf().getWarehouse().equals(warehouse))
                .filter(item -> item.getStatus() == ItemStatus.IN_STOCK)
                .toList();

        int totalItems = allItems.size();

        BigDecimal totalValue = allItems.stream()
                .map(Item::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalQuantity = allItems.stream()
                .map(Item::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        summary.put("warehouse", warehouse);
        summary.put("totalItems", totalItems);
        summary.put("totalQuantity", totalQuantity);
        summary.put("totalValue", totalValue);

        return summary;
    }
}

