package ru.itis.pokerproject.network;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.template.client.Client;
import ru.itis.pokerproject.shared.template.client.ClientException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class SocketClient implements Client {

    protected final InetAddress address;
    protected final int port;
    protected Socket socket;

    public SocketClient(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public void connect() throws ClientException {
        try{
            socket = new Socket(address, port);
        }
        catch(IOException ex){
            throw new ClientException("Can't connect.", ex);
        }
    }

    @Override
    public ClientServerMessage sendMessage(ClientServerMessage message) throws ClientException {
        if (socket == null || socket.isClosed()) {
            throw new ClientException("Socket is not connected.");
        }
        try{
            socket.getOutputStream().write(ClientServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();
            return ClientServerMessageUtils.readMessage(socket.getInputStream());
        }
        catch(IOException ex){
            throw new ClientException("Can't send message.", ex);
        }
    }

    public void close() throws ClientException {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            throw new ClientException("Can't close socket.", ex);
        }
    }

    public void reconnect() throws ClientException {
        close();
        connect();
    }
}
