package com.store.inventory.domain;

/**
 * Перечисление типов документов
 */
public enum DocumentType {
    RECEIPT("Поступление"),
    SALE("Реализация"),
    MOVEMENT("Перемещение"),
    WRITE_OFF("Списание"),
    INVENTORY("Инвентаризация");

    private final String displayName;

    DocumentType(String displayName) {
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

