package ru.itis.pokerproject.gameserver.server;

import ru.itis.pokerproject.gameserver.models.Room;
import ru.itis.pokerproject.gameserver.service.CreateRoomService;
import ru.itis.pokerproject.gameserver.service.GetRoomsCountService;
import ru.itis.pokerproject.gameserver.service.GetRoomsInfoService;
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
    private Socket clientServerToSend;
    private Socket clientServerToListen;
    private final String clientServerHost;
    private final int clientServerPort;
    private final List<ServerEventListener<GameMessageType, GameServerMessage>> clientServerListeners;
    private List<Room> rooms;

    private final UUID id = UUID.randomUUID();

    public SocketServer(int port, String clientServerHost, int clientServerPort) {
        super(port);
        this.clientServerHost = clientServerHost;
        this.clientServerPort = clientServerPort;
        clientServerListeners = new ArrayList<>();

        GetRoomsInfoService.init(this);
        CreateRoomService.init(this);
        GetRoomsCountService.init(this);

        // Проверка общения между серверами.
        rooms = new ArrayList<>();
//        rooms.add(new Room(5, 100));
//        rooms.add(new Room(2, 1000));
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
                            System.out.println("Я отдал данные о комнатах!");
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

    protected void handleServerConnection() {
        new Thread(() -> {
            int connectionId = sockets.lastIndexOf(clientServerToListen);
            try (InputStream inputStream = clientServerToListen.getInputStream()) {

                while (!clientServerToListen.isClosed()) {
                    GameServerMessage message = GameServerMessageUtils.readMessage(inputStream);

                    for (ServerEventListener<GameMessageType, GameServerMessage> listener : clientServerListeners) {
                        if (message.getType() == listener.getType()) {
                            GameServerMessage answer = listener.handle(connectionId, message);
                            sendMessageToClientServer(answer);
                            System.out.println("Я отдал данные о комнатах!");
                        }
                    }
                }
            } catch (EmptyMessageException | MessageReadingException e) {
                System.out.println("Упал главный сервак");
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

    @Override
    public void sendMessage(int connectionId, GameServerMessage message) throws ServerException {
        if (!started) {
            throw new ServerException("Server hasn't been started yet.");
        }
        try {
            Socket clientServer = sockets.get(connectionId);
            clientServer.getOutputStream().write(GameServerMessageUtils.getBytes(message));
            clientServer.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void sendBroadCastMessage(GameServerMessage message) throws ServerException {

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
                handleConnection(s);
            }
        } catch (IOException e) {
            throw new ServerException("Problem with server starting.", e);
        }
    }

    public Set<String> getRoomsInfo() {
        return rooms.stream().map(Room::getRoomInfo).collect(Collectors.toSet());
    }

    public UUID createRoom(int maxPlayers, long minBet) {
        Room newRoom = new Room(maxPlayers, minBet);
        rooms.add(newRoom);
        System.out.println(rooms.size());
        return newRoom.getCode();
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
}
