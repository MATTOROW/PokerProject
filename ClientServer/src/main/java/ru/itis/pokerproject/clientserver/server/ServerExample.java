package ru.itis.pokerproject.clientserver.server;

import ru.itis.pokerproject.clientserver.server.listeners.ServerEventListener;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

public interface ServerExample {
    public void registerListener(ServerEventListener listener) throws ServerException;
    public void sendMessage(int connectionId, ClientServerMessage message) throws ServerException;
    public void sendBroadCastMessage(ClientServerMessage message) throws ServerException;
    public void start() throws ServerException;
}
