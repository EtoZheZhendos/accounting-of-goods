package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность "История"
 * 
 * <p>Хранит полную историю всех операций с товарными позициями.
 * Каждая запись фиксирует тип операции, изменения количества и статуса,
 * перемещения между полками, а также связь с документом-основанием.</p>
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

    /** Уникальный идентификатор записи истории */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Товарная позиция, с которой произведена операция */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** Документ-основание операции (может быть NULL для ручных операций) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id")
    private Document document;

    /** Тип операции (поступление, реализация, перемещение, списание) */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 50)
    private OperationType operationType;

    /** Изменение количества товара (положительное для поступления, отрицательное для расхода) */
    @Column(name = "quantity_change", precision = 10, scale = 3)
    private BigDecimal quantityChange;

    /** Цена товара на момент операции */
    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    /** Полка, откуда перемещен товар (для операций перемещения) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_shelf_id")
    private Shelf fromShelf;

    /** Полка, куда перемещен товар (для операций перемещения и поступления) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_shelf_id")
    private Shelf toShelf;

    /** Статус товара до операции */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private ItemStatus fromStatus;

    /** Статус товара после операции */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 20)
    private ItemStatus toStatus;

    /** Дата и время выполнения операции */
    @Column(name = "operation_date", nullable = false)
    private LocalDateTime operationDate;

    /** Примечания к операции */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Пользователь, выполнивший операцию */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /** Дата и время создания записи в истории */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Автоматически устанавливает даты создания и операции перед сохранением записи
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (operationDate == null) {
            operationDate = LocalDateTime.now();
        }
    }

    /**
     * Создает запись истории с указанными параметрами
     * 
     * @param item товарная позиция
     * @param document документ-основание
     * @param operationType тип операции
     * @param quantityChange изменение количества
     * @param price цена
     * @param fromShelf полка-источник
     * @param toShelf полка-приемник
     * @param fromStatus статус до операции
     * @param toStatus статус после операции
     * @param createdBy пользователь
     * @param notes примечания
     */
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

