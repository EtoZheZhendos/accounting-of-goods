package com.store.inventory.domain;

/**
 * Перечисление статусов документа
 */
public enum DocumentStatus {
    DRAFT("Черновик"),
    CONFIRMED("Проведён"),
    CANCELLED("Отменён");

    private final String displayName;

    DocumentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

