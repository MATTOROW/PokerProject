package ru.itis.pokerproject.gameserver.network;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 50000);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                String answer = reader.readLine();
                if (answer == null) {
                    continue;
                }
                System.out.println(answer);
                if (answer.equals("Disconnected.")) {
                    break;
                }
            }
            writer.close();
            reader.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
