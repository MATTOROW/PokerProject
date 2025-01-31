package ru.itis.pokerproject.clientserver;

import ru.itis.pokerproject.clientserver.server.SocketServer;
import ru.itis.pokerproject.clientserver.server.listener.LoginEventListener;
import ru.itis.pokerproject.clientserver.server.listener.RegisterEventListener;
import ru.itis.pokerproject.clientserver.server.listener.RoomsRequestEventListener;

public class ClientServer {
    private static final int PORT = 25000;

    public static void main(String[] args) {
        try{
            SocketServer server = new SocketServer(PORT);
            server.registerListener(new LoginEventListener());
            server.registerListener(new RegisterEventListener());
            server.registerListener(new RoomsRequestEventListener());
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
