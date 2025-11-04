package com.store.inventory.controller;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import com.store.inventory.service.MovementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Контроллер диалога перемещения товаров между полками
 */
public class MovementDialogController {

    private static final Logger logger = LoggerFactory.getLogger(MovementDialogController.class);

    @FXML private TextField documentNumberField;
    @FXML private DatePicker documentDatePicker;
    @FXML private ComboBox<Nomenclature> nomenclatureCombo;
    @FXML private ComboBox<Item> itemCombo;
    @FXML private Label availableQuantityLabel;
    @FXML private TextField quantityField;
    @FXML private ComboBox<Shelf> sourceShelfCombo;
    @FXML private ComboBox<Shelf> targetCurrentShelfCombo;
    @FXML private TextArea commentArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final ItemDao itemDao = new ItemDao();
    private final ShelfDao shelfDao = new ShelfDao();
    private final MovementService movementService = new MovementService();

    private boolean saved = false;

    @FXML
    public void initialize() {
        documentDatePicker.setValue(LocalDate.now());
        
        loadNomenclatures();
        loadShelves();

        // Обработчики изменений
        nomenclatureCombo.setOnAction(e -> updateItems());
        itemCombo.setOnAction(e -> updateAvailableQuantity());
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
        var shelfList = FXCollections.observableArrayList(shelves);
        
        sourceShelfCombo.setItems(shelfList);
        targetCurrentShelfCombo.setItems(shelfList);
        
        ListCell<Shelf> shelfCellFactory = new ListCell<>() {
            @Override
            protected void updateItem(Shelf item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getCode() + " (" + item.getWarehouse().getName() + ")");
                }
            }
        };
        
        sourceShelfCombo.setCellFactory(lv -> shelfCellFactory);
        sourceShelfCombo.setButtonCell(new ListCell<>() {
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
        
        targetCurrentShelfCombo.setCellFactory(lv -> new ListCell<>() {
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
        
        targetCurrentShelfCombo.setButtonCell(new ListCell<>() {
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

    private void updateItems() {
        Nomenclature nomenclature = nomenclatureCombo.getValue();
        
        if (nomenclature != null) {
            List<Item> items = itemDao.findByNomenclature(nomenclature);
            
            itemCombo.setItems(FXCollections.observableArrayList(items));
            
            itemCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Item item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        String text = String.format("Партия: %s, Полка: %s (остаток: %.2f)", 
                            item.getBatchNumber(), 
                            item.getCurrentShelf().getCode(),
                            item.getQuantity());
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
                        String text = String.format("Партия: %s, Полка: %s (остаток: %.2f)", 
                            item.getBatchNumber(), 
                            item.getCurrentShelf().getCode(),
                            item.getQuantity());
                        setText(text);
                    }
                }
            });
            
            if (!items.isEmpty()) {
                itemCombo.setValue(items.get(0));
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
            sourceShelfCombo.setValue(item.getCurrentShelf());
        } else {
            availableQuantityLabel.setText("Доступно: 0");
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            String docNumber = documentNumberField.getText().trim();
            LocalDate docDate = documentDatePicker.getValue();
            Item item = itemCombo.getValue();
            BigDecimal quantity = new BigDecimal(quantityField.getText().trim());
            Shelf targetCurrentShelf = targetCurrentShelfCombo.getValue();
            String comment = commentArea.getText().trim();

            // Проверка количества
            if (quantity.compareTo(item.getQuantity()) > 0) {
                showError("Ошибка", "Недостаточно товара. Доступно: " + item.getQuantity());
                return;
            }

            // Проверка, что целевая полка отличается от исходной
            if (item.getCurrentShelf().equals(targetCurrentShelf)) {
                showError("Ошибка", "Целевая полка должна отличаться от исходной");
                return;
            }

            // Выполняем перемещение
            movementService.moveItem(
                docNumber, 
                docDate, 
                item, 
                quantity, 
                targetCurrentShelf, 
                comment,
                "Система"
            );

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Ошибка при перемещении товара", e);
            showError("Ошибка", "Не удалось выполнить перемещение: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateInput() {
        if (documentNumberField.getText().trim().isEmpty()) {
            showError("Ошибка ввода", "Введите номер документа");
            return false;
        }
        if (documentDatePicker.getValue() == null) {
            showError("Ошибка ввода", "Выберите дату документа");
            return false;
        }
        if (nomenclatureCombo.getValue() == null) {
            showError("Ошибка ввода", "Выберите номенклатуру");
            return false;
        }
        if (itemCombo.getValue() == null) {
            showError("Ошибка ввода", "Выберите товарную позицию");
            return false;
        }
        if (quantityField.getText().trim().isEmpty()) {
            showError("Ошибка ввода", "Введите количество");
            return false;
        }
        if (targetCurrentShelfCombo.getValue() == null) {
            showError("Ошибка ввода", "Выберите целевую полку");
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

