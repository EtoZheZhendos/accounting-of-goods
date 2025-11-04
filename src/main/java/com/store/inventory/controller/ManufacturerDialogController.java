package com.store.inventory.controller;

import com.store.inventory.domain.Manufacturer;
import com.store.inventory.repository.ManufacturerDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер диалога добавления/редактирования производителя
 */
public class ManufacturerDialogController {

    private static final Logger logger = LoggerFactory.getLogger(ManufacturerDialogController.class);

    @FXML private TextField nameField;
    @FXML private TextField countryField;
    @FXML private TextArea contactInfoArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ManufacturerDao manufacturerDao = new ManufacturerDao();
    private Manufacturer manufacturer;
    private boolean saved = false;

    /**
     * Установить производителя для редактирования
     */
    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
        
        if (manufacturer != null) {
            nameField.setText(manufacturer.getName());
            countryField.setText(manufacturer.getCountry());
            contactInfoArea.setText(manufacturer.getContactInfo());
        }
    }

    /**
     * Обработчик кнопки "Сохранить"
     */
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            if (manufacturer == null) {
                manufacturer = new Manufacturer();
            }

            manufacturer.setName(nameField.getText().trim());
            manufacturer.setCountry(countryField.getText().trim());
            manufacturer.setContactInfo(contactInfoArea.getText().trim());

            manufacturerDao.save(manufacturer);

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Ошибка при сохранении производителя", e);
            showError("Ошибка сохранения", "Не удалось сохранить производителя: " + e.getMessage());
        }
    }

    /**
     * Обработчик кнопки "Отмена"
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    /**
     * Проверка введённых данных
     */
    private boolean validateInput() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showError("Ошибка ввода", "Название производителя обязательно для заполнения");
            return false;
        }
        return true;
    }

    /**
     * Закрыть диалог
     */
    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
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

    public boolean isSaved() {
        return saved;
    }
}

