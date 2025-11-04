package com.store.inventory.controller;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import com.store.inventory.service.ReceiptService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Контроллер диалога создания документа поступления
 */
public class ReceiptDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptDocumentController.class);

    @FXML private TextField documentNumberField;
    @FXML private DatePicker documentDatePicker;
    @FXML private ComboBox<Warehouse> warehouseCombo;
    @FXML private TextField supplierField;
    @FXML private ComboBox<Nomenclature> nomenclatureCombo;
    @FXML private TextField quantityField;
    @FXML private TextField purchasePriceField;
    @FXML private TextField sellingPriceField;
    @FXML private ComboBox<Shelf> shelfCombo;
    @FXML private TextField batchNumberField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TableView<ReceiptLine> itemsTable;
    @FXML private TableColumn<ReceiptLine, String> itemNomenclatureCol;
    @FXML private TableColumn<ReceiptLine, BigDecimal> itemQuantityCol;
    @FXML private TableColumn<ReceiptLine, BigDecimal> itemPriceCol;
    @FXML private TableColumn<ReceiptLine, BigDecimal> itemTotalCol;
    @FXML private Label totalLabel;
    @FXML private Button addItemButton;
    @FXML private Button removeItemButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final WarehouseDao warehouseDao = new WarehouseDao();
    private final ShelfDao shelfDao = new ShelfDao();
    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final ReceiptService receiptService = new ReceiptService();

    private ObservableList<ReceiptLine> receiptLines = FXCollections.observableArrayList();
    private boolean saved = false;

    @FXML
    public void initialize() {
        documentDatePicker.setValue(LocalDate.now());
        
        // Настройка таблицы строк
        itemNomenclatureCol.setCellValueFactory(new PropertyValueFactory<>("nomenclatureName"));
        itemQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemPriceCol.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        itemTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        itemsTable.setItems(receiptLines);

        // Загрузка справочников
        loadWarehouses();
        loadNomenclatures();

        // Обработчик смены склада
        warehouseCombo.setOnAction(e -> loadShelves());
    }

    private void loadWarehouses() {
        var warehouses = warehouseDao.findAllActive();
        warehouseCombo.setItems(FXCollections.observableArrayList(warehouses));
        
        warehouseCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        
        warehouseCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        
        if (!warehouses.isEmpty()) {
            warehouseCombo.setValue(warehouses.get(0));
            loadShelves();
        }
    }

    private void loadShelves() {
        Warehouse selected = warehouseCombo.getValue();
        if (selected != null) {
            var shelves = shelfDao.findActiveByWarehouse(selected);
            shelfCombo.setItems(FXCollections.observableArrayList(shelves));
            
            shelfCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Shelf item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getCode());
                }
            });
            
            shelfCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Shelf item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getCode());
                }
            });
            
            if (!shelves.isEmpty()) {
                shelfCombo.setValue(shelves.get(0));
            }
        }
    }

    private void loadNomenclatures() {
        var nomenclatures = nomenclatureDao.findAll();
        nomenclatureCombo.setItems(FXCollections.observableArrayList(nomenclatures));
        
        nomenclatureCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Nomenclature item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getArticle() + " - " + item.getName());
            }
        });
        
        nomenclatureCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Nomenclature item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getArticle() + " - " + item.getName());
            }
        });
    }

    @FXML
    private void handleAddItem() {
        try {
            if (!validateItem()) {
                return;
            }

            Nomenclature nomenclature = nomenclatureCombo.getValue();
            BigDecimal quantity = new BigDecimal(quantityField.getText().trim());
            BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText().trim());
            BigDecimal sellingPrice = new BigDecimal(sellingPriceField.getText().trim());
            Shelf shelf = shelfCombo.getValue();
            String batchNumber = batchNumberField.getText().trim();
            LocalDate expiryDate = expiryDatePicker.getValue();

            ReceiptLine line = new ReceiptLine(
                nomenclature, quantity, purchasePrice, sellingPrice, 
                shelf, batchNumber, expiryDate
            );

            receiptLines.add(line);
            updateTotal();
            clearItemFields();

        } catch (NumberFormatException e) {
            showError("Ошибка ввода", "Неверный формат числа");
        }
    }

    @FXML
    private void handleRemoveItem() {
        ReceiptLine selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            receiptLines.remove(selected);
            updateTotal();
        }
    }

    @FXML
    private void handleSave() {
        if (!validateDocument()) {
            return;
        }

        try {
            String docNumber = documentNumberField.getText().trim();
            LocalDate docDate = documentDatePicker.getValue();
            Warehouse warehouse = warehouseCombo.getValue();
            String supplier = supplierField.getText().trim();

            // Преобразуем строки в формат для сервиса
            java.util.List<com.store.inventory.service.ReceiptService.ReceiptItemData> items = 
                new java.util.ArrayList<>();
            
            for (ReceiptLine line : receiptLines) {
                items.add(new com.store.inventory.service.ReceiptService.ReceiptItemData(
                    line.getNomenclature(),
                    line.getQuantity(),
                    line.getPurchasePrice(),
                    line.getSellingPrice(),
                    line.getShelf(),
                    line.getBatchNumber(),
                    LocalDate.now(),
                    line.getExpiryDate()
                ));
            }

            // Создаём и проводим документ в одной транзакции
            receiptService.createAndConfirmReceiptDocument(
                docNumber, docDate, warehouse, supplier, items, "Система"
            );

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Ошибка при сохранении документа поступления", e);
            showError("Ошибка сохранения", "Не удалось сохранить документ: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateItem() {
        if (nomenclatureCombo.getValue() == null) {
            showError("Ошибка", "Выберите номенклатуру");
            return false;
        }
        if (quantityField.getText().trim().isEmpty()) {
            showError("Ошибка", "Введите количество");
            return false;
        }
        if (purchasePriceField.getText().trim().isEmpty()) {
            showError("Ошибка", "Введите цену закупки");
            return false;
        }
        if (sellingPriceField.getText().trim().isEmpty()) {
            showError("Ошибка", "Введите цену продажи");
            return false;
        }
        if (shelfCombo.getValue() == null) {
            showError("Ошибка", "Выберите полку");
            return false;
        }
        return true;
    }

    private boolean validateDocument() {
        if (documentNumberField.getText().trim().isEmpty()) {
            showError("Ошибка", "Введите номер документа");
            return false;
        }
        if (documentDatePicker.getValue() == null) {
            showError("Ошибка", "Выберите дату документа");
            return false;
        }
        if (warehouseCombo.getValue() == null) {
            showError("Ошибка", "Выберите склад");
            return false;
        }
        if (receiptLines.isEmpty()) {
            showError("Ошибка", "Добавьте хотя бы одну строку товара");
            return false;
        }
        return true;
    }

    private void updateTotal() {
        BigDecimal total = receiptLines.stream()
            .map(ReceiptLine::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText(String.format("Итого: %.2f руб.", total));
    }

    private void clearItemFields() {
        nomenclatureCombo.setValue(null);
        quantityField.clear();
        purchasePriceField.clear();
        sellingPriceField.clear();
        batchNumberField.clear();
        expiryDatePicker.setValue(null);
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaved() {
        return saved;
    }

    // Вспомогательный класс для строки поступления
    public static class ReceiptLine {
        private final Nomenclature nomenclature;
        private final BigDecimal quantity;
        private final BigDecimal purchasePrice;
        private final BigDecimal sellingPrice;
        private final Shelf shelf;
        private final String batchNumber;
        private final LocalDate expiryDate;

        public ReceiptLine(Nomenclature nomenclature, BigDecimal quantity, 
                          BigDecimal purchasePrice, BigDecimal sellingPrice,
                          Shelf shelf, String batchNumber, LocalDate expiryDate) {
            this.nomenclature = nomenclature;
            this.quantity = quantity;
            this.purchasePrice = purchasePrice;
            this.sellingPrice = sellingPrice;
            this.shelf = shelf;
            this.batchNumber = batchNumber;
            this.expiryDate = expiryDate;
        }

        public String getNomenclatureName() {
            return nomenclature.getName();
        }

        public BigDecimal getTotal() {
            return purchasePrice.multiply(quantity);
        }

        // Геттеры
        public Nomenclature getNomenclature() { return nomenclature; }
        public BigDecimal getQuantity() { return quantity; }
        public BigDecimal getPurchasePrice() { return purchasePrice; }
        public BigDecimal getSellingPrice() { return sellingPrice; }
        public Shelf getShelf() { return shelf; }
        public String getBatchNumber() { return batchNumber; }
        public LocalDate getExpiryDate() { return expiryDate; }
    }
}

