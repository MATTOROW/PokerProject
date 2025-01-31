package ru.itis.pokerproject.gameserver;

import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.gameserver.server.listener.GetRoomsEventListener;

public class GameServer {
    private static final int PORT = 30000;
    private static final String CLIENT_SERVER_HOST = "127.0.0.1";
    private static final int CLIENT_SERVER_PORT = 25000;

    public static void main(String[] args) {
        try{
            SocketServer server = new SocketServer(PORT, CLIENT_SERVER_HOST, CLIENT_SERVER_PORT);
            server.registerListener(new GetRoomsEventListener());
            server.start();
            server.connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
