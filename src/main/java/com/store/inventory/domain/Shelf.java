package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Полка/Стеллаж"
 * Места хранения на складах
 */
@Entity
@Table(name = "shelf", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"warehouse_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"warehouse", "items"})
@EqualsAndHashCode(of = "id")
public class Shelf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "code", nullable = false, length = 50)
    private String code; // например: A-1-5 (ряд-стеллаж-полка)

    @Column(name = "description")
    private String description;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "currentShelf", cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Shelf(Warehouse warehouse, String code, String description, Integer capacity, Boolean isActive) {
        this.warehouse = warehouse;
        this.code = code;
        this.description = description;
        this.capacity = capacity;
        this.isActive = isActive;
    }

    /**
     * Возвращает полный адрес полки (имя склада + код полки)
     */
    public String getFullAddress() {
        return warehouse.getName() + " / " + code;
    }
}

