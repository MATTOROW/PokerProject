package ru.itis.pokerproject.gameserver.server.listener;

import ru.itis.pokerproject.gameserver.service.PlayerReadyService;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

public class PlayerReadyEventListener {
    public void handle(int connectionId, GameServerMessage message) throws ServerEventListenerException {
        PlayerReadyService.setReady(connectionId);
    }

    public GameMessageType getType() {
        return GameMessageType.READY;
    }
}
