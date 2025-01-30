package ru.itis.pokerproject.gameserver.server;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientMessageType;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.server.Server;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

public abstract class SocketServer implements Server<GameMessageType, GameServerMessage> {
    private final UUID id = UUID.randomUUID();
    protected List<ServerEventListener<GameMessageType, GameServerMessage>> listeners;
    protected int port;
    protected ServerSocket server;
    protected boolean started;
    protected List<Socket> sockets;


    @Override
    public void registerListener(ServerEventListener<GameMessageType, GameServerMessage> listener) throws ServerException {
        if (started) {
            throw new ServerException("Server has been started already.");
        }
        this.listeners.add(listener);
    }

    @Override
    public void sendMessage(int connectionId, GameServerMessage message) throws ServerException {

    }

    @Override
    public void sendBroadCastMessage(GameServerMessage message) throws ServerException {

    }

    @Override
    public void start() throws ServerException {

    }
}
