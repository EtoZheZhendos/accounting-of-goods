package com.store.inventory.controller;

import com.store.inventory.domain.Manufacturer;
import com.store.inventory.domain.Nomenclature;
import com.store.inventory.repository.ManufacturerDao;
import com.store.inventory.repository.NomenclatureDao;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер диалога добавления/редактирования номенклатуры
 */
public class NomenclatureDialogController {

    private static final Logger logger = LoggerFactory.getLogger(NomenclatureDialogController.class);

    @FXML private TextField articleField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField unitField;
    @FXML private ComboBox<Manufacturer> manufacturerCombo;
    @FXML private Spinner<Integer> minStockSpinner;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ManufacturerDao manufacturerDao = new ManufacturerDao();
    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();

    private Nomenclature nomenclature;
    private boolean saved = false;

    /**
     * Инициализация контроллера
     */
    @FXML
    public void initialize() {
        // Загружаем производителей
        loadManufacturers();

        // Настраиваем спиннер для минимального запаса
        minStockSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0)
        );
    }

    /**
     * Загрузка списка производителей
     */
    private void loadManufacturers() {
        try {
            var manufacturers = manufacturerDao.findAll();
            manufacturerCombo.setItems(FXCollections.observableArrayList(manufacturers));
            
            // Настраиваем отображение производителей
            manufacturerCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Manufacturer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
            
            manufacturerCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Manufacturer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка загрузки производителей", e);
            showError("Ошибка", "Не удалось загрузить список производителей");
        }
    }

    /**
     * Установить номенклатуру для редактирования
     */
    public void setNomenclature(Nomenclature nomenclature) {
        this.nomenclature = nomenclature;
        
        if (nomenclature != null) {
            articleField.setText(nomenclature.getArticle());
            nameField.setText(nomenclature.getName());
            descriptionArea.setText(nomenclature.getDescription());
            unitField.setText(nomenclature.getUnit());
            manufacturerCombo.setValue(nomenclature.getManufacturer());
            minStockSpinner.getValueFactory().setValue(nomenclature.getMinStockLevel());
            
            // При редактировании артикул нельзя менять
            articleField.setDisable(true);
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
            if (nomenclature == null) {
                // Создаём новую номенклатуру
                nomenclature = new Nomenclature();
                nomenclature.setArticle(articleField.getText().trim());
            }

            // Обновляем поля
            nomenclature.setName(nameField.getText().trim());
            nomenclature.setDescription(descriptionArea.getText().trim());
            nomenclature.setUnit(unitField.getText().trim());
            nomenclature.setManufacturer(manufacturerCombo.getValue());
            nomenclature.setMinStockLevel(minStockSpinner.getValue());

            // Сохраняем в БД
            nomenclatureDao.save(nomenclature);

            saved = true;
            closeDialog();

        } catch (Exception e) {
            logger.error("Ошибка при сохранении номенклатуры", e);
            showError("Ошибка сохранения", "Не удалось сохранить номенклатуру: " + e.getMessage());
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
        StringBuilder errors = new StringBuilder();

        if (articleField.getText() == null || articleField.getText().trim().isEmpty()) {
            errors.append("Артикул обязателен для заполнения\n");
        }

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errors.append("Наименование обязательно для заполнения\n");
        }

        if (unitField.getText() == null || unitField.getText().trim().isEmpty()) {
            errors.append("Единица измерения обязательна для заполнения\n");
        }

        if (errors.length() > 0) {
            showError("Ошибка ввода", errors.toString());
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

    /**
     * Проверить, были ли сохранены изменения
     */
    public boolean isSaved() {
        return saved;
    }
}

