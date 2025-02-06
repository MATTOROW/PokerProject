package ru.itis.pokerproject.application;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.itis.pokerproject.service.ConnectToRoomService;
import ru.itis.pokerproject.service.CreateRoomService;
import ru.itis.pokerproject.service.GetRoomsService;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class RoomsScreen {
    private final VBox view;
    private final GetRoomsService getRoomsService;
    private final CreateRoomService createRoomService;
    private final ConnectToRoomService connectToRoomService;
    private final ScreenManager manager;
    private final TableView<TableRow> roomsTableView = new TableView<>();


    private final SimpleStringProperty usernameProperty = new SimpleStringProperty();
    private final SimpleLongProperty moneyProperty = new SimpleLongProperty();

    public RoomsScreen(GetRoomsService getRoomsService, CreateRoomService createRoomService, ConnectToRoomService connectToRoomService, ScreenManager manager) {
        this.getRoomsService = getRoomsService;
        this.createRoomService = createRoomService;
        this.manager = manager;
        this.connectToRoomService = connectToRoomService;


        usernameProperty.set(SessionStorage.getUsername());
        moneyProperty.set(SessionStorage.getMoney());


        Label titleLabel = new Label("Список комнат");
        titleLabel.setFont(new Font("Arial", 20));


        Label usernameLabel = new Label();
        usernameLabel.textProperty().bind(usernameProperty);

        Label balanceLabel = new Label();
        balanceLabel.textProperty().bind(moneyProperty.asString("Баланс: %d"));

        HBox userBox = new HBox(10, new Label("Игрок:"), usernameLabel, balanceLabel);
        userBox.setAlignment(Pos.CENTER_RIGHT);


        roomsTableView.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 10;");


        TableColumn<TableRow, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        TableColumn<TableRow, String> playersColumn = new TableColumn<>("Игроки");
        playersColumn.setCellValueFactory(cellData -> cellData.getValue().playersProperty());

        TableColumn<TableRow, String> minBetColumn = new TableColumn<>("Мин. ставка");
        minBetColumn.setCellValueFactory(cellData -> cellData.getValue().minBetProperty());


        TableColumn<TableRow, Button> connectColumn = new TableColumn<>("Подключиться");
        connectColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getConnectButton()));

        connectColumn.setCellFactory(param -> new TableCell<TableRow, Button>() {
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    item.setOnAction(event -> handleConnectButtonClick(getTableRow().getItem()));
                    setGraphic(item);
                } else {
                    setGraphic(null);
                }
            }
        });

        roomsTableView.getColumns().addAll(idColumn, playersColumn, minBetColumn, connectColumn);


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
            Platform.runLater(manager::showLoginScreen);
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

    private void handleConnectButtonClick(TableRow room) {
        String roomId = room.idProperty().get();
        new Thread(() -> {
            try {
                connectToRoomService.connectToRoom(roomId, SessionStorage.getToken());
            } catch (ClientException e) {
                manager.showErrorScreen(e.getMessage());
            }
        }).start();
    }

    public void refreshRooms() {
        new Thread(() -> {
            try {
                roomsTableView.getItems().clear();
                String[] roomsInfo = getRoomsService.getRoomsInfo();
                Platform.runLater(() -> {
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
                roomsTableView.getItems().clear();
                manager.showErrorScreen(e.getMessage());
            }
        }).start();
    }

    private void showCreateRoomDialog(TableView<TableRow> roomsTableView) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Создание комнаты");


        Label maxPlayersLabel = new Label("Макс. игроков:");
        ComboBox<Integer> maxPlayersComboBox = new ComboBox<>();
        maxPlayersComboBox.getItems().addAll(2, 3, 4);
        maxPlayersComboBox.setValue(2); // значение по умолчанию


        Label minBetLabel = new Label("Мин. ставка:");
        TextField minBetField = new TextField();
        minBetField.setPromptText("Например, 100");


        Button createButton = new Button("Создать");
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        createButton.setOnAction(event -> {
            try {

                int maxPlayers = maxPlayersComboBox.getValue();
                long minBet = Long.parseLong(minBetField.getText());

                if (maxPlayers < 2 || maxPlayers > 4 || minBet < 0) {
                    manager.showErrorScreen("Введите корректные данные");
                    return;
                }

                String roomId = createRoomService.createRoom(maxPlayers, minBet).toString();
                roomsTableView.getItems().add(new TableRow(roomId, "0/" + maxPlayers, String.valueOf(minBet)));
                dialogStage.close();
            } catch (NumberFormatException e) {
                manager.showErrorScreen("Введите числовые значения!");
            } catch (ClientException e) {
                manager.showErrorScreen("Ошибка создания комнаты: " + e.getMessage());
            }
        });


        VBox dialogLayout = new VBox(10, maxPlayersLabel, maxPlayersComboBox, minBetLabel, minBetField, createButton);
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.setPrefSize(300, 200);

        Scene dialogScene = new Scene(dialogLayout);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    public void updateUserData() {
        usernameProperty.set(SessionStorage.getUsername());
        moneyProperty.set(SessionStorage.getMoney());
    }

    public static class TableRow {
        private final SimpleStringProperty id;
        private final SimpleStringProperty players;
        private final SimpleStringProperty minBet;
        private final Button connectButton;

        public TableRow(String id, String players, String minBet) {
            this.id = new SimpleStringProperty(id);
            this.players = new SimpleStringProperty(players);
            this.minBet = new SimpleStringProperty(minBet);
            this.connectButton = new Button("Подключиться");
            this.connectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
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

        public Button getConnectButton() {
            return connectButton;
        }
    }
}
