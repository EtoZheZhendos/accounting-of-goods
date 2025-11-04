package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность "Строка документа"
 * 
 * <p>Представляет одну позицию (строку) в документе движения товаров.
 * Содержит информацию о товаре, количестве, цене и месте размещения.
 * Может быть связана с конкретной товарной позицией (Item) или только с номенклатурой.</p>
 */
@Entity
@Table(name = "document_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"document", "nomenclature", "item", "shelf"})
@EqualsAndHashCode(of = "id")
public class DocumentItem {

    /** Уникальный идентификатор строки документа */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Документ, к которому относится строка */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /** Номенклатура товара */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    private Nomenclature nomenclature;

    /** Ссылка на конкретную товарную позицию (может быть NULL для новых поступлений) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Item item;

    /** Количество товара в строке */
    @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    /** Цена за единицу товара */
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Итоговая сумма строки (количество × цена) */
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /** Полка для размещения или списания товара */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;

    /** Дата и время создания строки */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Автоматически устанавливает дату создания и рассчитывает итоговую сумму перед сохранением
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateTotal();
    }

    /**
     * Автоматически пересчитывает итоговую сумму при обновлении строки
     */
    @PreUpdate
    protected void onUpdate() {
        calculateTotal();
    }

    /**
     * Создает строку документа с указанными параметрами
     * 
     * @param document документ
     * @param nomenclature номенклатура
     * @param quantity количество
     * @param price цена
     * @param shelf полка
     */
    public DocumentItem(Document document, Nomenclature nomenclature, BigDecimal quantity,
                        BigDecimal price, Shelf shelf) {
        this.document = document;
        this.nomenclature = nomenclature;
        this.quantity = quantity;
        this.price = price;
        this.shelf = shelf;
        calculateTotal();
    }

    /**
     * Рассчитывает итоговую сумму строки
     * 
     * <p>Сумма вычисляется как: количество × цена.</p>
     */
    public void calculateTotal() {
        if (quantity != null && price != null) {
            this.total = quantity.multiply(price);
        }
    }
}

