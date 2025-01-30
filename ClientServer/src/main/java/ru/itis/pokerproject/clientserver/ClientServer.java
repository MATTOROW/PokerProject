package ru.itis.pokerproject.clientserver;

import ru.itis.pokerproject.shared.template.server.Server;
import ru.itis.pokerproject.clientserver.server.SocketServer;
import ru.itis.pokerproject.clientserver.server.listener.LoginEventListener;
import ru.itis.pokerproject.clientserver.server.listener.RegisterEventListener;

public class ClientServer {
    private static final int PORT = 25000;

    public static void main(String[] args) {
        try{
            Server server = new SocketServer(PORT);
            server.registerListener(new LoginEventListener());
            server.registerListener(new RegisterEventListener());
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
