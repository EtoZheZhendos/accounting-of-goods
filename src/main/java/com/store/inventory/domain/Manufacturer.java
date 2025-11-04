package com.store.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Производитель"
 * Справочник производителей товаров
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "country")
    private String country;

    @Column(name = "contact_info", columnDefinition = "TEXT")
    private String contactInfo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL)
    private List<Nomenclature> nomenclatures = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Manufacturer(String name, String country, String contactInfo) {
        this.name = name;
        this.country = country;
        this.contactInfo = contactInfo;
    }
}

