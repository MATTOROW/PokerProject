package ru.itis.pokerproject.shared.template.server;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;

public interface Server {
    void registerListener(ServerEventListener listener) throws ServerException;
    void sendMessage(int connectionId, ClientServerMessage message) throws ServerException;
    void sendBroadCastMessage(ClientServerMessage message) throws ServerException;
    void start() throws ServerException;
}
