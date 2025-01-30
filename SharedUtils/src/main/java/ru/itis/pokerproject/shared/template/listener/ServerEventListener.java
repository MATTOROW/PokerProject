package ru.itis.pokerproject.shared.template.listener;

import ru.itis.pokerproject.shared.template.server.Server;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

public interface ServerEventListener {
    public void init(Server server);
    public void handle(int connectionId, ClientServerMessage message) throws ServerEventListenerException;
    public ClientServerMessage.MessageType getType();
}
