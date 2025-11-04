package com.store.inventory.domain;

/**
 * Перечисление типов операций в истории
 */
public enum OperationType {
    RECEIPT("Поступление"),
    SALE("Продажа"),
    MOVEMENT("Перемещение"),
    WRITE_OFF("Списание"),
    STATUS_CHANGE("Изменение статуса"),
    INVENTORY("Инвентаризация"),
    RETURN("Возврат");

    private final String displayName;

    OperationType(String displayName) {
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

