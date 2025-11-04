package com.store.inventory.domain;

/**
 * Перечисление статусов документа
 * 
 * <p>Определяет жизненный цикл документа в системе:
 * черновик, проведённый или отменённый.</p>
 */
public enum DocumentStatus {
    /** Черновик - документ создан, но не проведён */
    DRAFT("Черновик"),
    
    /** Проведён - документ обработан и повлиял на остатки */
    CONFIRMED("Проведён"),
    
    /** Отменён - документ аннулирован */
    CANCELLED("Отменён");

    /** Отображаемое наименование статуса */
    private final String displayName;

    /**
     * Создает статус документа с указанным отображаемым наименованием
     * 
     * @param displayName отображаемое наименование
     */
    DocumentStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает отображаемое наименование статуса
     * 
     * @return отображаемое наименование
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
