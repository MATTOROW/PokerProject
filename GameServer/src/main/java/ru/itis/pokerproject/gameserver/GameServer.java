package ru.itis.pokerproject.gameserver;

import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.gameserver.server.listener.GetRoomsEventListener;

public class GameServer {
    private static final int PORT = 25000;

    public static void main(String[] args) {
        try{
            SocketServer server = new SocketServer(PORT);
            server.registerListener(new GetRoomsEventListener());
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
