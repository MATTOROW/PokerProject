package ru.itis.pokerproject.application;

import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.itis.pokerproject.service.AuthService;
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

    public ScreenManager(Stage primaryStage, ConnectionErrorHandler errorHandler, Client<ClientMessageType, ClientServerMessage> client) {
        this.authService = new AuthService(client);
        this.getRoomsService = new GetRoomsService(client);
        this.createRoomService = new CreateRoomService(client);

        this.primaryStage = primaryStage;

        // Создаем экраны один раз
        this.loginScreen = new LoginScreen(authService, this);
        this.registerScreen = new RegisterScreen(authService, this);
        this.roomsScreen = new RoomsScreen(getRoomsService, createRoomService,this);

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
        primaryStage.getScene().setRoot(roomsScreen.getView());
        primaryStage.sizeToScene();
    }
}
