# Приложение "Учёт товаров в магазине"

Приложение для учёта товаров в магазине с возможностью:
- Регистрации поступления товаров
- Оформления продаж (реализации)
- Перемещения товаров между складами и полками
- Ведения истории операций
- Формирования отчётов

## Технологический стек

- **Backend**: Java 17 + Hibernate 6.3 + H2 Database
- **Frontend**: JavaFX 21
- **Build Tool**: Maven
- **Архитектура**: MVC + Repository Pattern + Service Layer

## Структура базы данных

### Основные сущности:

1. **Manufacturer** (Производитель)
   - Справочник производителей товаров

2. **Nomenclature** (Номенклатура)
   - Справочник товаров (артикул, название, единица измерения)

3. **Warehouse** (Склад)
   - Справочник складов

4. **Shelf** (Полка)
   - Места хранения на складах (ряд-стеллаж-полка)

5. **Item** (Товарная позиция)
   - Конкретные экземпляры товаров с количеством, ценами, статусом

6. **Document** (Документ)
   - Документы движения товаров (поступление, реализация, перемещение, списание)

7. **DocumentItem** (Строка документа)
   - Товарные позиции в документах

8. **History** (История)
   - История всех операций с товарами

### Граф зависимостей (без циркулярности):

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

## Структура проекта

```
src/
├── main/
│   ├── java/
│   │   └── com/store/inventory/
│   │       ├── domain/              # Entity классы
│   │       │   ├── Manufacturer.java
│   │       │   ├── Nomenclature.java
│   │       │   ├── Warehouse.java
│   │       │   ├── Shelf.java
│   │       │   ├── Item.java
│   │       │   ├── ItemStatus.java
│   │       │   ├── Document.java
│   │       │   ├── DocumentType.java
│   │       │   ├── DocumentStatus.java
│   │       │   ├── DocumentItem.java
│   │       │   ├── History.java
│   │       │   └── OperationType.java
│   │       │
│   │       ├── repository/          # DAO слой
│   │       │   ├── GenericDao.java
│   │       │   ├── ManufacturerDao.java
│   │       │   ├── NomenclatureDao.java
│   │       │   ├── WarehouseDao.java
│   │       │   ├── ShelfDao.java
│   │       │   ├── ItemDao.java
│   │       │   ├── DocumentDao.java
│   │       │   ├── DocumentItemDao.java
│   │       │   └── HistoryDao.java
│   │       │
│   │       ├── service/             # Бизнес-логика
│   │       │   ├── ReceiptService.java
│   │       │   ├── SaleService.java
│   │       │   ├── MovementService.java
│   │       │   └── ReportService.java
│   │       │
│   │       ├── util/                # Утилиты
│   │       │   ├── HibernateUtil.java
│   │       │   └── DatabaseInitializer.java
│   │       │
│   │       └── (будет добавлено: controller, view для JavaFX)
│   │
│   └── resources/
│       ├── hibernate.cfg.xml        # Конфигурация Hibernate
│       └── db/migration/            # SQL-скрипты миграций
│           ├── V1__initial_schema.sql
│           └── V2__initial_data.sql
│
└── database_design.md               # Документация БД
```

## Основные возможности Backend

### DAO слой (Repository)

- **GenericDao**: базовый класс с CRUD операциями
- Специализированные DAO для каждой сущности с дополнительными методами поиска

### Service слой

1. **ReceiptService** - Поступление товаров
   - Создание документа поступления
   - Добавление строк
   - Проведение документа (создание товарных позиций + история)
   - Отмена документа

2. **SaleService** - Реализация товаров
   - Создание документа продажи
   - Добавление товаров для продажи
   - Проведение документа (обновление статусов + история)
   - Проверка доступности товаров

3. **MovementService** - Перемещение товаров
   - Создание документа перемещения
   - Перемещение между полками/складами
   - Быстрое перемещение без документа

4. **ReportService** - Отчёты
   - Остатки товаров на складе
   - Товары с низким запасом
   - Просроченные товары
   - Отчёты по продажам/поступлениям
   - История операций
   - Сводка по складу

## Запуск проекта

### Требования:
- JDK 17 или выше
- Maven 3.6+

### Сборка:
```bash
mvn clean install
```

### Запуск (после создания JavaFX UI):
```bash
mvn javafx:run
```

## База данных

- **СУБД**: H2 (встроенная)
- **Режим**: файловая БД с автоматическим сервером
- **Консоль H2**: доступна на `http://localhost:8082` (при включении)

## Статусы

### Статусы товарной позиции (ItemStatus):
- `IN_STOCK` - На складе
- `SOLD` - Продано
- `RESERVED` - Зарезервировано
- `DAMAGED` - Повреждено
- `EXPIRED` - Просрочено
- `RETURNED` - Возвращено

### Типы документов (DocumentType):
- `RECEIPT` - Поступление
- `SALE` - Реализация
- `MOVEMENT` - Перемещение
- `WRITE_OFF` - Списание
- `INVENTORY` - Инвентаризация

### Статусы документа (DocumentStatus):
- `DRAFT` - Черновик
- `CONFIRMED` - Проведён
- `CANCELLED` - Отменён

### Типы операций в истории (OperationType):
- `RECEIPT` - Поступление
- `SALE` - Продажа
- `MOVEMENT` - Перемещение
- `WRITE_OFF` - Списание
- `STATUS_CHANGE` - Изменение статуса
- `INVENTORY` - Инвентаризация
- `RETURN` - Возврат

