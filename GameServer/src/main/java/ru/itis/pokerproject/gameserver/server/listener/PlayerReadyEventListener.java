package ru.itis.pokerproject.gameserver.server.listener;

import ru.itis.pokerproject.gameserver.service.PlayerReadyService;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;

public class PlayerReadyEventListener {
    public void handle(Socket socket, GameServerMessage message) throws ServerEventListenerException {
        PlayerReadyService.setReady(socket);
    }

    public GameMessageType getType() {
        return GameMessageType.READY;
    }
}
