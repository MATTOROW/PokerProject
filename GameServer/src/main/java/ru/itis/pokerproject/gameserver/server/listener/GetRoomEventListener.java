package ru.itis.pokerproject.gameserver.server.listener;

import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;
import ru.itis.pokerproject.shared.template.listener.AbstractServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import static ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage.MessageType.GET_ROOMS_REQUEST;

public class GetRoomEventListener extends AbstractServerEventListener {
    @Override
    public void handle(int connectionId, ClientServerMessage message) throws ServerEventListenerException {
        
    }

    @Override
    public ClientServerMessage.MessageType getType() {
        return GET_ROOMS_REQUEST;
    }
}
