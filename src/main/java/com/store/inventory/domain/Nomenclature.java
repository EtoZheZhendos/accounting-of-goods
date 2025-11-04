package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Номенклатура"
 * 
 * <p>Представляет справочник товаров в системе учета.
 * Содержит общую информацию о товаре: артикул, наименование, описание, единицы измерения.
 * Связана с производителем и конкретными товарными позициями (Items).</p>
 * 
 */
@Entity
@Table(name = "nomenclature")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"items", "manufacturer"})
@EqualsAndHashCode(of = "id")
public class Nomenclature {

    /** Уникальный идентификатор номенклатуры */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Артикул товара (уникальный) */
    @Column(name = "article", nullable = false, unique = true, length = 50)
    private String article;

    /** Наименование товара */
    @Column(name = "name", nullable = false)
    private String name;

    /** Подробное описание товара */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Единица измерения (шт, кг, л, м и т.д.) */
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    /** Производитель товара */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    /** Минимальный уровень запаса на складе */
    @Column(name = "min_stock_level")
    private Integer minStockLevel = 0;

    /** Дата и время создания записи */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления записи */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Список товарных позиций данной номенклатуры */
    @OneToMany(mappedBy = "nomenclature", cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    /**
     * Автоматически устанавливает дату создания и обновления перед сохранением новой записи
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
     * Создает номенклатуру с указанными параметрами
     * 
     * @param article артикул товара
     * @param name наименование товара
     * @param description описание товара
     * @param unit единица измерения
     * @param manufacturer производитель
     * @param minStockLevel минимальный уровень запаса
     */
    public Nomenclature(String article, String name, String description, String unit, Manufacturer manufacturer, Integer minStockLevel) {
        this.article = article;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.manufacturer = manufacturer;
        this.minStockLevel = minStockLevel;
    }
}

