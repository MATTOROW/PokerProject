package ru.itis.pokerproject.application;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.itis.pokerproject.service.CreateRoomService;
import ru.itis.pokerproject.service.GetRoomsService;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class RoomsScreen {
    private final VBox view;
    private final GetRoomsService getRoomsService;
    private final CreateRoomService createRoomService;
    private final ScreenManager screenManager;
    private final TableView<TableRow> roomsTableView = new TableView<>();

    // Данные сессии
    private final SimpleStringProperty usernameProperty = new SimpleStringProperty();
    private final SimpleLongProperty moneyProperty = new SimpleLongProperty();

    public RoomsScreen(GetRoomsService getRoomsService, CreateRoomService createRoomService, ScreenManager screenManager) {
        this.getRoomsService = getRoomsService;
        this.createRoomService = createRoomService;
        this.screenManager = screenManager;

        // Получаем данные из сессии
        usernameProperty.set(SessionStorage.getUsername());
        moneyProperty.set(SessionStorage.getMoney());

        // Заголовок
        Label titleLabel = new Label("Список комнат");
        titleLabel.setFont(new Font("Arial", 20));

        // Панель пользователя (отображает username и баланс)
        Label usernameLabel = new Label();
        usernameLabel.textProperty().bind(usernameProperty);

        Label balanceLabel = new Label();
        balanceLabel.textProperty().bind(moneyProperty.asString("Баланс: %d"));

        HBox userBox = new HBox(10, new Label("Игрок:"), usernameLabel, balanceLabel);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        // Таблица комнат
        roomsTableView.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 10;");

        // Столбцы
        TableColumn<TableRow, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        TableColumn<TableRow, String> playersColumn = new TableColumn<>("Игроки");
        playersColumn.setCellValueFactory(cellData -> cellData.getValue().playersProperty());

        TableColumn<TableRow, String> minBetColumn = new TableColumn<>("Мин. ставка");
        minBetColumn.setCellValueFactory(cellData -> cellData.getValue().minBetProperty());

        roomsTableView.getColumns().addAll(idColumn, playersColumn, minBetColumn);

        // Кнопки
        Button createRoomButton = new Button("Создать комнату");
        createRoomButton.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-font-weight: bold;");
        createRoomButton.setPrefWidth(150);
        createRoomButton.setOnAction(event -> showCreateRoomDialog(roomsTableView));

        Button refreshButton = new Button("Обновить");
        refreshButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshButton.setPrefWidth(100);
        refreshButton.setOnAction(event -> {
            refreshButton.setDisable(true);
            refreshRooms();
            refreshButton.setDisable(false);
        });

        Button logoutButton = new Button("Выйти");
        logoutButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutButton.setPrefWidth(100);
        logoutButton.setOnAction(event -> {
            SessionStorage.clear();
            Platform.runLater(screenManager::showLoginScreen);
        });

        HBox buttonBox = new HBox(10, refreshButton, logoutButton, createRoomButton);
        buttonBox.setAlignment(Pos.CENTER);

        view = new VBox(15, userBox, titleLabel, roomsTableView, buttonBox);
        view.setAlignment(Pos.CENTER);
        view.setPrefSize(600, 400);
    }

    public VBox getView() {
        return view;
    }

    public void refreshRooms() {
        new Thread(() -> {
            try {
                String[] roomsInfo = getRoomsService.getRoomsInfo();
                Platform.runLater(() -> {
                    roomsTableView.getItems().clear();
                    for (String room : roomsInfo) {
                        String[] parts = room.split(";");
                        if (parts.length == 4) {
                            roomsTableView.getItems().add(new TableRow(parts[0], parts[2] + "/" + parts[1], parts[3]));
                        } else {
                            roomsTableView.getItems().add(new TableRow("Ошибка", "0/0", "0"));
                        }
                    }
                });
            } catch (ClientException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setContentText("Не удалось загрузить список комнат: " + e.getMessage());
                    alert.show();
                });
            }
        }).start();
    }

    private void showCreateRoomDialog(TableView<TableRow> roomsTableView) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Создание комнаты");

        Label maxPlayersLabel = new Label("Макс. игроков:");
        TextField maxPlayersField = new TextField();
        maxPlayersField.setPromptText("Например, 6");

        Label minBetLabel = new Label("Мин. ставка:");
        TextField minBetField = new TextField();
        minBetField.setPromptText("Например, 100");

        Button createButton = new Button("Создать");
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        createButton.setOnAction(event -> {
            try {
                int maxPlayers = Integer.parseInt(maxPlayersField.getText());
                long minBet = Long.parseLong(minBetField.getText());

                if (maxPlayers <= 0 || minBet < 0) {
                    showAlert("Ошибка", "Введите корректные данные");
                    return;
                }

                String roomId = createRoomService.createRoom(maxPlayers, minBet).toString();
                roomsTableView.getItems().add(new TableRow(roomId, "0/" + maxPlayers, String.valueOf(minBet)));
                dialogStage.close();
            } catch (NumberFormatException e) {
                showAlert("Ошибка", "Введите числовые значения!");
            } catch (ClientException e) {
                showAlert("Ошибка", "Ошибка создания комнаты: " + e.getMessage());
            }
        });

        VBox dialogLayout = new VBox(10, maxPlayersLabel, maxPlayersField, minBetLabel, minBetField, createButton);
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.setPrefSize(300, 200);

        Scene dialogScene = new Scene(dialogLayout);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    public static class TableRow {
        private final SimpleStringProperty id;
        private final SimpleStringProperty players;
        private final SimpleStringProperty minBet;

        public TableRow(String id, String players, String minBet) {
            this.id = new SimpleStringProperty(id);
            this.players = new SimpleStringProperty(players);
            this.minBet = new SimpleStringProperty(minBet);
        }

        public SimpleStringProperty idProperty() {
            return id;
        }

        public SimpleStringProperty playersProperty() {
            return players;
        }

        public SimpleStringProperty minBetProperty() {
            return minBet;
        }
    }

    public void updateUserData() {
        usernameProperty.set(SessionStorage.getUsername());
        moneyProperty.set(SessionStorage.getMoney());
    }
}
