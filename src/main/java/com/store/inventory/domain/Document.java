package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Документ"
 * 
 * <p>Представляет документ движения товаров в системе учета.
 * Является базовой сущностью для всех типов документов: поступление, реализация, 
 * перемещение и списание. Каждый документ содержит строки (позиции) и имеет статус.</p>
 */
@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"warehouse", "documentItems"})
@EqualsAndHashCode(of = "id")
public class Document {

    /** Уникальный идентификатор документа */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Тип документа (поступление, реализация, перемещение, списание) */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    /** Номер документа (уникальный) */
    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    /** Дата документа */
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    /** Склад, с которым связан документ */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    /** Контрагент (поставщик или покупатель) */
    @Column(name = "counterparty")
    private String counterparty;

    /** Общая сумма документа */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** Статус документа (черновик, проведен, отменен) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    /** Примечания к документу */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Пользователь, создавший документ */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /** Дата и время создания документа */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления документа */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Строки документа (товарные позиции) */
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentItem> documentItems = new ArrayList<>();

    /**
     * Автоматически устанавливает даты создания и обновления перед сохранением нового документа
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Автоматически обновляет дату изменения при обновлении документа
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Создает документ с указанными параметрами
     * 
     * @param documentType тип документа
     * @param documentNumber номер документа
     * @param documentDate дата документа
     * @param warehouse склад
     * @param counterparty контрагент
     * @param status статус документа
     * @param createdBy пользователь-создатель
     */
    public Document(DocumentType documentType, String documentNumber, LocalDate documentDate,
                    Warehouse warehouse, String counterparty, DocumentStatus status, String createdBy) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.documentDate = documentDate;
        this.warehouse = warehouse;
        this.counterparty = counterparty;
        this.status = status;
        this.createdBy = createdBy;
    }

    /**
     * Добавляет строку в документ
     * 
     * <p>Автоматически устанавливает связь между документом и строкой.</p>
     * 
     * @param item добавляемая строка документа
     */
    public void addDocumentItem(DocumentItem item) {
        documentItems.add(item);
        item.setDocument(this);
    }

    /**
     * Удаляет строку из документа
     * 
     * <p>Автоматически разрывает связь между документом и строкой.</p>
     * 
     * @param item удаляемая строка документа
     */
    public void removeDocumentItem(DocumentItem item) {
        documentItems.remove(item);
        item.setDocument(null);
    }

    /**
     * Пересчитывает общую сумму документа
     * 
     * <p>Сумма вычисляется как сумма всех строк документа.
     * Каждая строка вычисляется как: количество × цена.</p>
     */
    public void recalculateTotalAmount() {
        totalAmount = documentItems.stream()
                .map(DocumentItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

