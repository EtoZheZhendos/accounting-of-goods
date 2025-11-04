package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность "Строка документа"
 * Товарные позиции в документах
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id", nullable = false)
    private Nomenclature nomenclature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // ссылка на конкретную товарную позицию (может быть NULL для новых поступлений)

    @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_id")
    private Shelf shelf; // полка для размещения/списания

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotal();
    }

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
     */
    public void calculateTotal() {
        if (quantity != null && price != null) {
            this.total = quantity.multiply(price);
        }
    }
}

