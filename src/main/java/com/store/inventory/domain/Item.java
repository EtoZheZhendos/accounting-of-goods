package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Товарная позиция"
 * Конкретные экземпляры товаров на складе
 */
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"nomenclature", "currentShelf", "historyRecords"})
@EqualsAndHashCode(of = "id")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    private Nomenclature nomenclature;

    @Column(name = "batch_number", length = 100)
    private String batchNumber; // номер партии

    @Column(name = "serial_number", length = 100)
    private String serialNumber; // серийный номер

    @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice; // закупочная цена

    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice; // цена продажи

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_shelf_id")
    private Shelf currentShelf; // текущее местоположение

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ItemStatus status;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<History> historyRecords = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Item(Nomenclature nomenclature, String batchNumber, BigDecimal quantity,
                BigDecimal purchasePrice, BigDecimal sellingPrice, Shelf currentShelf, ItemStatus status) {
        this.nomenclature = nomenclature;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.currentShelf = currentShelf;
        this.status = status;
    }

    /**
     * Проверяет, истёк ли срок годности товара
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Возвращает полную стоимость партии (количество × цена продажи)
     */
    public BigDecimal getTotalValue() {
        if (sellingPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return sellingPrice.multiply(quantity);
    }

    /**
     * Возвращает общую стоимость закупки
     */
    public BigDecimal getTotalPurchaseCost() {
        if (purchasePrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return purchasePrice.multiply(quantity);
    }
}

