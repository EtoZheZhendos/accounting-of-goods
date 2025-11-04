package com.store.inventory.controller;

import com.store.inventory.domain.*;
import com.store.inventory.repository.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контроллер диалога просмотра истории операций с товаром
 */
public class ItemHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(ItemHistoryController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @FXML private ComboBox<Nomenclature> nomenclatureCombo;
    @FXML private ComboBox<Item> itemCombo;
    @FXML private TableView<HistoryRow> historyTable;
    @FXML private TableColumn<HistoryRow, String> dateTimeCol;
    @FXML private TableColumn<HistoryRow, String> operationTypeCol;
    @FXML private TableColumn<HistoryRow, String> documentNumberCol;
    @FXML private TableColumn<HistoryRow, BigDecimal> quantityChangeCol;
    @FXML private TableColumn<HistoryRow, BigDecimal> balanceCol;
    @FXML private TableColumn<HistoryRow, String> shelfCol;
    @FXML private TableColumn<HistoryRow, String> performerCol;
    @FXML private Button closeButton;

    private final NomenclatureDao nomenclatureDao = new NomenclatureDao();
    private final ItemDao itemDao = new ItemDao();
    private final HistoryDao historyDao = new HistoryDao();

    private ObservableList<HistoryRow> historyData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настройка колонок таблицы
        dateTimeCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        operationTypeCol.setCellValueFactory(new PropertyValueFactory<>("operationType"));
        documentNumberCol.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        quantityChangeCol.setCellValueFactory(new PropertyValueFactory<>("quantityChange"));
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        shelfCol.setCellValueFactory(new PropertyValueFactory<>("shelf"));
        performerCol.setCellValueFactory(new PropertyValueFactory<>("performer"));

        historyTable.setItems(historyData);

        // Загрузка номенклатуры
        loadNomenclatures();

        // Обработчики изменений
        nomenclatureCombo.setOnAction(e -> loadItemsByNomenclature());
        itemCombo.setOnAction(e -> loadHistory());
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

    private void loadItemsByNomenclature() {
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
                        setText(String.format("Партия: %s (остаток: %.2f)", 
                            item.getBatchNumber(), item.getQuantity()));
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
                        setText(String.format("Партия: %s (остаток: %.2f)", 
                            item.getBatchNumber(), item.getQuantity()));
                    }
                }
            });
            
            if (!items.isEmpty()) {
                itemCombo.setValue(items.get(0));
                loadHistory();
            }
        } else {
            itemCombo.setItems(FXCollections.observableArrayList());
            historyData.clear();
        }
    }

    private void loadHistory() {
        Item item = itemCombo.getValue();
        if (item != null) {
            List<History> history = historyDao.findByItem(item);
            
            historyData.clear();
            for (History h : history) {
                historyData.add(new HistoryRow(h));
            }
        } else {
            historyData.clear();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Вспомогательный класс для отображения истории
     */
    public static class HistoryRow {
        private final String dateTime;
        private final String operationType;
        private final String documentNumber;
        private final BigDecimal quantityChange;
        private final BigDecimal balance;
        private final String shelf;
        private final String performer;

        public HistoryRow(History history) {
            this.dateTime = history.getOperationDate().format(DATE_TIME_FORMATTER);
            this.operationType = translateOperationType(history.getOperationType());
            this.documentNumber = history.getDocument() != null ? history.getDocument().getDocumentNumber() : "-";
            this.quantityChange = history.getQuantityChange();
            // TODO: Рассчитать баланс, если требуется
            this.balance = BigDecimal.ZERO;
            // Для операций перемещения показываем обе полки
            if (history.getFromShelf() != null && history.getToShelf() != null) {
                this.shelf = history.getFromShelf().getCode() + " → " + history.getToShelf().getCode();
            } else if (history.getToShelf() != null) {
                this.shelf = history.getToShelf().getCode();
            } else if (history.getFromShelf() != null) {
                this.shelf = history.getFromShelf().getCode();
            } else {
                this.shelf = "-";
            }
            this.performer = history.getCreatedBy();
        }

        private String translateOperationType(OperationType type) {
            if (type == null) {
                return "Неизвестно";
            }
            return switch (type) {
                case RECEIPT -> "Поступление";
                case SALE -> "Реализация";
                case MOVEMENT -> "Перемещение";
                case WRITE_OFF -> "Списание";
                default -> "Другое";
            };
        }

        // Геттеры для PropertyValueFactory
        public String getDateTime() { return dateTime; }
        public String getOperationType() { return operationType; }
        public String getDocumentNumber() { return documentNumber; }
        public BigDecimal getQuantityChange() { return quantityChange; }
        public BigDecimal getBalance() { return balance; }
        public String getShelf() { return shelf; }
        public String getPerformer() { return performer; }
    }
}

