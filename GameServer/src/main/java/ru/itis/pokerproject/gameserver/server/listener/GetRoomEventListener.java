package ru.itis.pokerproject.gameserver.server.listener;

import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

public class GetRoomEventListener implements ServerEventListener<GameMessageType, GameServerMessage> {
    @Override
    public GameServerMessage handle(int connectionId, GameServerMessage message) throws ServerEventListenerException {
        return null;
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.GET_ROOMS_REQUEST;
    }
}
