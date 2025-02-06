package ru.itis.pokerproject;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.itis.pokerproject.application.ConnectionErrorHandler;
import ru.itis.pokerproject.application.ScreenManager;
import ru.itis.pokerproject.network.SocketClient;
import ru.itis.pokerproject.network.listener.*;
import ru.itis.pokerproject.shared.template.client.ClientException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class App extends Application {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 25000;
    private static SocketClient client;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws UnknownHostException {

        this.client = new SocketClient(InetAddress.getByName(HOST), PORT);

        ConnectionErrorHandler handler = new ConnectionErrorHandler(client);

        try {
            client.registerListener(new ConnectToRoomEventListener());
            client.registerListener(new ErrorEventListener());
            client.registerListener(new PlayerReadyEventListener());
            client.registerListener(new PlayerConnectedEventListener());
            client.registerListener(new PlayerDisconnectedEventListener());
            client.registerListener(new StartGameEventListener());
            client.registerListener(new WaitForActionEventListener());
            client.registerListener(new PlayerFoldedEventListener());
            client.registerListener(new PlayerCheckedEventListener());
            client.registerListener(new PlayerCalledEventListener());
            client.registerListener(new PlayerRaisedEventListener());
            client.registerListener(new PlayerAllInnedEventListener());
            client.registerListener(new CommunityCardsEventListener());
            client.registerListener(new GameEndEventListener());
            client.connect();
        } catch (ClientException e) {
            handler.showConnectionErrorDialog(primaryStage);
        }


        ScreenManager manager = new ScreenManager(primaryStage, handler, client);
        manager.showLoginScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        client.closeGameServer();
        client.close();
    }
}
