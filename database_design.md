## Схема базы данных

### 1. manufacturer (Производитель)
Справочник производителей товаров.

```sql
CREATE TABLE manufacturer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    country VARCHAR(100),
    contact_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. nomenclature (Номенклатура)
Справочник товаров (общая информация о товаре).

```sql
CREATE TABLE nomenclature (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    unit VARCHAR(20) NOT NULL, -- шт, кг, л, м и т.д.
    manufacturer_id BIGINT,
    min_stock_level INT DEFAULT 0, -- минимальный остаток
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manufacturer_id) REFERENCES manufacturer(id)
);
```

### 3. warehouse (Склад)
Справочник складов.

```sql
CREATE TABLE warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4. shelf (Полка/Стеллаж)
Места хранения на складах.

```sql
CREATE TABLE shelf (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL, -- например: A-1-5 (ряд-стеллаж-полка)
    description VARCHAR(255),
    capacity INT, -- вместимость (опционально)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
    UNIQUE (warehouse_id, code)
);
```

### 5. items (Товарные позиции)
Конкретные экземпляры товаров.

```sql
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nomenclature_id BIGINT NOT NULL,
    batch_number VARCHAR(100), -- номер партии
    serial_number VARCHAR(100), -- серийный номер (если применимо)
    quantity DECIMAL(10, 3) NOT NULL, -- количество
    purchase_price DECIMAL(12, 2), -- закупочная цена
    selling_price DECIMAL(12, 2), -- цена продажи
    current_shelf_id BIGINT, -- текущее местоположение
    status VARCHAR(20) NOT NULL, -- IN_STOCK, SOLD, RESERVED, DAMAGED
    manufacture_date DATE,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nomenclature_id) REFERENCES nomenclature(id),
    FOREIGN KEY (current_shelf_id) REFERENCES shelf(id)
);
```

### 6. document (Документ - базовая таблица)
Базовая таблица для всех документов.

```sql
CREATE TABLE document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type VARCHAR(50) NOT NULL, -- RECEIPT, SALE, MOVEMENT, WRITE_OFF
    document_number VARCHAR(50) NOT NULL UNIQUE,
    document_date DATE NOT NULL,
    warehouse_id BIGINT,
    counterparty VARCHAR(255), -- контрагент (поставщик/покупатель)
    total_amount DECIMAL(12, 2),
    status VARCHAR(20) NOT NULL, -- DRAFT, CONFIRMED, CANCELLED
    notes TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
);
```

### 7. document_items (Строки документа)
Товарные позиции в документах.

```sql
CREATE TABLE document_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    nomenclature_id BIGINT NOT NULL,
    item_id BIGINT, -- ссылка на конкретную товарную позицию (может быть NULL для новых поступлений)
    quantity DECIMAL(10, 3) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    shelf_id BIGINT, -- полка для размещения/списания
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES document(id) ON DELETE CASCADE,
    FOREIGN KEY (nomenclature_id) REFERENCES nomenclature(id),
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (shelf_id) REFERENCES shelf(id)
);
```

### 8. history (История операций)
История всех операций с товарами.

```sql
CREATE TABLE history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    document_id BIGINT,
    operation_type VARCHAR(50) NOT NULL, -- RECEIPT, SALE, MOVEMENT, WRITE_OFF, STATUS_CHANGE
    quantity_change DECIMAL(10, 3), -- изменение количества (+/-)
    price DECIMAL(12, 2),
    from_shelf_id BIGINT,
    to_shelf_id BIGINT,
    from_status VARCHAR(20),
    to_status VARCHAR(20),
    operation_date TIMESTAMP NOT NULL,
    notes TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (document_id) REFERENCES document(id),
    FOREIGN KEY (from_shelf_id) REFERENCES shelf(id),
    FOREIGN KEY (to_shelf_id) REFERENCES shelf(id)
);
```

## Индексы для оптимизации

```sql
-- Индексы для быстрого поиска
CREATE INDEX idx_nomenclature_article ON nomenclature(article);
CREATE INDEX idx_items_nomenclature ON items(nomenclature_id);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_shelf ON items(current_shelf_id);
CREATE INDEX idx_items_batch ON items(batch_number);
CREATE INDEX idx_document_date ON document(document_date);
CREATE INDEX idx_document_type ON document(document_type);
CREATE INDEX idx_document_items_doc ON document_items(document_id);
CREATE INDEX idx_history_item ON history(item_id);
CREATE INDEX idx_history_operation_date ON history(operation_date);
CREATE INDEX idx_shelf_warehouse ON shelf(warehouse_id);
```

## Проверка на циркулярность

Граф зависимостей:
```
manufacturer (корневая)
    ↓
nomenclature → manufacturer
    ↓
items → nomenclature
items → shelf → warehouse (корневая)
    ↓
history → items
history → shelf
history → document → warehouse

document_items → document
document_items → nomenclature
document_items → items
document_items → shelf
```

## Основные бизнес-операции

### Поступление товара:
1. Создать документ типа RECEIPT
2. Добавить строки в document_items
3. Создать записи в items (новые товары)
4. Записать в history операцию RECEIPT

### Реализация товара:
1. Создать документ типа SALE
2. Добавить строки в document_items (со ссылками на существующие items)
3. Обновить статус items на SOLD
4. Записать в history операцию SALE

### Перемещение товара:
1. Создать документ типа MOVEMENT
2. Обновить current_shelf_id в items
3. Записать в history операцию MOVEMENT

### Списание товара:
1. Создать документ типа WRITE_OFF
2. Обновить статус items на DAMAGED/EXPIRED
3. Записать в history операцию WRITE_OFF

