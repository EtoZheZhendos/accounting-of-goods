package com.store.inventory.controller;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Контроллер диалога добавления/редактирования товарной позиции
 */
public class ItemDialogController {

    private static final Logger logger = LoggerFactory.getLogger(ItemDialogController.class);

    @FXML private ComboBox<Nomenclature> nomenclatureCombo;
    @FXML private ComboBox<Shelf> shelfCombo;
    @FXML private TextField batchNumberField;
    @FXML private TextField initialQuantityField;
    @FXML private TextField currentQuantityField;
    @FXML private TextField purchasePriceField;
    @FXML private TextField sellingPriceField;
    @FXML private DatePicker manufactureDatePicker;
    @FXML private DatePicker expiryDatePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final ShelfDao shelfDao = new ShelfDao();
    private final ItemDao itemDao = new ItemDao();
    private Item item;
    private boolean saved = false;

    @FXML
    public void initialize() {
        manufactureDatePicker.setValue(LocalDate.now());
        loadNomenclatures();
        loadShelves();
    }

    public void setItem(Item item) {
        this.item = item;
        
        if (item != null) {
            nomenclatureCombo.setValue(item.getNomenclature());
            shelfCombo.setValue(item.getCurrentShelf());
            batchNumberField.setText(item.getBatchNumber());
            initialQuantityField.setText(item.getQuantity().toString());
            currentQuantityField.setText(item.getQuantity().toString());
            purchasePriceField.setText(item.getPurchasePrice().toString());
            sellingPriceField.setText(item.getSellingPrice().toString());
            manufactureDatePicker.setValue(item.getManufactureDate());
            expiryDatePicker.setValue(item.getExpiryDate());
            
            // При редактировании блокируем некоторые поля
            nomenclatureCombo.setDisable(true);
            batchNumberField.setDisable(true);
            initialQuantityField.setDisable(true);
            manufactureDatePicker.setDisable(true);
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

    private void loadShelves() {
        var shelves = shelfDao.findAll();
        shelfCombo.setItems(FXCollections.observableArrayList(shelves));
        
        shelfCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Shelf item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getCode() + " (" + item.getWarehouse().getName() + ")");
                }
            }
        });
        
        shelfCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Shelf item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getCode() + " (" + item.getWarehouse().getName() + ")");
                }
            }
        });
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            if (item == null) {
                item = new Item();
                item.setNomenclature(nomenclatureCombo.getValue());
                item.setBatchNumber(batchNumberField.getText().trim());
                item.setQuantity(new BigDecimal(initialQuantityField.getText().trim()));
                item.setManufactureDate(manufactureDatePicker.getValue());
                item.setStatus(com.store.inventory.domain.ItemStatus.IN_STOCK);
            }

            item.setCurrentShelf(shelfCombo.getValue());
            item.setQuantity(new BigDecimal(currentQuantityField.getText().trim()));
            item.setPurchasePrice(new BigDecimal(purchasePriceField.getText().trim()));
            item.setSellingPrice(new BigDecimal(sellingPriceField.getText().trim()));
            item.setExpiryDate(expiryDatePicker.getValue());

            itemDao.save(item);

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Ошибка при сохранении товарной позиции", e);
            showError("Ошибка сохранения", "Не удалось сохранить товарную позицию: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateInput() {
        if (nomenclatureCombo.getValue() == null) {
            showError("Ошибка ввода", "Выберите номенклатуру");
            return false;
        }
        if (shelfCombo.getValue() == null) {
            showError("Ошибка ввода", "Выберите полку");
            return false;
        }
        if (batchNumberField.getText().trim().isEmpty()) {
            showError("Ошибка ввода", "Введите номер партии");
            return false;
        }
        if (item == null) {
            if (initialQuantityField.getText().trim().isEmpty()) {
                showError("Ошибка ввода", "Введите начальное количество");
                return false;
            }
        }
        if (currentQuantityField.getText().trim().isEmpty()) {
            showError("Ошибка ввода", "Введите текущее количество");
            return false;
        }
        if (purchasePriceField.getText().trim().isEmpty()) {
            showError("Ошибка ввода", "Введите цену закупки");
            return false;
        }
        if (sellingPriceField.getText().trim().isEmpty()) {
            showError("Ошибка ввода", "Введите цену продажи");
            return false;
        }
        if (manufactureDatePicker.getValue() == null) {
            showError("Ошибка ввода", "Выберите дату производства");
            return false;
        }
        return true;
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
}

