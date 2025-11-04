package com.store.inventory.controller;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import com.store.inventory.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Контроллер главного окна приложения
 */
public class MainWindowController {

    private static final Logger logger = LoggerFactory.getLogger(MainWindowController.class);

    // DAO
    private final ManufacturerDao manufacturerDao = new ManufacturerDao();
    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final WarehouseDao warehouseDao = new WarehouseDao();
    private final ShelfDao shelfDao = new ShelfDao();
    private final ItemDao itemDao = new ItemDao();
    private final DocumentDao documentDao = new DocumentDao();

    // Services
    private final ReportService reportService = new ReportService();

    // FXML элементы - Вкладка "Остатки"
    @FXML
    private TableView<StockReportItem> stockTable;
    @FXML
    private TableColumn<StockReportItem, String> stockArticleCol;
    @FXML
    private TableColumn<StockReportItem, String> stockNameCol;
    @FXML
    private TableColumn<StockReportItem, String> stockManufacturerCol;
    @FXML
    private TableColumn<StockReportItem, String> stockWarehouseCol;
    @FXML
    private TableColumn<StockReportItem, BigDecimal> stockQuantityCol;
    @FXML
    private TableColumn<StockReportItem, String> stockUnitCol;
    
    // Фильтры для остатков
    @FXML
    private ComboBox<Warehouse> filterWarehouseCombo;
    @FXML
    private TextField filterArticleField;
    @FXML
    private TextField filterNameField;
    
    // Данные для фильтрации
    private ObservableList<StockReportItem> allStockData = FXCollections.observableArrayList();

    // FXML элементы - Вкладка "Номенклатура"
    @FXML
    private TableView<Nomenclature> nomenclatureTable;
    @FXML
    private TableColumn<Nomenclature, String> nomArticleCol;
    @FXML
    private TableColumn<Nomenclature, String> nomNameCol;
    @FXML
    private TableColumn<Nomenclature, String> nomManufacturerCol;
    @FXML
    private TableColumn<Nomenclature, String> nomUnitCol;

    // FXML элементы - Вкладка "Документы"
    @FXML
    private TableView<Document> documentTable;
    @FXML
    private TableColumn<Document, String> docNumberCol;
    @FXML
    private TableColumn<Document, String> docTypeCol;
    @FXML
    private TableColumn<Document, String> docDateCol;
    @FXML
    private TableColumn<Document, String> docStatusCol;
    @FXML
    private TableColumn<Document, BigDecimal> docAmountCol;

    // FXML элементы - Вкладка "Склады"
    @FXML
    private TableView<Warehouse> warehouseTable;
    @FXML
    private TableColumn<Warehouse, String> warehouseNameCol;
    @FXML
    private TableColumn<Warehouse, String> warehouseAddressCol;
    @FXML
    private TableColumn<Warehouse, Boolean> warehouseActiveCol;

    @FXML
    private Label statusLabel;

    /**
     * Инициализация контроллера
     */
    @FXML
    public void initialize() {
        logger.info("Инициализация главного окна...");

        try {
            initializeStockTable();
            initializeNomenclatureTable();
            initializeDocumentTable();
            initializeWarehouseTable();
            initializeStockFilters();

            loadAllData();

            statusLabel.setText("Готово");
            logger.info("Главное окно инициализировано");

        } catch (Exception e) {
            logger.error("Ошибка при инициализации главного окна", e);
            showError("Ошибка инициализации", e.getMessage());
        }
    }
    
