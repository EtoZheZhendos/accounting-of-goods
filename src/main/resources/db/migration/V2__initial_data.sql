-- V2: Добавление начальных данных для тестирования

-- Добавление производителей
INSERT INTO manufacturer (name, country, contact_info) VALUES
('ООО "Рассвет"', 'Россия', 'тел: +7-495-123-45-67, email: info@rassvet.ru'),
('Samsung Electronics', 'Южная Корея', 'www.samsung.com'),
('Apple Inc.', 'США', 'www.apple.com'),
('Xiaomi Corporation', 'Китай', 'www.mi.com');

-- Добавление складов
INSERT INTO warehouse (name, address, is_active) VALUES
('Центральный склад', 'г. Москва, ул. Складская, д. 1', TRUE),
('Склад №2', 'г. Москва, ул. Торговая, д. 15', TRUE),
('Резервный склад', 'г. Подольск, пр. Ленина, д. 50', TRUE);

-- Добавление полок для Центрального склада
INSERT INTO shelf (warehouse_id, code, description, capacity, is_active) VALUES
(1, 'A-1-1', 'Ряд A, Стеллаж 1, Полка 1', 100, TRUE),
(1, 'A-1-2', 'Ряд A, Стеллаж 1, Полка 2', 100, TRUE),
(1, 'A-2-1', 'Ряд A, Стеллаж 2, Полка 1', 150, TRUE),
(1, 'B-1-1', 'Ряд B, Стеллаж 1, Полка 1', 100, TRUE),
(1, 'B-1-2', 'Ряд B, Стеллаж 1, Полка 2', 100, TRUE);

-- Добавление полок для Склада №2
INSERT INTO shelf (warehouse_id, code, description, capacity, is_active) VALUES
(2, 'A-1-1', 'Ряд A, Стеллаж 1, Полка 1', 80, TRUE),
(2, 'A-1-2', 'Ряд A, Стеллаж 1, Полка 2', 80, TRUE),
(2, 'B-1-1', 'Ряд B, Стеллаж 1, Полка 1', 80, TRUE);

-- Добавление номенклатуры
INSERT INTO nomenclature (article, name, description, unit, manufacturer_id, min_stock_level) VALUES
('SM-G990', 'Samsung Galaxy S21', 'Смартфон Samsung Galaxy S21 128GB', 'шт', 2, 5),
('IPHONE-13', 'Apple iPhone 13', 'Смартфон Apple iPhone 13 128GB', 'шт', 3, 3),
('MI-11', 'Xiaomi Mi 11', 'Смартфон Xiaomi Mi 11 256GB', 'шт', 4, 5),
('CASE-001', 'Чехол универсальный', 'Защитный чехол для смартфонов', 'шт', 1, 20),
('CHARGER-USB-C', 'Зарядное устройство USB-C', 'Быстрая зарядка 25W', 'шт', 2, 15);

