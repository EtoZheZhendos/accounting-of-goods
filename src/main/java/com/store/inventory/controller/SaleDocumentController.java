package com.store.inventory.controller;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import com.store.inventory.service.SaleService;
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
import java.util.List;

/**
 * Контроллер диалога создания документа реализации
 */
public class SaleDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(SaleDocumentController.class);

    @FXML private TextField documentNumberField;
    @FXML private DatePicker documentDatePicker;
    @FXML private ComboBox<Warehouse> warehouseCombo;
    @FXML private TextField customerField;
    @FXML private ComboBox<Nomenclature> nomenclatureCombo;
    @FXML private TextField quantityField;
    @FXML private TextField salePriceField;
    @FXML private ComboBox<Item> itemCombo;
    @FXML private Label availableQuantityLabel;
    @FXML private TableView<SaleLine> itemsTable;
    @FXML private TableColumn<SaleLine, String> itemNomenclatureCol;
    @FXML private TableColumn<SaleLine, BigDecimal> itemQuantityCol;
    @FXML private TableColumn<SaleLine, BigDecimal> itemPriceCol;
    @FXML private TableColumn<SaleLine, BigDecimal> itemTotalCol;
    @FXML private Label totalLabel;
    @FXML private Button addItemButton;
    @FXML private Button removeItemButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final WarehouseDao warehouseDao = new WarehouseDao();
    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final ItemDao itemDao = new ItemDao();
    private final SaleService saleService = new SaleService();

    private ObservableList<SaleLine> saleLines = FXCollections.observableArrayList();
    private boolean saved = false;

    @FXML
    public void initialize() {
        documentDatePicker.setValue(LocalDate.now());
        
        // Настройка таблицы строк
        itemNomenclatureCol.setCellValueFactory(new PropertyValueFactory<>("nomenclatureName"));
        itemQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemPriceCol.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        itemTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        itemsTable.setItems(saleLines);

        // Загрузка справочников
        loadWarehouses();
        loadNomenclatures();

        // Обработчик смены склада
        warehouseCombo.setOnAction(e -> updateItemsByWarehouse());
        
        // Обработчик смены номенклатуры
        nomenclatureCombo.setOnAction(e -> updateAvailableItems());
        
        // Обработчик смены товарной позиции
        itemCombo.setOnAction(e -> updateAvailableQuantity());
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
            updateItemsByWarehouse();
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

    private void updateItemsByWarehouse() {
        Warehouse warehouse = warehouseCombo.getValue();
        if (warehouse != null) {
            updateAvailableItems();
        }
    }

    private void updateAvailableItems() {
        Nomenclature nomenclature = nomenclatureCombo.getValue();
        Warehouse warehouse = warehouseCombo.getValue();
        
        if (nomenclature != null && warehouse != null) {
            List<Item> availableItems = itemDao.findAvailableByNomenclatureAndWarehouse(
                nomenclature, warehouse
            );
            
            itemCombo.setItems(FXCollections.observableArrayList(availableItems));
            
            itemCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Item item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        String text = String.format("Партия: %s (остаток: %.2f)", 
                            item.getBatchNumber(), item.getQuantity());
                        setText(text);
                    }
                }
            });
            
            itemCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Item item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        String text = String.format("Партия: %s (остаток: %.2f)", 
                            item.getBatchNumber(), item.getQuantity());
                        setText(text);
                    }
                }
            });
            
            if (!availableItems.isEmpty()) {
                itemCombo.setValue(availableItems.get(0));
                updateAvailableQuantity();
            }
        } else {
            itemCombo.setItems(FXCollections.observableArrayList());
            availableQuantityLabel.setText("Доступно: 0");
        }
    }

    private void updateAvailableQuantity() {
        Item item = itemCombo.getValue();
        if (item != null) {
            availableQuantityLabel.setText(String.format("Доступно: %.2f", item.getQuantity()));
            salePriceField.setText(item.getSellingPrice().toString());
        } else {
            availableQuantityLabel.setText("Доступно: 0");
        }
    }

    @FXML
    private void handleAddItem() {
        try {
            if (!validateItem()) {
                return;
            }

            Item item = itemCombo.getValue();
            Nomenclature nomenclature = nomenclatureCombo.getValue();
            BigDecimal quantity = new BigDecimal(quantityField.getText().trim());
            BigDecimal salePrice = new BigDecimal(salePriceField.getText().trim());

            // Проверка количества
            if (quantity.compareTo(item.getQuantity()) > 0) {
                showError("Ошибка", "Недостаточно товара на остатке. Доступно: " + item.getQuantity());
                return;
            }

            SaleLine line = new SaleLine(nomenclature, item, quantity, salePrice);

            saleLines.add(line);
            updateTotal();
            clearItemFields();

        } catch (NumberFormatException e) {
            showError("Ошибка ввода", "Неверный формат числа");
        }
    }

    @FXML
    private void handleRemoveItem() {
        SaleLine selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            saleLines.remove(selected);
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
            String customer = customerField.getText().trim();

            // Преобразуем строки в формат для сервиса
            java.util.List<com.store.inventory.service.SaleService.SaleItemData> items = 
                new java.util.ArrayList<>();
            
            for (SaleLine line : saleLines) {
                items.add(new com.store.inventory.service.SaleService.SaleItemData(
                    line.getItem(),
                    line.getQuantity(),
                    line.getSalePrice()
                ));
            }

            // Создаём и проводим документ в одной транзакции
            saleService.createAndConfirmSaleDocument(
                docNumber, docDate, warehouse, customer, items, "Система"
            );

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Ошибка при сохранении документа реализации", e);
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
        if (itemCombo.getValue() == null) {
            showError("Ошибка", "Выберите товарную позицию");
            return false;
        }
        if (quantityField.getText().trim().isEmpty()) {
            showError("Ошибка", "Введите количество");
            return false;
        }
        if (salePriceField.getText().trim().isEmpty()) {
            showError("Ошибка", "Введите цену продажи");
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
        if (saleLines.isEmpty()) {
            showError("Ошибка", "Добавьте хотя бы одну строку товара");
            return false;
        }
        return true;
    }

    private void updateTotal() {
        BigDecimal total = saleLines.stream()
            .map(SaleLine::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText(String.format("Итого: %.2f руб.", total));
    }

    private void clearItemFields() {
        nomenclatureCombo.setValue(null);
        itemCombo.setValue(null);
        quantityField.clear();
        salePriceField.clear();
        availableQuantityLabel.setText("Доступно: 0");
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

    // Вспомогательный класс для строки реализации
    public static class SaleLine {
        private final Nomenclature nomenclature;
        private final Item item;
        private final BigDecimal quantity;
        private final BigDecimal salePrice;

        public SaleLine(Nomenclature nomenclature, Item item, 
                       BigDecimal quantity, BigDecimal salePrice) {
            this.nomenclature = nomenclature;
            this.item = item;
            this.quantity = quantity;
            this.salePrice = salePrice;
        }

        public String getNomenclatureName() {
            return nomenclature.getName();
        }

        public BigDecimal getTotal() {
            return salePrice.multiply(quantity);
        }

        // Геттеры
        public Nomenclature getNomenclature() { return nomenclature; }
        public Item getItem() { return item; }
        public BigDecimal getQuantity() { return quantity; }
        public BigDecimal getSalePrice() { return salePrice; }
    }
}

