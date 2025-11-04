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
    private TableView<NomenclatureStock> stockTable;
    @FXML
    private TableColumn<NomenclatureStock, String> stockArticleCol;
    @FXML
    private TableColumn<NomenclatureStock, String> stockNameCol;
    @FXML
    private TableColumn<NomenclatureStock, String> stockManufacturerCol;
    @FXML
    private TableColumn<NomenclatureStock, BigDecimal> stockQuantityCol;
    @FXML
    private TableColumn<NomenclatureStock, String> stockUnitCol;

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

            loadAllData();

            statusLabel.setText("Готово");
            logger.info("Главное окно инициализировано");

        } catch (Exception e) {
            logger.error("Ошибка при инициализации главного окна", e);
            showError("Ошибка инициализации", e.getMessage());
        }
    }

    /**
     * Инициализация таблицы остатков
     */
    private void initializeStockTable() {
        stockArticleCol.setCellValueFactory(new PropertyValueFactory<>("article"));
        stockNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        stockManufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
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
            Map<Nomenclature, BigDecimal> stockReport = reportService.getStockReport();
            ObservableList<NomenclatureStock> stockList = FXCollections.observableArrayList();

            stockReport.forEach((nom, qty) -> {
                String manufacturerName = nom.getManufacturer() != null ? nom.getManufacturer().getName() : "";
                stockList.add(new NomenclatureStock(
                        nom.getArticle(),
                        nom.getName(),
                        manufacturerName,
                        qty,
                        nom.getUnit()
                ));
            });

            stockTable.setItems(stockList);
            logger.info("Загружено остатков: {}", stockList.size());

        } catch (Exception e) {
            logger.error("Ошибка при загрузке остатков", e);
            showError("Ошибка загрузки", e.getMessage());
        }
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

