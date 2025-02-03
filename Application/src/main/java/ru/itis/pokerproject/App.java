package ru.itis.pokerproject;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.itis.pokerproject.application.ConnectionErrorHandler;
import ru.itis.pokerproject.application.ScreenManager;
import ru.itis.pokerproject.network.listener.ConnectToRoomEventListener;
import ru.itis.pokerproject.network.listener.ErrorEventListener;
import ru.itis.pokerproject.network.listener.PlayerConnectedEventListener;
import ru.itis.pokerproject.shared.template.client.ClientException;
import ru.itis.pokerproject.network.SocketClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class App extends Application {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25000;

    @Override
    public void start(Stage primaryStage) throws UnknownHostException {
        // Создаем клиент для взаимодействия с сервером
        SocketClient client = new SocketClient(InetAddress.getByName(HOST), PORT);

        ConnectionErrorHandler handler = new ConnectionErrorHandler(client);

        try {
            client.registerListener(new ConnectToRoomEventListener());
            client.registerListener(new ErrorEventListener());
            client.registerListener(new PlayerConnectedEventListener());
            client.connect();
        } catch (ClientException e) {
            handler.showConnectionErrorDialog(primaryStage);
        }

        // Создаем экран логина
        ScreenManager manager = new ScreenManager(primaryStage, handler, client);
        manager.showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
