package ru.itis.pokerproject.gameserver.server;

import ru.itis.pokerproject.gameserver.server.listener.ConnectToRoomEventListener;
import ru.itis.pokerproject.gameserver.server.listener.PlayerReadyEventListener;
import ru.itis.pokerproject.gameserver.service.*;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class SocketServer extends AbstractSocketServer<GameMessageType, GameServerMessage> {
    private final String clientServerHost;
    private final int clientServerPort;
    private final List<ServerEventListener<GameMessageType, GameServerMessage>> clientServerListeners;
    private final RoomManager manager;
    private final UUID id = UUID.randomUUID();
    private Socket clientServerToSend;
    private Socket clientServerToListen;

    public SocketServer(int port, String clientServerHost, int clientServerPort) {
        super(port);
        this.clientServerHost = clientServerHost;
        this.clientServerPort = clientServerPort;
        this.clientServerListeners = new ArrayList<>();
        this.manager = new RoomManager(this);

        GetRoomsInfoService.init(this);
        CreateRoomService.init(this);
        GetRoomsCountService.init(this);
        FindRoomService.init(this);
        ConnectToRoomService.init(this);
        PlayerReadyService.init(this);
        DisconnectFromRoomService.init(this);
    }

    private static String getLocalIPv4() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return "Error: not found.";
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                    return address.getHostAddress();
                }
            }
        }
        return "Not found";
    }

    protected void handleConnection(Socket socket) {
        new Thread(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                GameServerMessage message = readMessage(inputStream);
                ConnectToRoomEventListener listener = new ConnectToRoomEventListener(this);
                if (message.getType() == listener.getType()) {
                    GameServerMessage answer = listener.handle(socket, message);
                    sendMessage(socket, answer);
                    if (answer.getType() == GameMessageType.ERROR) {
                        socket.close();
                    } else {
                        GameServerMessage ready = readMessage(inputStream);
                        PlayerReadyEventListener readyListener = new PlayerReadyEventListener();
                        if (ready.getType() == readyListener.getType()) {
                            System.out.println("Реально ready!");
                            readyListener.handle(socket, ready);
                        }
                    }
                } else {
                    GameServerMessage error = GameServerMessageUtils.createMessage(
                            GameMessageType.ERROR,
                            "You are not allowed to receive data using this message type: %s."
                                    .formatted(message.getType()).getBytes()
                    );
                    sendMessage(socket, error);
                    socket.close();
                }
            } catch (EmptyMessageException | MessageReadingException e) {
                System.out.println("Lost connect");
                DisconnectFromRoomService.disconnectFromRoom(socket);
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (ExceedingLengthException | UnknownMessageTypeException | WrongStartBytesException e) {
                GameServerMessage errorMessage = GameServerMessageUtils.createMessage(
                        GameMessageType.ERROR,
                        "Error while connecting to server.".getBytes()
                );
                sendMessage(socket, errorMessage);
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (IOException | ServerEventListenerException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                System.err.println("Error handling connection: " + e.getMessage());
            }
        }).start();
    }

    protected void handleServerConnection() {
        new Thread(() -> {
            try (InputStream inputStream = clientServerToListen.getInputStream()) {
                int connectionId = -1;
                while (!clientServerToListen.isClosed()) {
                    boolean handled = false;
                    GameServerMessage message = readMessage(inputStream);

                    for (ServerEventListener<GameMessageType, GameServerMessage> listener : clientServerListeners) {
                        if (message.getType() == listener.getType()) {
                            handled = true;
                            GameServerMessage answer = listener.handle(clientServerToListen, message);
                            sendMessageToClientServer(answer);
                        }
                    }
                    if (!handled) {
                        GameServerMessage error = GameServerMessageUtils.createMessage(
                                GameMessageType.ERROR,
                                "You are not allowed to receive data using this message type: %s."
                                        .formatted(message.getType()).getBytes()
                        );
                        sendMessageToClientServer(error);
                    }
                }
            } catch (EmptyMessageException | MessageReadingException e) {
                System.out.println("Упал главный сервак");
                try {
                    clientServerToListen.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                clientServerToListen = null;
            } catch (ExceedingLengthException | UnknownMessageTypeException | WrongStartBytesException e) {
                GameServerMessage errorMessage = GameServerMessageUtils.createMessage(
                        GameMessageType.ERROR,
                        "Error while connecting to server.".getBytes()
                );
                sendMessageToClientServer(errorMessage);
            } catch (IOException | ServerEventListenerException e) {
                System.err.println("Error handling connection: " + e.getMessage());
            }
        }).start();
    }

    public void registerClientServerListener(ServerEventListener<GameMessageType, GameServerMessage> listener) throws ServerException {
        if (started) {
            throw new ServerException("Server has been started already.");
        }
        this.clientServerListeners.add(listener);
    }

    public void sendMessage(Socket socket, GameServerMessage message) {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            socket.getOutputStream().write(GameServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameServerMessage readMessage(InputStream in) {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        return GameServerMessageUtils.readMessage(in);
    }

    public GameServerMessage readMessage(Socket socket) {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            return readMessage(socket.getInputStream());
        } catch (IOException e) {
            throw new ServerException("Can't read a message.");
        }
    }

    public void connect() {
        try {
            clientServerToSend = new Socket(clientServerHost, clientServerPort);
            ClientServerMessage response = sendRequestToClientServer(
                    ClientServerMessageUtils.createMessage(ClientMessageType.REGISTER_GAME_SERVER_REQUEST,
                            "%s:%s".formatted(getLocalIPv4(), port).getBytes())
            );
            if (response.getType() != ClientMessageType.REGISTER_GAME_SERVER_RESPONSE) {
                throw new ServerException("Can't connect.");
            }
            this.clientServerToListen = server.accept();
        } catch (IOException ex) {
            throw new ServerException("Can't connect.", ex);
        }
    }

    public ClientServerMessage sendRequestToClientServer(ClientServerMessage message) {
        if (clientServerToSend == null || clientServerToSend.isClosed()) {
            throw new ServerException("Socket is not connected.");
        }
        try {
            clientServerToSend.getOutputStream().write(ClientServerMessageUtils.getBytes(message));
            clientServerToSend.getOutputStream().flush();
            return ClientServerMessageUtils.readMessage(clientServerToSend.getInputStream());
        } catch (IOException ex) {
            throw new ServerException("Can't send message.", ex);
        }
    }

    public void sendMessageToClientServer(GameServerMessage message) {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            Socket socket = clientServerToListen;
            socket.getOutputStream().write(GameServerMessageUtils.getBytes(message));
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() throws ServerException {
        try {
            server = new ServerSocket(this.port);
            started = true;
            connect();
            handleServerConnection();

            while (true) {
                Socket s = server.accept();
                System.out.println("Принял connect!");
                handleConnection(s);
            }
        } catch (IOException e) {
            throw new ServerException("Problem with server starting.", e);
        }
    }

    public RoomManager getRoomManager() {
        return this.manager;
    }

    public Set<String> getRoomsInfo() {
        return manager.getRooms().entrySet().stream()
                .map(entry -> "%s;%s".formatted(entry.getKey().toString(), entry.getValue().getRoomInfo()))
                .collect(Collectors.toSet());
    }

    public boolean findRoom(UUID code) {
        return manager.getRooms().containsKey(code);
    }
}
