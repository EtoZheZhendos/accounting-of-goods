-- V1: Создание базовой схемы базы данных

-- 1. Таблица производителей
CREATE TABLE manufacturer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    country VARCHAR(100),
    contact_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Таблица номенклатуры товаров
CREATE TABLE nomenclature (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    unit VARCHAR(20) NOT NULL,
    manufacturer_id BIGINT,
    min_stock_level INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manufacturer_id) REFERENCES manufacturer(id)
);

-- 3. Таблица складов
CREATE TABLE warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Таблица полок/стеллажей
CREATE TABLE shelf (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    capacity INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
    UNIQUE (warehouse_id, code)
);

-- 5. Таблица товарных позиций
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nomenclature_id BIGINT NOT NULL,
    batch_number VARCHAR(100),
    serial_number VARCHAR(100),
    quantity DECIMAL(10, 3) NOT NULL,
    purchase_price DECIMAL(12, 2),
    selling_price DECIMAL(12, 2),
    current_shelf_id BIGINT,
    status VARCHAR(20) NOT NULL,
    manufacture_date DATE,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nomenclature_id) REFERENCES nomenclature(id),
    FOREIGN KEY (current_shelf_id) REFERENCES shelf(id)
);

-- 6. Таблица документов
CREATE TABLE document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(50) NOT NULL UNIQUE,
    document_date DATE NOT NULL,
    warehouse_id BIGINT,
    counterparty VARCHAR(255),
    total_amount DECIMAL(12, 2),
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
);

-- 7. Таблица строк документов
CREATE TABLE document_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    nomenclature_id BIGINT NOT NULL,
    item_id BIGINT,
    quantity DECIMAL(10, 3) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    shelf_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES document(id) ON DELETE CASCADE,
    FOREIGN KEY (nomenclature_id) REFERENCES nomenclature(id),
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (shelf_id) REFERENCES shelf(id)
);

-- 8. Таблица истории операций
CREATE TABLE history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    document_id BIGINT,
    operation_type VARCHAR(50) NOT NULL,
    quantity_change DECIMAL(10, 3),
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

-- Создание индексов для оптимизации запросов
CREATE INDEX idx_nomenclature_article ON nomenclature(article);
CREATE INDEX idx_nomenclature_manufacturer ON nomenclature(manufacturer_id);
CREATE INDEX idx_items_nomenclature ON items(nomenclature_id);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_shelf ON items(current_shelf_id);
CREATE INDEX idx_items_batch ON items(batch_number);
CREATE INDEX idx_document_date ON document(document_date);
CREATE INDEX idx_document_type ON document(document_type);
CREATE INDEX idx_document_warehouse ON document(warehouse_id);
CREATE INDEX idx_document_items_doc ON document_items(document_id);
CREATE INDEX idx_document_items_nomenclature ON document_items(nomenclature_id);
CREATE INDEX idx_history_item ON history(item_id);
CREATE INDEX idx_history_operation_date ON history(operation_date);
CREATE INDEX idx_history_document ON history(document_id);
CREATE INDEX idx_shelf_warehouse ON shelf(warehouse_id);

