package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Производитель"
 * 
 * <p>Представляет производителя товаров в системе учета.
 * Содержит информацию о наименовании, стране производства и контактных данных.
 * Связан с номенклатурой товаров отношением один-ко-многим.</p>
 * 
 */
@Entity
@Table(name = "manufacturer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "nomenclatures")
@EqualsAndHashCode(of = "id")
public class Manufacturer {

    /** Уникальный идентификатор производителя */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Наименование производителя (уникальное) */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /** Страна производителя */
    @Column(name = "country")
    private String country;

    /** Контактная информация (телефон, email, адрес) */
    @Column(name = "contact_info", columnDefinition = "TEXT")
    private String contactInfo;

    /** Дата и время создания записи */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления записи */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Список номенклатуры данного производителя */
    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL)
    private List<Nomenclature> nomenclatures = new ArrayList<>();

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
     * Создает производителя с указанными параметрами
     * 
     * @param name наименование производителя
     * @param country страна производителя
     * @param contactInfo контактная информация
     */
    public Manufacturer(String name, String country, String contactInfo) {
        this.name = name;
        this.country = country;
        this.contactInfo = contactInfo;
    }
}

