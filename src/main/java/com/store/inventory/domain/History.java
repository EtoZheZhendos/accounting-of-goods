package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность "История"
 * История всех операций с товарами
 */
@Entity
@Table(name = "history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"item", "document", "fromShelf", "toShelf"})
@EqualsAndHashCode(of = "id")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 50)
    private OperationType operationType;

    @Column(name = "quantity_change", precision = 10, scale = 3)
    private BigDecimal quantityChange; // изменение количества (+/-)

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_shelf_id")
    private Shelf fromShelf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_shelf_id")
    private Shelf toShelf;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private ItemStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 20)
    private ItemStatus toStatus;

    @Column(name = "operation_date", nullable = false)
    private LocalDateTime operationDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (operationDate == null) {
            operationDate = LocalDateTime.now();
        }
    }

    public History(Item item, Document document, OperationType operationType,
                   BigDecimal quantityChange, BigDecimal price, Shelf fromShelf, Shelf toShelf,
                   ItemStatus fromStatus, ItemStatus toStatus, String createdBy, String notes) {
        this.item = item;
        this.document = document;
        this.operationType = operationType;
        this.quantityChange = quantityChange;
        this.price = price;
        this.fromShelf = fromShelf;
        this.toShelf = toShelf;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.createdBy = createdBy;
        this.notes = notes;
        this.operationDate = LocalDateTime.now();
    }
}

