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
 * Базовая сущность для всех документов (поступление, реализация, перемещение, списание)
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(name = "counterparty")
    private String counterparty; // контрагент (поставщик/покупатель)

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentItem> documentItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
     * Добавляет строку документа
     */
    public void addDocumentItem(DocumentItem item) {
        documentItems.add(item);
        item.setDocument(this);
    }

    /**
     * Удаляет строку документа
     */
    public void removeDocumentItem(DocumentItem item) {
        documentItems.remove(item);
        item.setDocument(null);
    }

    /**
     * Пересчитывает общую сумму документа
     */
    public void recalculateTotalAmount() {
        totalAmount = documentItems.stream()
                .map(DocumentItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

