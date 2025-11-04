package com.store.inventory.controller;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import com.store.inventory.service.MovementService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер диалога перемещения товаров между складами
 */
public class MovementDialogController {

    private static final Logger logger = LoggerFactory.getLogger(MovementDialogController.class);

    @FXML private TextField documentNumberField;
    @FXML private DatePicker documentDatePicker;
    @FXML private ComboBox<Nomenclature> nomenclatureCombo;
    @FXML private TableView<ItemRow> itemsTable;
    @FXML private TableColumn<ItemRow, String> colArticle;
    @FXML private TableColumn<ItemRow, String> colName;
    @FXML private TableColumn<ItemRow, String> colBatch;
    @FXML private TableColumn<ItemRow, String> colWarehouse;
    @FXML private TableColumn<ItemRow, BigDecimal> colQuantity;
    @FXML private TableColumn<ItemRow, Boolean> colSelect;
    @FXML private ComboBox<Warehouse> targetWarehouseCombo;
    @FXML private javafx.scene.layout.VBox targetShelfBox;
    @FXML private ComboBox<Shelf> targetShelfCombo;

    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final ItemDao itemDao = new ItemDao();
    private final WarehouseDao warehouseDao = new WarehouseDao();
    private final ShelfDao shelfDao = new ShelfDao();
    private final MovementService movementService = new MovementService();

    /**
     * Инициализация контроллера
     */
    @FXML
    public void initialize() {
        logger.info("Инициализация диалога перемещения");
        
        setupTableColumns();
        loadNomenclature();
        loadWarehouses();
        
        documentDatePicker.setValue(java.time.LocalDate.now());
        generateDocumentNumber();
    }
    
    /**
     * Генерация номера документа
     */
    private void generateDocumentNumber() {
        String number = "ПЕР-" + System.currentTimeMillis();
        documentNumberField.setText(number);
    }

    /**
     * Настройка колонок таблицы
     */
    private void setupTableColumns() {
        colArticle.setCellValueFactory(new PropertyValueFactory<>("article"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNumber"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        colSelect.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        itemsTable.setEditable(true);
    }

    /**
     * Загрузка номенклатуры
     */
    private void loadNomenclature() {
        try {
            List<Nomenclature> nomenclatures = nomenclatureDao.findAll();
            nomenclatureCombo.setItems(FXCollections.observableArrayList(nomenclatures));
            
            nomenclatureCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Nomenclature item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : 
                        item.getArticle() + " - " + item.getName());
                }
            });
            
            nomenclatureCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Nomenclature item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : 
                        item.getArticle() + " - " + item.getName());
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка при загрузке номенклатуры", e);
            showError("Ошибка", "Не удалось загрузить номенклатуру: " + e.getMessage());
        }
    }

    /**
     * Загрузка складов
     */
    private void loadWarehouses() {
        try {
            List<Warehouse> warehouses = warehouseDao.findAllActive();
            targetWarehouseCombo.setItems(FXCollections.observableArrayList(warehouses));
            
            targetWarehouseCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Warehouse item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
            
            targetWarehouseCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Warehouse item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка при загрузке складов", e);
            showError("Ошибка", "Не удалось загрузить склады: " + e.getMessage());
        }
    }

    /**
     * Обработка выбора склада-приёмника
     */
    @FXML
    public void handleTargetWarehouseSelected() {
        Warehouse warehouse = targetWarehouseCombo.getValue();
        if (warehouse == null) {
            targetShelfBox.setVisible(false);
            targetShelfBox.setManaged(false);
            targetShelfCombo.getItems().clear();
            return;
        }
        
        try {
            List<Shelf> shelves = shelfDao.findByWarehouse(warehouse);
            targetShelfCombo.setItems(FXCollections.observableArrayList(shelves));
            
            targetShelfCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Shelf item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        String text = item.getCode();
                        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                            text += " - " + item.getDescription();
                        }
                        setText(text);
                    }
                }
            });
            
            targetShelfCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Shelf item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        String text = item.getCode();
                        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                            text += " - " + item.getDescription();
                        }
                        setText(text);
                    }
                }
            });
            
            if (!shelves.isEmpty()) {
                targetShelfCombo.setValue(shelves.get(0));
            }
            
            targetShelfBox.setVisible(true);
            targetShelfBox.setManaged(true);
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке полок", e);
            showError("Ошибка", "Не удалось загрузить полки: " + e.getMessage());
        }
    }
    
    /**
     * Обработка выбора номенклатуры
     */
    @FXML
    public void handleNomenclatureSelected() {
        Nomenclature selected = nomenclatureCombo.getValue();
        if (selected == null) {
            itemsTable.getItems().clear();
            return;
        }
        
        try {
            List<Item> items = itemDao.findByNomenclature(selected);
            List<ItemRow> rows = new ArrayList<>();
            
            for (Item item : items) {
                if (item.getStatus() == ItemStatus.IN_STOCK && 
                    item.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    
                    String warehouseName = item.getCurrentShelf() != null && 
                                          item.getCurrentShelf().getWarehouse() != null ?
                        item.getCurrentShelf().getWarehouse().getName() : "Неизвестно";
                    
                    rows.add(new ItemRow(
                        item,
                        item.getNomenclature().getArticle(),
                        item.getNomenclature().getName(),
                        item.getBatchNumber(),
                        warehouseName,
                        item.getQuantity()
                    ));
                }
            }
            
            itemsTable.setItems(FXCollections.observableArrayList(rows));
            logger.info("Загружено позиций: {}", rows.size());
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке позиций", e);
            showError("Ошибка", "Не удалось загрузить позиции: " + e.getMessage());
        }
    }

    /**
     * Обработка перемещения
     */
    @FXML
    public void handleMove() {
        try {
            String documentNumber = documentNumberField.getText();
            if (documentNumber == null || documentNumber.trim().isEmpty()) {
                showWarning("Предупреждение", "Укажите номер документа");
                return;
            }
            
            java.time.LocalDate documentDate = documentDatePicker.getValue();
            if (documentDate == null) {
                showWarning("Предупреждение", "Укажите дату документа");
                return;
            }
            
            List<ItemRow> selectedRows = itemsTable.getItems().stream()
                .filter(ItemRow::isSelected)
                .toList();
            
            if (selectedRows.isEmpty()) {
                showWarning("Предупреждение", "Выберите позиции для перемещения");
                return;
            }
            
            Shelf targetShelf = targetShelfCombo.getValue();
            if (targetShelf == null) {
                showWarning("Предупреждение", "Выберите полку-приёмник");
                return;
            }
            
            Item firstItem = selectedRows.get(0).getItem();
            Warehouse sourceWarehouse = firstItem.getCurrentShelf().getWarehouse();
            
            java.util.List<MovementService.MovementItemData> itemsData = new java.util.ArrayList<>();
            for (ItemRow row : selectedRows) {
                itemsData.add(new MovementService.MovementItemData(row.getItem(), targetShelf));
            }
            
            Document document = movementService.createAndConfirmMovementDocument(
                documentNumber.trim(),
                documentDate,
                sourceWarehouse,
                itemsData,
                "Пользователь"
            );
            
            showInfo("Успех", 
                "Документ перемещения " + documentNumber + " успешно создан и проведён");
            closeDialog();
            
        } catch (Exception e) {
            logger.error("Ошибка при перемещении", e);
            showError("Ошибка", "Не удалось выполнить перемещение: " + e.getMessage());
        }
    }

    /**
     * Обработка отмены
     */
    @FXML
    public void handleCancel() {
        closeDialog();
    }

    /**
     * Закрытие диалога
     */
    private void closeDialog() {
        Stage stage = (Stage) nomenclatureCombo.getScene().getWindow();
        stage.close();
    }

    /**
     * Показ ошибки
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показ предупреждения
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показ информации
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Класс для представления строки позиции в таблице
     */
    public static class ItemRow {
        private final Item item;
        private final String article;
        private final String name;
        private final String batchNumber;
        private final String warehouseName;
        private final BigDecimal quantity;
        private final BooleanProperty selected;

        public ItemRow(Item item, String article, String name, String batchNumber, 
                      String warehouseName, BigDecimal quantity) {
            this.item = item;
            this.article = article;
            this.name = name;
            this.batchNumber = batchNumber;
            this.warehouseName = warehouseName;
            this.quantity = quantity;
            this.selected = new SimpleBooleanProperty(false);
        }

        public Item getItem() { return item; }
        public String getArticle() { return article; }
        public String getName() { return name; }
        public String getBatchNumber() { return batchNumber; }
        public String getWarehouseName() { return warehouseName; }
        public BigDecimal getQuantity() { return quantity; }
        
        public BooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean value) { selected.set(value); }
    }
}
