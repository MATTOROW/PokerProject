package ru.itis.pokerproject.clientserver.server;

import ru.itis.pokerproject.clientserver.service.ConnectToRoomService;
import ru.itis.pokerproject.clientserver.service.CreateRoomService;
import ru.itis.pokerproject.clientserver.service.GetRoomsService;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessageUtils;
import ru.itis.pokerproject.shared.protocol.exception.*;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;
import ru.itis.pokerproject.shared.template.server.AbstractSocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;
import ru.itis.pokerproject.shared.template.server.SocketArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer extends AbstractSocketServer<ClientMessageType, ClientServerMessage> {
    protected SocketArrayList gameServersToListen;
    protected SocketArrayList gameServersToSend;
    protected List<ServerEventListener<ClientMessageType, ClientServerMessage>> gameServerListeners;

    public SocketServer(int port) {
        super(port);
        gameServersToListen = new SocketArrayList();
        gameServersToSend = new SocketArrayList();
        gameServerListeners = new ArrayList<>();
        GetRoomsService.init(this);
        CreateRoomService.init(this);
        ConnectToRoomService.init(this);
    }

    public void registerGameServerListener(ServerEventListener<ClientMessageType, ClientServerMessage> listener) throws ServerException {
        if (started) {
            throw new ServerException("Server has been started already.");
        }
        this.gameServerListeners.add(listener);
    }

    protected void handleConnection(Socket socket) {
        new Thread(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                while (!socket.isClosed()) {
                    boolean handled = false;
                    ClientServerMessage message = ClientServerMessageUtils.readMessage(inputStream);

                    if (message.getType() == ClientMessageType.REGISTER_GAME_SERVER_REQUEST) {
                        gameServersToListen.add(socket);
                        String[] connectionData = new String(message.getData()).split(":");
                        gameServersToSend.add(new Socket(connectionData[0], Integer.parseInt(connectionData[1])));
                        int connectionId = gameServersToListen.lastIndexOf(socket);
                        sendMessageToGameServer(connectionId, ClientServerMessageUtils.createMessage(ClientMessageType.REGISTER_GAME_SERVER_RESPONSE, new byte[0]));
                        handleServerConnection(socket);
                        break;
                    } else {
                        for (ServerEventListener<ClientMessageType, ClientServerMessage> listener : listeners) {
                            if (message.getType() == listener.getType()) {
                                System.out.println("Нашелся нужный!" + message.getType());
                                handled = true;
                                ClientServerMessage answer = listener.handle(socket, message);
                                sendMessage(socket, answer);
                            }
                        }
                        if (!handled) {
                            ClientServerMessage error = ClientServerMessageUtils.createMessage(
                                    ClientMessageType.ERROR,
                                    "You are not allowed to recieve data using this message type: %s."
                                            .formatted(message.getType()).getBytes()
                            );
                            sendMessage(socket, error);
                        }
                    }
                }
            } catch (EmptyMessageException | MessageReadingException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (ExceedingLengthException | UnknownMessageTypeException | WrongStartBytesException e) {
                ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                        ClientMessageType.ERROR,
                        "Error while connecting to server.".getBytes()
                );
                System.out.println(e.getMessage());
                sendMessage(socket, errorMessage);
            } catch (IOException | ServerEventListenerException e) {
                System.err.println("Error handling connection: " + e.getMessage());
            }
        }).start();
    }

    protected void handleServerConnection(Socket socket) {
        new Thread(() -> {
            int connectionId = gameServersToListen.lastIndexOf(socket);
            try (InputStream inputStream = socket.getInputStream()) {
                while (!socket.isClosed() && gameServersToListen.contains(socket)) {
                    boolean handled = false;
                    ClientServerMessage message = ClientServerMessageUtils.readMessage(inputStream);
                    for (ServerEventListener<ClientMessageType, ClientServerMessage> listener : gameServerListeners) {
                        if (message.getType() == listener.getType()) {
                            System.out.println("Нашелся нужный у сервера! " + message.getType() + "Его данные: " + new String(message.getData()));
                            handled = true;
                            ClientServerMessage answer = listener.handle(socket, message);
                            sendMessageToGameServer(connectionId, answer);
                        }
                    }
                    if (!handled) {
                        ClientServerMessage error = ClientServerMessageUtils.createMessage(
                                ClientMessageType.ERROR,
                                "You are not allowed to recieve data using this message type: %s."
                                        .formatted(message.getType()).getBytes()
                        );
                        sendMessageToGameServer(connectionId, error);
                    }
                }
            } catch (EmptyMessageException | MessageReadingException e) {
                gameServersToListen.remove(connectionId);
                gameServersToSend.remove(connectionId);
            } catch (ExceedingLengthException | UnknownMessageTypeException | WrongStartBytesException e) {
                ClientServerMessage errorMessage = ClientServerMessageUtils.createMessage(
                        ClientMessageType.ERROR,
                        "Error while connecting to server.".getBytes()
                );
                sendMessageToGameServer(connectionId, errorMessage);
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
    public void sendMessage(Socket socket, ClientServerMessage message) throws ServerException {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            socket.getOutputStream().write(ClientServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
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
        try {
            return sendRequestToGameServer(socket, message);
        } catch (ServerException e) {
            gameServersToSend.remove(serverConnectionId);
            gameServersToListen.remove(serverConnectionId);
            return GameServerMessageUtils.createMessage(GameMessageType.ERROR, e.getMessage().getBytes());
        }
    }

    protected GameServerMessage sendRequestToGameServer(Socket socket, GameServerMessage message) throws ServerException {
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
            throw new ServerException(e.getMessage());
        }
    }

    public List<GameServerMessage> sendBroadcastRequestToGameServer(GameServerMessage message) {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        List<GameServerMessage> answers = new ArrayList<>();
        for (int i = 0; i < gameServersToSend.size(); ++i) {
            GameServerMessage messages = sendRequestToGameServer(i, message);
            answers.add(messages);
        }
        return answers;
    }

    public String getServerAddr(int serverId) {
        return "%s:%s".formatted(gameServersToSend.get(serverId).getInetAddress().getHostAddress(),
                gameServersToSend.get(serverId).getPort()
        );
    }
}
