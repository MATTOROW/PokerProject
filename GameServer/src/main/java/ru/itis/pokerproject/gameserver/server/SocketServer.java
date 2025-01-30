package ru.itis.pokerproject.gameserver.server;

import ru.itis.pokerproject.shared.protocol.exception.*;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;
import ru.itis.pokerproject.shared.template.server.AbstractSocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.UUID;

public class SocketServer extends AbstractSocketServer<GameMessageType, GameServerMessage> {
    private final UUID id = UUID.randomUUID();

    public SocketServer(int port) {
        super(port);
    }

    protected void handleConnection(Socket socket) {
        sockets.add(socket);
        new Thread(() -> {
            int connectionId = sockets.lastIndexOf(socket);
            try (InputStream inputStream = socket.getInputStream()) {

                while (!socket.isClosed()) {
                    GameServerMessage message = GameServerMessageUtils.readMessage(inputStream);

                    for (ServerEventListener<GameMessageType, GameServerMessage> listener : listeners) {
                        if (message.getType() == listener.getType()) {
                            GameServerMessage answer = listener.handle(connectionId, message);
                            sendMessage(connectionId, answer);
                        }
                    }
                }
            } catch (EmptyMessageException | MessageReadingException e) {
                sockets.remove(socket);
            } catch (ExceedingLengthException | UnknownMessageTypeException | WrongStartBytesException e) {
                GameServerMessage errorMessage = GameServerMessageUtils.createMessage(
                        GameMessageType.ERROR,
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
    public void sendMessage(int connectionId, GameServerMessage message) throws ServerException {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            Socket socket = sockets.get(connectionId);
            socket.getOutputStream().write(GameServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void sendBroadCastMessage(GameServerMessage message) throws ServerException {

    }
}
