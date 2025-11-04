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
 * 
 * <p>Представляет конкретный экземпляр товара на складе.
 * Товарная позиция связана с номенклатурой и хранит информацию о партии, количестве,
 * ценах, местоположении и сроках годности. Каждая позиция имеет статус и историю операций.</p>
 * 
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

    /** Уникальный идентификатор товарной позиции */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Номенклатура товара */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    private Nomenclature nomenclature;

    /** Номер партии товара */
    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    /** Серийный номер товара */
    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    /** Количество товара */
    @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    /** Закупочная цена за единицу товара */
    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    /** Цена продажи за единицу товара */
    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    /** Текущее местоположение товара (полка на складе) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_shelf_id")
    private Shelf currentShelf;

    /** Статус товарной позиции */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ItemStatus status;

    /** Дата производства товара */
    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    /** Срок годности товара */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /** Дата и время создания записи */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления записи */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** История операций с данной товарной позицией */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<History> historyRecords = new ArrayList<>();

    /**
     * Автоматически устанавливает даты создания и обновления перед сохранением новой записи
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Автоматически обновляет дату изменения при обновлении записи
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Создает товарную позицию с указанными параметрами
     * 
     * @param nomenclature номенклатура товара
     * @param batchNumber номер партии
     * @param quantity количество
     * @param purchasePrice закупочная цена
     * @param sellingPrice цена продажи
     * @param currentShelf текущая полка
     * @param status статус позиции
     */
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
     * 
     * @return true, если срок годности истёк; false в противном случае
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Вычисляет общую стоимость партии товара
     * 
     * <p>Стоимость рассчитывается как: количество × цена продажи</p>
     * 
     * @return общая стоимость партии или ZERO, если цена или количество не указаны
     */
    public BigDecimal getTotalValue() {
        if (sellingPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return sellingPrice.multiply(quantity);
    }

    /**
     * Вычисляет общую стоимость закупки партии товара
     * 
     * <p>Стоимость рассчитывается как: количество × закупочная цена</p>
     * 
     * @return общая стоимость закупки или ZERO, если цена или количество не указаны
     */
    public BigDecimal getTotalPurchaseCost() {
        if (purchasePrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return purchasePrice.multiply(quantity);
    }
}

