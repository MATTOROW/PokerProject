package ru.itis.pokerproject.clientserver.server.listeners;

import ru.itis.pokerproject.clientserver.server.ServerExample;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

public interface ServerEventListener {
    public void init(ServerExample server);
    public void handle(int connectionId, ClientServerMessage message) throws ServerEventListenerException;
    public ClientServerMessage.MessageType getType();
}