    /**
     * Инициализация фильтров остатков
     */
    private void initializeStockFilters() {
        // Загружаем склады для фильтра
        List<Warehouse> warehouses = warehouseDao.findAllActive();
        filterWarehouseCombo.setItems(FXCollections.observableArrayList(warehouses));
        
        filterWarehouseCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        
        filterWarehouseCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Все склады" : item.getName());
            }
        });
    }

    /**
     * Инициализация таблицы остатков
     */
    private void initializeStockTable() {
        stockArticleCol.setCellValueFactory(new PropertyValueFactory<>("article"));
        stockNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        stockManufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        stockWarehouseCol.setCellValueFactory(new PropertyValueFactory<>("warehouse"));
        stockQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        stockUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
    }

    /**
     * Инициализация таблицы номенклатуры
     */
    private void initializeNomenclatureTable() {
        nomArticleCol.setCellValueFactory(new PropertyValueFactory<>("article"));
        nomNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nomManufacturerCol.setCellValueFactory(cellData -> {
            Nomenclature nom = cellData.getValue();
            String manufacturerName = nom.getManufacturer() != null ? nom.getManufacturer().getName() : "";
            return new javafx.beans.property.SimpleStringProperty(manufacturerName);
        });
        nomUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
    }

    /**
     * Инициализация таблицы документов
     */
    private void initializeDocumentTable() {
        docNumberCol.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        docTypeCol.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getDocumentType().getDisplayName();
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        docDateCol.setCellValueFactory(new PropertyValueFactory<>("documentDate"));
        docStatusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus().getDisplayName();
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        docAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }

    /**
     * Инициализация таблицы складов
     */
    private void initializeWarehouseTable() {
        warehouseNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        warehouseAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        warehouseActiveCol.setCellValueFactory(new PropertyValueFactory<>("isActive"));
    }

    /**
     * Загрузка всех данных
     */
    private void loadAllData() {
        loadStockData();
        loadNomenclatureData();
        loadDocumentData();
        loadWarehouseData();
    }

    /**
     * Загрузка данных об остатках
     */
    @FXML
    public void loadStockData() {
        try {
            List<Object[]> stockReport = reportService.getStockReportByWarehouse();
            allStockData.clear();

            for (Object[] row : stockReport) {
                Nomenclature nom = (Nomenclature) row[0];
                Warehouse warehouse = (Warehouse) row[1];
                BigDecimal qty = (BigDecimal) row[2];
                
                String manufacturerName = nom.getManufacturer() != null ? nom.getManufacturer().getName() : "";
                String warehouseName = warehouse != null ? warehouse.getName() : "";
                
                allStockData.add(new StockReportItem(
                        nom.getArticle(),
                        nom.getName(),
                        manufacturerName,
                        warehouseName,
                        qty,
                        nom.getUnit()
                ));
            }

            applyStockFilter();
            logger.info("Загружено остатков: {}", allStockData.size());

        } catch (Exception e) {
            logger.error("Ошибка при загрузке остатков", e);
            showError("Ошибка загрузки", e.getMessage());
        }
    }
    
    /**
     * Применение фильтров к остаткам
     */
    @FXML
    public void applyStockFilter() {
        if (allStockData == null || allStockData.isEmpty()) {
            return;
        }
        
        String articleFilter = filterArticleField != null && filterArticleField.getText() != null ? 
                filterArticleField.getText().toLowerCase().trim() : "";
        String nameFilter = filterNameField != null && filterNameField.getText() != null ? 
                filterNameField.getText().toLowerCase().trim() : "";
        Warehouse warehouseFilter = filterWarehouseCombo != null ? filterWarehouseCombo.getValue() : null;
        
        ObservableList<StockReportItem> filteredData = FXCollections.observableArrayList();
        
        for (StockReportItem stock : allStockData) {
            boolean matches = true;
            
            // Фильтр по артикулу
            if (!articleFilter.isEmpty() && 
                (stock.getArticle() == null || !stock.getArticle().toLowerCase().contains(articleFilter))) {
                matches = false;
            }
            
            // Фильтр по названию
            if (!nameFilter.isEmpty() && 
                (stock.getName() == null || !stock.getName().toLowerCase().contains(nameFilter))) {
                matches = false;
            }
            
            // Фильтр по складу
            if (warehouseFilter != null && 
                (stock.getWarehouse() == null || !stock.getWarehouse().equals(warehouseFilter.getName()))) {
                matches = false;
            }
            
            if (matches) {
                filteredData.add(stock);
            }
        }
        
        stockTable.setItems(filteredData);
        statusLabel.setText(String.format("Найдено позиций: %d из %d", filteredData.size(), allStockData.size()));
    }
    
    /**
     * Очистка фильтров остатков
     */
    @FXML
    public void clearStockFilters() {
        if (filterArticleField != null) filterArticleField.clear();
        if (filterNameField != null) filterNameField.clear();
        if (filterWarehouseCombo != null) filterWarehouseCombo.setValue(null);
        applyStockFilter();
        statusLabel.setText("Фильтры очищены");
    }

    /**
     * Загрузка номенклатуры
     */
    @FXML
    public void loadNomenclatureData() {
        try {
            List<Nomenclature> nomenclatures = nomenclatureDao.findAll();
            ObservableList<Nomenclature> nomenclatureList = FXCollections.observableArrayList(nomenclatures);
            nomenclatureTable.setItems(nomenclatureList);
            logger.info("Загружено номенклатуры: {}", nomenclatures.size());

        } catch (Exception e) {
            logger.error("Ошибка при загрузке номенклатуры", e);
            showError("Ошибка загрузки", e.getMessage());
        }
    }

    /**
     * Загрузка документов
     */
    @FXML
    public void loadDocumentData() {
        try {
            List<Document> documents = documentDao.findAll();
            ObservableList<Document> documentList = FXCollections.observableArrayList(documents);
            documentTable.setItems(documentList);
            logger.info("Загружено документов: {}", documents.size());

        } catch (Exception e) {
            logger.error("Ошибка при загрузке документов", e);
            showError("Ошибка загрузки", e.getMessage());
        }
    }

    /**
     * Загрузка складов
     */
    @FXML
    public void loadWarehouseData() {
        try {
            List<Warehouse> warehouses = warehouseDao.findAll();
            ObservableList<Warehouse> warehouseList = FXCollections.observableArrayList(warehouses);
            warehouseTable.setItems(warehouseList);
            logger.info("Загружено складов: {}", warehouses.size());

        } catch (Exception e) {
            logger.error("Ошибка при загрузке складов", e);
            showError("Ошибка загрузки", e.getMessage());
        }
    }

    /**
     * Обработчик кнопки "Обновить"
     */
    @FXML
    public void handleRefresh() {
        loadAllData();
        statusLabel.setText("Данные обновлены");
    }

    /**
     * Показать ошибку
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Обработчик выхода из приложения
     */
    @FXML
    public void handleExit() {
        logger.info("Выход из приложения");
        javafx.application.Platform.exit();
    }

    /**
     * Добавить новую номенклатуру
     */
    @FXML
    public void handleAddNomenclature() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/view/NomenclatureDialog.fxml")
            );
            javafx.scene.Parent root = loader.load();

            NomenclatureDialogController controller = loader.getController();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Добавление номенклатуры");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                loadNomenclatureData();
                statusLabel.setText("Номенклатура добавлена");
            }

        } catch (Exception e) {
            logger.error("Ошибка при открытии диалога номенклатуры", e);
            showError("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
        }
    }

    /**
     * Редактировать выбранную номенклатуру
     */
    @FXML
    public void handleEditNomenclature() {
        Nomenclature selected = nomenclatureTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите номенклатуру", "Пожалуйста, выберите номенклатуру для редактирования");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/view/NomenclatureDialog.fxml")
            );
            javafx.scene.Parent root = loader.load();

            NomenclatureDialogController controller = loader.getController();
            controller.setNomenclature(selected);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Редактирование номенклатуры");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                loadNomenclatureData();
                statusLabel.setText("Номенклатура обновлена");
            }

        } catch (Exception e) {
            logger.error("Ошибка при открытии диалога редактирования", e);
            showError("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
        }
    }

    /**
     * Удалить выбранную номенклатуру
     */
    @FXML
    public void handleDeleteNomenclature() {
        Nomenclature selected = nomenclatureTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите номенклатуру", "Пожалуйста, выберите номенклатуру для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить номенклатуру?");
        confirm.setContentText("Вы уверены, что хотите удалить \"" + selected.getName() + "\"?");

        if (confirm.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            try {
                nomenclatureDao.delete(selected);
                loadNomenclatureData();
                statusLabel.setText("Номенклатура удалена");
            } catch (Exception e) {
                logger.error("Ошибка при удалении номенклатуры", e);
                showError("Ошибка удаления", "Не удалось удалить номенклатуру: " + e.getMessage());
            }
        }
    }

    /**
     * Показать предупреждение
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Добавить склад
     */
    @FXML
    public void handleAddWarehouse() {
        openDialog("/view/WarehouseDialog.fxml", "Добавление склада", 
                    this::loadWarehouseData, "Склад добавлен");
    }

    /**
     * Редактировать склад
     */
    @FXML
    public void handleEditWarehouse() {
        Warehouse selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите склад", "Пожалуйста, выберите склад для редактирования");
            return;
        }
        openDialogWithData("/view/WarehouseDialog.fxml", "Редактирование склада", 
                          selected, this::loadWarehouseData, "Склад обновлён");
    }

    /**
     * Удалить склад
     */
    @FXML
    public void handleDeleteWarehouse() {
        Warehouse selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите склад", "Пожалуйста, выберите склад для удаления");
            return;
        }

        if (confirmDelete("склад", selected.getName())) {
            try {
                warehouseDao.delete(selected);
                loadWarehouseData();
                statusLabel.setText("Склад удалён");
            } catch (Exception e) {
                logger.error("Ошибка при удалении склада", e);
                showError("Ошибка удаления", "Не удалось удалить склад: " + e.getMessage());
            }
        }
    }

    /**
     * Создать документ поступления
     */
    @FXML
    public void handleCreateReceiptDocument() {
        openDialog("/view/ReceiptDocument.fxml", "Документ поступления", 
                    this::loadAllData, "Документ поступления создан");
    }

    /**
     * Создать документ реализации
     */
    @FXML
    public void handleCreateSaleDocument() {
        openDialog("/view/SaleDocument.fxml", "Документ реализации", 
                    this::loadAllData, "Документ реализации создан");
    }

    /**
     * Просмотр истории операций
     */
    @FXML
    public void handleViewHistory() {
        openDialog("/view/ItemHistory.fxml", "История операций с товаром", 
                    null, "");
    }

    /**
     * Создать документ перемещения
     */
    @FXML
    public void handleCreateMovementDocument() {
        openDialog("/view/MovementDialog.fxml", "Перемещение товара", 
                    this::loadAllData, "Перемещение выполнено");
    }

    /**
     * Создать новый документ (универсальная кнопка)
     */
    @FXML
    public void handleCreateDocument() {
        // Показываем меню выбора типа документа
        javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
        
        javafx.scene.control.MenuItem receiptItem = new javafx.scene.control.MenuItem("Поступление");
        receiptItem.setOnAction(e -> handleCreateReceiptDocument());
        
        javafx.scene.control.MenuItem saleItem = new javafx.scene.control.MenuItem("Реализация");
        saleItem.setOnAction(e -> handleCreateSaleDocument());
        
        javafx.scene.control.MenuItem movementItem = new javafx.scene.control.MenuItem("Перемещение");
        movementItem.setOnAction(e -> handleCreateMovementDocument());
        
        menu.getItems().addAll(receiptItem, saleItem, movementItem);
        
        // Показываем меню у кнопки "Создать"
        javafx.scene.Node source = (javafx.scene.Node) documentTable.getScene().lookup(".button");
        if (source != null) {
            javafx.geometry.Bounds bounds = source.localToScreen(source.getBoundsInLocal());
            menu.show(source, bounds.getMinX(), bounds.getMaxY());
        }
    }

    /**
     * Открыть выбранный документ
     */
    @FXML
    public void handleOpenDocument() {
        Document selected = documentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите документ", "Пожалуйста, выберите документ для открытия");
            return;
        }

        showInfo("Просмотр документа", 
            String.format("Документ: %s\nТип: %s\nДата: %s\nСтатус: %s\nСумма: %.2f руб.",
                selected.getDocumentNumber(),
                translateDocumentType(selected.getDocumentType()),
                selected.getDocumentDate(),
                translateDocumentStatus(selected.getStatus()),
                selected.getTotalAmount()
            ));
    }

    /**
     * Удалить выбранный документ
     */
    @FXML
    public void handleDeleteDocument() {
        Document selected = documentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите документ", "Пожалуйста, выберите документ для удаления");
            return;
        }

        if (selected.getStatus() == DocumentStatus.CONFIRMED) {
            showWarning("Невозможно удалить", "Нельзя удалить проведенный документ. Сначала отмените проведение.");
            return;
        }

        if (confirmDelete("документ", selected.getDocumentNumber())) {
            try {
                documentDao.delete(selected);
                loadDocumentData();
                statusLabel.setText("Документ удалён");
            } catch (Exception e) {
                logger.error("Ошибка при удалении документа", e);
                showError("Ошибка удаления", "Не удалось удалить документ: " + e.getMessage());
            }
        }
    }

    /**
     * Перевод типа документа
     */
    private String translateDocumentType(DocumentType type) {
        if (type == null) return "Неизвестно";
        return switch (type) {
            case RECEIPT -> "Поступление";
            case SALE -> "Реализация";
            case MOVEMENT -> "Перемещение";
            case WRITE_OFF -> "Списание";
            default -> "Другое";
        };
    }

    /**
     * Перевод статуса документа
     */
    private String translateDocumentStatus(DocumentStatus status) {
        if (status == null) return "Неизвестно";
        return switch (status) {
            case DRAFT -> "Черновик";
            case CONFIRMED -> "Проведён";
            case CANCELLED -> "Отменён";
            default -> "Другое";
        };
    }

    /**
     * Показать информационное сообщение
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Универсальный метод открытия диалога
     */
    private void openDialog(String fxmlPath, String title, Runnable onSuccess, String successMessage) {
        try {
            logger.info("Открытие диалога: {}", fxmlPath);
            
            // Используем ClassLoader для загрузки
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlPath));
            
            if (loader.getLocation() == null) {
                logger.error("FXML файл не найден: {}", fxmlPath);
                showError("Ошибка", "Файл диалога не найден: " + fxmlPath);
                return;
            }
            
            javafx.scene.Parent root = loader.load();

            Object controller = loader.getController();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(title);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // Проверяем, был ли сохранён результат
            boolean saved = false;
            try {
                java.lang.reflect.Method method = controller.getClass().getMethod("isSaved");
                saved = (Boolean) method.invoke(controller);
            } catch (Exception e) {
                // Игнорируем, если метод не найден
            }

            if (saved && onSuccess != null) {
                onSuccess.run();
                statusLabel.setText(successMessage);
            }

        } catch (Exception e) {
            logger.error("Ошибка при открытии диалога: " + fxmlPath, e);
            showError("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
        }
    }

    /**
     * Универсальный метод открытия диалога с данными
     */
    private <T> void openDialogWithData(String fxmlPath, String title, T data, 
                                       Runnable onSuccess, String successMessage) {
        try {
            logger.info("Открытие диалога с данными: {}", fxmlPath);
            
            // Используем ClassLoader для загрузки
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlPath));
            
            if (loader.getLocation() == null) {
                logger.error("FXML файл не найден: {}", fxmlPath);
                showError("Ошибка", "Файл диалога не найден: " + fxmlPath);
                return;
            }
            
            javafx.scene.Parent root = loader.load();

            Object controller = loader.getController();

            // Устанавливаем данные через рефлексию
            if (data != null) {
                String setterName = "set" + data.getClass().getSimpleName();
                try {
                    java.lang.reflect.Method method = controller.getClass()
                        .getMethod(setterName, data.getClass());
                    method.invoke(controller, data);
                } catch (Exception e) {
                    logger.warn("Не удалось вызвать метод " + setterName, e);
                }
            }

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(title);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            boolean saved = false;
            try {
                java.lang.reflect.Method method = controller.getClass().getMethod("isSaved");
                saved = (Boolean) method.invoke(controller);
            } catch (Exception e) {
                // Игнорируем
            }

            if (saved && onSuccess != null) {
                onSuccess.run();
                statusLabel.setText(successMessage);
            }

        } catch (Exception e) {
            logger.error("Ошибка при открытии диалога: " + fxmlPath, e);
            showError("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
        }
    }

    /**
     * Подтверждение удаления
     */
    private boolean confirmDelete(String entityType, String entityName) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить " + entityType + "?");
        confirm.setContentText("Вы уверены, что хотите удалить \"" + entityName + "\"?");
        return confirm.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK;
    }

    /**
     * Вспомогательный класс для отображения остатков
     */
    public static class NomenclatureStock {
        private final String article;
        private final String name;
        private final String manufacturer;
        private final BigDecimal quantity;
        private final String unit;

        public NomenclatureStock(String article, String name, String manufacturer, BigDecimal quantity, String unit) {
            this.article = article;
            this.name = name;
            this.manufacturer = manufacturer;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getArticle() { return article; }
        public String getName() { return name; }
        public String getManufacturer() { return manufacturer; }
        public BigDecimal getQuantity() { return quantity; }
        public String getUnit() { return unit; }
    }
}

