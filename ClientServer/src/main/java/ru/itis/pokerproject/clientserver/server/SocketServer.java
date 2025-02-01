package ru.itis.pokerproject.clientserver.server;

import ru.itis.pokerproject.clientserver.service.CreateRoomService;
import ru.itis.pokerproject.clientserver.service.GetRoomsService;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.server.AbstractSocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.protocol.exception.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer extends AbstractSocketServer<ClientMessageType, ClientServerMessage> {
    protected List<Socket> gameServersToListen;
    protected List<Socket> gameServersToSend;
    protected List<ServerEventListener<ClientMessageType, ClientServerMessage>> gameServerListeners;

    public SocketServer(int port) {
        super(port);
        gameServersToListen = new ArrayList<>();
        gameServersToSend = new ArrayList<>();
        gameServerListeners = new ArrayList<>();
        GetRoomsService.init(this);
        CreateRoomService.init(this);
    }

    public void registerGameServerListener(ServerEventListener<ClientMessageType, ClientServerMessage> listener) throws ServerException {
        if (started) {
            throw new ServerException("Server has been started already.");
        }
        this.gameServerListeners.add(listener);
    }

    protected void handleConnection(Socket socket) {
        sockets.add(socket);
        new Thread(() -> {
            int connectionId = sockets.lastIndexOf(socket);
            try {
                InputStream inputStream = socket.getInputStream();
                while (!socket.isClosed() && sockets.contains(socket)) {
                    ClientServerMessage message = ClientServerMessageUtils.readMessage(inputStream);

                    if (message.getType() == ClientMessageType.REGISTER_GAME_SERVER_REQUEST) {
                        sockets.remove(socket);
                        gameServersToListen.add(socket);
                        String[] connectionData = new String(message.getData()).split(":");
                        gameServersToSend.add(new Socket(connectionData[0], Integer.parseInt(connectionData[1])));
                        connectionId = gameServersToListen.lastIndexOf(socket);
                        inputStream = null;
                        sendMessageToGameServer(connectionId, ClientServerMessageUtils.createMessage(ClientMessageType.REGISTER_GAME_SERVER_RESPONSE, new byte[0]));
                        handleServerConnection(socket);
                    } else {
                        for (ServerEventListener<ClientMessageType, ClientServerMessage> listener : listeners) {
                            if (message.getType() == listener.getType()) {
                                System.out.println("Нашелся нужный!" + message.getType());
                                ClientServerMessage answer = listener.handle(connectionId, message);
                                sendMessage(connectionId, answer);
                            }
                        }
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (EmptyMessageException | MessageReadingException e) {

            } catch (ExceedingLengthException | UnknownMessageTypeException | WrongStartBytesException e) {
                ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                        ClientMessageType.ERROR,
                        "Error while connecting to server.".getBytes()
                );
                System.out.println(e.getMessage());
                sendMessage(connectionId, errorMessage);
            } catch (IOException | ServerEventListenerException e) {
                System.err.println("Error handling connection: " + e.getMessage());
            } finally {
                sockets.remove(socket);
            }
        }).start();
    }

    protected void handleServerConnection(Socket socket) {
        new Thread(() -> {
            int connectionId = gameServersToListen.lastIndexOf(socket);
            try (InputStream inputStream = socket.getInputStream()) {

                while (!socket.isClosed()) {
                    ClientServerMessage message = ClientServerMessageUtils.readMessage(inputStream);
                    for (ServerEventListener<ClientMessageType, ClientServerMessage> listener : gameServerListeners) {
                        if (message.getType() == listener.getType()) {
                            System.out.println("Нашелся нужный!" + message.getType());
                            ClientServerMessage answer = listener.handle(connectionId, message);
                            sendMessage(connectionId, answer);
                        }
                    }
                }
            } catch (EmptyMessageException | MessageReadingException e) {
                e.printStackTrace();
                gameServersToListen.remove(connectionId);
                gameServersToSend.remove(connectionId);
            } catch (ExceedingLengthException | UnknownMessageTypeException | WrongStartBytesException e) {
                ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                        ClientMessageType.ERROR,
                        "Error while connecting to server.".getBytes()
                );
                sendMessage(connectionId, errorMessage);
            } catch (IOException | ServerEventListenerException e) {
                System.err.println("Error handling connection: " + e.getMessage());
            } finally {
                if (gameServersToListen.contains(socket)) {
                    gameServersToListen.remove(connectionId);
                    gameServersToSend.remove(connectionId);
                }
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

    public void sendMessageToGameServer(int serverConnectionId, ClientServerMessage message) {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            Socket socket = gameServersToListen.get(serverConnectionId);
            socket.getOutputStream().write(ClientServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameServerMessage sendRequestToGameServer(int serverConnectionId, GameServerMessage message) {
        Socket socket = gameServersToSend.get(serverConnectionId);
        return sendRequestToGameServer(socket, message);
    }

    public GameServerMessage sendRequestToGameServer(Socket socket, GameServerMessage message) {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            System.out.println("Sending message for rooms");
            socket.getOutputStream().write(GameServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();

            GameServerMessage ans = GameServerMessageUtils.readMessage(socket.getInputStream());
            System.out.println(GameServerMessageUtils.toString(ans));
            return ans;
        } catch (IOException e) {
            gameServersToSend.remove(socket);
            throw new ServerException(e.getMessage());
        }
    }

    public List<GameServerMessage> sendBroadcastRequestToGameServer(GameServerMessage message) {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        List<GameServerMessage> answers = new ArrayList<>();
        for (Socket gameServer : gameServersToSend) {
            GameServerMessage messages = sendRequestToGameServer(gameServer, message);
            answers.add(messages);
        }
        return answers;
    }

}
