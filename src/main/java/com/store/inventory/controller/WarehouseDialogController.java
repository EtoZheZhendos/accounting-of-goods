package com.store.inventory.controller;

import com.store.inventory.domain.Warehouse;
import com.store.inventory.repository.WarehouseDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер диалога добавления/редактирования склада
 */
public class WarehouseDialogController {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseDialogController.class);

    @FXML private TextField nameField;
    @FXML private TextArea addressArea;
    @FXML private CheckBox isActiveCheckbox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final WarehouseDao warehouseDao = new WarehouseDao();
    private Warehouse warehouse;
    private boolean saved = false;

    @FXML
    public void initialize() {
        isActiveCheckbox.setSelected(true);
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
        
        if (warehouse != null) {
            nameField.setText(warehouse.getName());
            addressArea.setText(warehouse.getAddress());
            isActiveCheckbox.setSelected(warehouse.getIsActive());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            if (warehouse == null) {
                warehouse = new Warehouse();
            }

            warehouse.setName(nameField.getText().trim());
            warehouse.setAddress(addressArea.getText().trim());
            warehouse.setIsActive(isActiveCheckbox.isSelected());

            warehouseDao.save(warehouse);

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Ошибка при сохранении склада", e);
            showError("Ошибка сохранения", "Не удалось сохранить склад: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateInput() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showError("Ошибка ввода", "Название склада обязательно для заполнения");
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

