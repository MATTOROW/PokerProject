package ru.itis.pokerproject.application;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.itis.pokerproject.network.SocketClient;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class ConnectionErrorHandler {
    private final SocketClient client;

    public ConnectionErrorHandler(SocketClient client) {
        this.client = client;
    }

    public void showConnectionErrorDialog(Stage primaryStage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(primaryStage);
            alert.setTitle("Ошибка подключения");
            alert.setHeaderText("Проблемы с соединением");
            alert.setContentText("Пожалуйста, проверьте подключение к интернету и попробуйте снова.");

            ButtonType reconnectButton = new ButtonType("Повторить");
            ButtonType closeButton = new ButtonType("Закрыть");

            alert.getButtonTypes().setAll(reconnectButton, closeButton);

            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == reconnectButton) {
                    tryReconnect(primaryStage);
                } else {
                    Platform.exit();
                }
            });
        });
    }

    public void tryReconnect(Stage primaryStage) {
        new Thread(() -> {
            try {
                client.reconnect();
                Platform.runLater(() -> {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Успех");
                    successAlert.setHeaderText("Подключение восстановлено");
                    successAlert.setContentText("Соединение с сервером успешно восстановлено.");
                    successAlert.show();
                });
            } catch (ClientException e) {
                showConnectionErrorDialog(primaryStage);
            }
        }).start();
    }
}
