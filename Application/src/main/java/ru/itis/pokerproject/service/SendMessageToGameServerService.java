package ru.itis.pokerproject.service;

import ru.itis.pokerproject.network.SocketClient;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.template.client.ClientException;

public class SendMessageToGameServerService {
    private final SocketClient client;

    public SendMessageToGameServerService(SocketClient client) {
        this.client = client;
    }

    public void sendMessage(GameServerMessage message) throws ClientException {
        client.sendMessageToGameServer(message);
    }
}
