package ru.itis.pokerproject.application;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import ru.itis.pokerproject.service.GetRoomsService;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class RoomsScreen {
    private final VBox view;
    private final GetRoomsService getRoomsService;
    private final ScreenManager screenManager;

    public RoomsScreen(GetRoomsService getRoomsService, ScreenManager screenManager) {
        this.getRoomsService = getRoomsService;
        this.screenManager = screenManager;

        // Заголовок
        Label titleLabel = new Label("Список комнат");
        titleLabel.setFont(new Font("Arial", 20));

        // Список комнат
        ListView<String> roomsListView = new ListView<>();
        roomsListView.setPlaceholder(new Label("Нет доступных комнат"));

        // Кнопка для обновления списка
        Button refreshButton = new Button("Обновить");

        // Кнопка для возврата на экран входа
        Button logoutButton = new Button("Выйти");

        // Обработка кнопки "Обновить"
        refreshButton.setOnAction(event -> {
            refreshButton.setDisable(true); // Блокируем кнопку на время загрузки
            new Thread(() -> {
                try {
                    String[] roomsInfo = getRoomsService.getRoomsInfo();
                    Platform.runLater(() -> {
                        roomsListView.getItems().clear();
                        for (String room : roomsInfo) {
                            roomsListView.getItems().add(formatRoomInfo(room));
                        }
                    });
                } catch (ClientException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setContentText("Не удалось загрузить список комнат: " + e.getMessage());
                        alert.show();
                    });
                } finally {
                    Platform.runLater(() -> refreshButton.setDisable(false)); // Разблокируем кнопку
                }
            }).start();
        });

        // Обработка кнопки "Выйти"
        logoutButton.setOnAction(event -> screenManager.showLoginScreen());

        // Layout
        HBox buttonBox = new HBox(10, refreshButton, logoutButton);
        buttonBox.setAlignment(Pos.CENTER);

        view = new VBox(15, titleLabel, roomsListView, buttonBox);
        view.setAlignment(Pos.CENTER);
        view.setPrefSize(400, 300);

    }

    public VBox getView() {
        return view;
    }

    private String formatRoomInfo(String roomData) {
        // Пример строки: UUID;maxPlayers;currentPlayersCount;minBet
        String[] parts = roomData.split(";");
        if (parts.length == 4) {
            return String.format(
                    "ID: %s\nИгроки: %s/%s\nМин. ставка: %s",
                    parts[0], parts[2], parts[1], parts[3]
            );
        } else {
            return "Некорректные данные комнаты";
        }
    }
}
