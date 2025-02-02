package ru.itis.pokerproject.shared.template.client;

import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public interface GameClient {
    void connectToGameServer(String address, int port) throws ClientException;

    GameServerMessage sendMessageToGameServer(GameServerMessage message) throws ClientException;
}
