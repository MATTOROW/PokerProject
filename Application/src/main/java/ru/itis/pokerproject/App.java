package ru.itis.pokerproject;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.itis.pokerproject.application.ConnectionErrorHandler;
import ru.itis.pokerproject.application.LoginScreen;
import ru.itis.pokerproject.shared.template.client.ClientException;
import ru.itis.pokerproject.network.SocketClient;
import ru.itis.pokerproject.service.AuthService;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class App extends Application {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25000;

    @Override
    public void start(Stage primaryStage) throws UnknownHostException, ClientException {
        // Создаем клиент для взаимодействия с сервером
        SocketClient client = new SocketClient(InetAddress.getByName(HOST), PORT);

        ConnectionErrorHandler handler = new ConnectionErrorHandler(client);

        try {
            client.connect();
        } catch (ClientException e) {
            handler.showConnectionErrorDialog(primaryStage);
        }

        // Создаем сервис для авторизации
        AuthService authService = new AuthService(client);

        // Создаем экран логина
        LoginScreen loginScreen = new LoginScreen(authService, primaryStage, handler);
        Scene scene = new Scene(loginScreen.getView(), 300, 200);

        primaryStage.setTitle("Авторизация");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
