package ru.itis.pokerproject.application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.itis.pokerproject.network.SocketClient;
import ru.itis.pokerproject.service.AuthService;
import ru.itis.pokerproject.service.ConnectToRoomService;
import ru.itis.pokerproject.service.CreateRoomService;
import ru.itis.pokerproject.service.GetRoomsService;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.template.client.Client;

public class ScreenManager {
    private final Stage primaryStage;
    private final LoginScreen loginScreen;
    private final RegisterScreen registerScreen;
    private final RoomsScreen roomsScreen;

    private final AuthService authService;
    private final GetRoomsService getRoomsService;
    private final CreateRoomService createRoomService;
    private final ConnectToRoomService connectToRoomService;

    public ScreenManager(Stage primaryStage, ConnectionErrorHandler errorHandler, SocketClient client) {
        this.authService = new AuthService(client);
        this.getRoomsService = new GetRoomsService(client);
        this.createRoomService = new CreateRoomService(client);
        this.connectToRoomService = new ConnectToRoomService(client);

        this.primaryStage = primaryStage;

        // Создаем экраны один раз
        this.loginScreen = new LoginScreen(authService, this);
        this.registerScreen = new RegisterScreen(authService, this);
        this.roomsScreen = new RoomsScreen(getRoomsService, createRoomService, connectToRoomService, this);

        // Устанавливаем начальный экран
        primaryStage.setScene(new Scene(loginScreen.getView()));
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public void showLoginScreen() {
        primaryStage.getScene().setRoot(loginScreen.getView());
        primaryStage.sizeToScene();
    }

    public void showRegisterScreen() {
        primaryStage.getScene().setRoot(registerScreen.getView());
        primaryStage.sizeToScene();
    }

    public void showRoomsScreen() {
        roomsScreen.updateUserData();
        roomsScreen.refreshRooms();
        primaryStage.getScene().setRoot(roomsScreen.getView());
        primaryStage.sizeToScene();
    }

    public void showErrorScreen(String message) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.setTitle("Ошибка!");
                    alert.setContentText(message);
                    alert.show();
                }
        );
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
