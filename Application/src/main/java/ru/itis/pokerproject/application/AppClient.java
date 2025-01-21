package ru.itis.pokerproject.application;

import ru.itis.pokerproject.clientserver.model.AccountEntity;
import ru.itis.pokerproject.shared.dto.response.AccountResponse;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AppClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 1234;

    public static void main(String[] args) {
        try{
            ClientExample client = new SocketClientExample(InetAddress.getByName(HOST), PORT);
            client.connect();
            byte[] data = ByteBuffer.allocate("ADMIN;ADMIN_TOP".getBytes(StandardCharsets.UTF_8).length).put("ADMIN;ADMIN_TOP".getBytes(StandardCharsets.UTF_8)).array();
            // Message request = Message.createMessage(-1, data);// Bad request
            ClientServerMessage request = ClientServerMessage.createMessage(ClientServerMessage.MessageType.LOGIN_REQUEST, data);
            System.out.println("Request");
            System.out.println(ClientServerMessage.toString(request));
            System.out.println();
            ClientServerMessage response = client.sendMessage(request);
            System.out.println("Response");
            System.out.println(ClientServerMessage.toString(response));
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getData());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            System.out.println(((AccountResponse) objectInputStream.readObject()).money());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
