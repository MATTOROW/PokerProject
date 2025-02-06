package ru.itis.pokerproject.clientserver;

import ru.itis.pokerproject.clientserver.server.SocketServer;
import ru.itis.pokerproject.clientserver.server.listener.*;

public class ClientServer {
    private static final int PORT = 25000;

    public static void main(String[] args) {
        try {
            SocketServer server = new SocketServer(PORT);
            server.registerListener(new LoginEventListener());
            server.registerListener(new RegisterEventListener());
            server.registerListener(new RoomsRequestEventListener());
            server.registerListener(new CreateRoomEventListener());
            server.registerListener(new ConnectToRoomEventListener());
            server.registerGameServerListener(new UpdateUserDataEventListener());
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
