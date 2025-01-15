package ru.itis.pokerproject.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int SERVER_PORT = 50000;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
            System.out.println("Start");
            System.out.println("Wait client connection");
            GameThread game = new GameThread(2);
            Thread thread = new Thread(game);
            while (game.countPlayers() < 2) {
                Socket clientSocket = server.accept();
                System.out.println("Client connected, address: " + clientSocket.getInetAddress());
                game.addPlayerSocket(clientSocket);
            }
            thread.start();
            System.out.println("Room is full!");
        } catch (IOException ignored) {
            throw new RuntimeException();
        }
        System.out.println("Server shutting down");
    }
}
