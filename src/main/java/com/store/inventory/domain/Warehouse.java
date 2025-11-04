package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Склад"
 * 
 * <p>Представляет склад в системе учета товаров.
 * Содержит информацию о наименовании, адресе и статусе активности.
 * Связан с полками отношением один-ко-многим.</p>
 * 
 */
@Entity
@Table(name = "warehouse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "shelves")
@EqualsAndHashCode(of = "id")
public class Warehouse {

    /** Уникальный идентификатор склада */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Наименование склада (уникальное) */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /** Адрес склада */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /** Признак активности склада */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /** Дата и время создания записи */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Список полок данного склада */
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<Shelf> shelves = new ArrayList<>();

    /**
     * Автоматически устанавливает дату создания перед сохранением новой записи
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Создает склад с указанными параметрами
     * 
     * @param name наименование склада
     * @param address адрес склада
     * @param isActive признак активности
     */
    public Warehouse(String name, String address, Boolean isActive) {
        this.name = name;
        this.address = address;
        this.isActive = isActive;
    }
}

