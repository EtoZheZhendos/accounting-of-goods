package com.store.inventory.controller;

import java.math.BigDecimal;

/**
 * Класс для отображения остатков товаров по складам
 */
public class StockReportItem {
    private final String article;
    private final String name;
    private final String manufacturer;
    private final String warehouse;
    private final BigDecimal quantity;
    private final String unit;

    public StockReportItem(String article, String name, String manufacturer, 
                          String warehouse, BigDecimal quantity, String unit) {
        this.article = article;
        this.name = name;
        this.manufacturer = manufacturer;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getArticle() {
        return article;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}

