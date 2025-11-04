package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Полка/Стеллаж"
 * 
 * <p>Представляет место хранения товаров на складе.
 * Каждая полка имеет уникальный код в пределах склада и может содержать товарные позиции.
 * Код полки строится по принципу: ряд-стеллаж-полка (например, A-1-5).</p>
 * 
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

    /** Уникальный идентификатор полки */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Склад, к которому относится полка */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    /** Код полки (уникален в пределах склада) */
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    /** Описание полки */
    @Column(name = "description")
    private String description;

    /** Вместимость полки (в единицах товара) */
    @Column(name = "capacity")
    private Integer capacity;

    /** Признак активности полки */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /** Дата и время создания записи */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Список товарных позиций, находящихся на данной полке */
    @OneToMany(mappedBy = "currentShelf", cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    /**
     * Автоматически устанавливает дату создания перед сохранением новой записи
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Создает полку с указанными параметрами
     * 
     * @param warehouse склад
     * @param code код полки
     * @param description описание
     * @param capacity вместимость
     * @param isActive признак активности
     */
    public Shelf(Warehouse warehouse, String code, String description, Integer capacity, Boolean isActive) {
        this.warehouse = warehouse;
        this.code = code;
        this.description = description;
        this.capacity = capacity;
        this.isActive = isActive;
    }

    /**
     * Возвращает полный адрес полки
     * 
     * <p>Адрес формируется как: "Название склада / Код полки"</p>
     * 
     * @return полный адрес полки в формате "Склад / Код"
     */
    public String getFullAddress() {
        return warehouse.getName() + " / " + code;
    }
}

