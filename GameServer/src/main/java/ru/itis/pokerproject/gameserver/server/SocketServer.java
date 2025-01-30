package ru.itis.pokerproject.gameserver.server;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.server.Server;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.util.UUID;

public class SocketServer implements Server {
    private final UUID id = UUID.randomUUID();


    @Override
    public void registerListener(ServerEventListener listener) throws ServerException {

    }

    @Override
    public void sendMessage(int connectionId, ClientServerMessage message) throws ServerException {

    }

    @Override
    public void sendBroadCastMessage(ClientServerMessage message) throws ServerException {

    }

    @Override
    public void start() throws ServerException {

    }
}
