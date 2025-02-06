package ru.itis.pokerproject.service;

import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.network.SocketClient;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class SendReadyStatusService {
    private final SocketClient client;

    public SendReadyStatusService(SocketClient client) {
        this.client = client;
    }

    public void sendStatus() throws ClientException {
        client.sendMessageToGameServer(GameServerMessageUtils.createMessage(GameMessageType.READY, new byte[0]));
        Game.updatePlayerStatus(Game.getMyPlayer().getUsername(), true);
    }
}
