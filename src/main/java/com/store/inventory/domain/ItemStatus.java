package com.store.inventory.domain;

/**
 * Перечисление статусов товарной позиции
 */
public enum ItemStatus {
    IN_STOCK("На складе"),
    SOLD("Продано"),
    RESERVED("Зарезервировано"),
    DAMAGED("Повреждено"),
    EXPIRED("Просрочено"),
    RETURNED("Возвращено");

    private final String displayName;

    ItemStatus(String displayName) {
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

