package ru.itis.pokerproject.clientserver.server;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.template.server.ServerException;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.protocol.exception.*;
import ru.itis.pokerproject.shared.template.server.Server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer implements Server<ClientMessageType, ClientServerMessage> {
    protected List<ServerEventListener<ClientMessageType, ClientServerMessage>> listeners;
    protected int port;
    protected ServerSocket server;
    protected boolean started;
    protected List<Socket> sockets;
    protected List<Socket> gameServers;

    public SocketServer(int port) {
        this.listeners = new ArrayList<>();
        this.port = port;
        this.sockets = new ArrayList<>();
        this.started = false;
    }

    @Override
    public void registerListener(ServerEventListener<ClientMessageType, ClientServerMessage> listener) throws ServerException {
        if (started) {
            throw new ServerException("Server has been started already.");
        }
        this.listeners.add(listener);
    }

    @Override
    public void start() throws ServerException {
        try {
            server = new ServerSocket(this.port);
            started = true;

            while (true) {
                Socket s = server.accept();
                handleConnection(s);
            }
        } catch (IOException e) {
            throw new ServerException("Problem with server starting.", e);
        }
    }

    protected void handleConnection(Socket socket) {
        sockets.add(socket);
        new Thread(() -> {
            int connectionId = sockets.lastIndexOf(socket);
            try (InputStream inputStream = socket.getInputStream()) {

                while (!socket.isClosed()) {
                    ClientServerMessage message = ClientServerMessageUtils.readMessage(inputStream);

                    for (ServerEventListener<ClientMessageType, ClientServerMessage> listener : listeners) {
                        if (message.getType() == listener.getType()) {
                            if (message.getType() == ClientMessageType.CONNECT_GAME_SERVER_REQUEST) {
                                gameServers.add(socket);
                            }
                            ClientServerMessage answer = listener.handle(connectionId, message);
                            sendMessage(connectionId, answer);
                        }
                    }
                }
            } catch (EmptyMessageException | MessageReadingException e) {
                sockets.remove(socket);
                gameServers.remove(socket);
            } catch (ExceedingLengthException | UnknownMessageTypeException | WrongStartBytesException e) {
                ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                        ClientMessageType.ERROR,
                        "Error while connecting to server.".getBytes()
                );
                sendMessage(connectionId, errorMessage);
            } catch (IOException | ServerEventListenerException e) {
                System.err.println("Error handling connection: " + e.getMessage());
            } finally {
                sockets.remove(socket);
            }
        }).start();
    }

    @Override
    public void sendMessage(int connectionId, ClientServerMessage message) throws ServerException {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            Socket socket = sockets.get(connectionId);
            socket.getOutputStream().write(ClientServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void sendBroadCastMessage(ClientServerMessage message) throws ServerException {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            byte[] rawMessage = ClientServerMessageUtils.getBytes(message);
            for (Socket socket : sockets) {
                socket.getOutputStream().write(rawMessage);
                socket.getOutputStream().flush();

            }
        } catch (IOException e) {
            throw new ServerException("Can't send message.", e);
        }
    }
}
