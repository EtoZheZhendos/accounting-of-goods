package com.store.inventory.controller;

import com.store.inventory.domain.Document;
import com.store.inventory.domain.DocumentItem;
import com.store.inventory.repository.DocumentItemDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контроллер диалога просмотра документа
 * 
 * <p>Отображает полную информацию о документе, включая все его позиции (товары).
 * Используется для просмотра уже созданных документов без возможности редактирования.</p>
 */
public class DocumentViewDialogController {

    @FXML private Label titleLabel;
    @FXML private TextField documentNumberField;
    @FXML private TextField documentTypeField;
    @FXML private TextField documentDateField;
    @FXML private TextField warehouseField;
    @FXML private TextField counterpartyField;
    @FXML private TextField statusField;
    @FXML private TextField totalAmountField;
    @FXML private TableView<DocumentItem> itemsTable;
    @FXML private TableColumn<DocumentItem, String> itemArticleCol;
    @FXML private TableColumn<DocumentItem, String> itemNameCol;
    @FXML private TableColumn<DocumentItem, BigDecimal> itemQuantityCol;
    @FXML private TableColumn<DocumentItem, String> itemUnitCol;
    @FXML private TableColumn<DocumentItem, BigDecimal> itemPriceCol;
    @FXML private TableColumn<DocumentItem, BigDecimal> itemTotalCol;
    @FXML private TableColumn<DocumentItem, String> itemShelfCol;
    @FXML private TextArea notesArea;

    private Document document;
    private final DocumentItemDao documentItemDao = new DocumentItemDao();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Инициализирует контроллер и настраивает таблицу позиций
     */
    @FXML
    private void initialize() {
        setupItemsTable();
    }

    /**
     * Настраивает колонки таблицы позиций документа
     */
    private void setupItemsTable() {
        itemArticleCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getNomenclature().getArticle()));
        
        itemNameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getNomenclature().getName()));
        
        itemQuantityCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getQuantity()));
        itemQuantityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        itemUnitCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getNomenclature().getUnit()));
        
        itemPriceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getPrice()));
        itemPriceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₽", item));
                }
            }
        });
        
        itemTotalCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getTotal()));
        itemTotalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₽", item));
                }
            }
        });
        
        itemShelfCol.setCellValueFactory(cellData -> {
            var shelf = cellData.getValue().getShelf();
            return new javafx.beans.property.SimpleStringProperty(
                shelf != null ? shelf.getCode() : "-");
        });
    }

    /**
     * Устанавливает документ для отображения
     * 
     * <p>Загружает информацию о документе и все его позиции из базы данных.</p>
     * 
     * @param document документ для отображения
     */
    public void setDocument(Document document) {
        this.document = document;
        loadDocumentData();
        loadDocumentItems();
    }

    /**
     * Загружает данные документа в поля формы
     */
    private void loadDocumentData() {
        if (document == null) return;

        titleLabel.setText("Документ " + document.getDocumentType().getDisplayName() + 
                          " №" + document.getDocumentNumber());
        documentNumberField.setText(document.getDocumentNumber());
        documentTypeField.setText(document.getDocumentType().getDisplayName());
        documentDateField.setText(document.getDocumentDate().format(dateFormatter));
        warehouseField.setText(document.getWarehouse() != null ? 
                              document.getWarehouse().getName() : "-");
        counterpartyField.setText(document.getCounterparty() != null ? 
                                 document.getCounterparty() : "-");
        statusField.setText(document.getStatus().getDisplayName());
        totalAmountField.setText(document.getTotalAmount() != null ? 
                                String.format("%.2f ₽", document.getTotalAmount()) : "0.00 ₽");
        notesArea.setText(document.getNotes() != null ? document.getNotes() : "");
    }

    /**
     * Загружает позиции документа в таблицу
     */
    private void loadDocumentItems() {
        try {
            List<DocumentItem> items = documentItemDao.findByDocument(document);
            ObservableList<DocumentItem> observableItems = FXCollections.observableArrayList(items);
            itemsTable.setItems(observableItems);
        } catch (Exception e) {
            showError("Ошибка при загрузке позиций документа: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обработчик кнопки "Закрыть"
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) documentNumberField.getScene().getWindow();
        stage.close();
    }

    /**
     * Отображает сообщение об ошибке
     * 
     * @param message текст сообщения
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

