package ru.itis.pokerproject.clientserver.server;

import ru.itis.pokerproject.clientserver.server.listeners.LoginEventListener;

public class ClientServer {
    private static final int PORT = 1234;

    public static void main(String[] args) {
        try{
            ServerExample server = new NioServerExample(PORT);
            server.registerListener(new LoginEventListener());
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
