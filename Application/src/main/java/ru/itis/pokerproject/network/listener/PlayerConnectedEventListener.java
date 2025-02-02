package ru.itis.pokerproject.network.listener;


import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerConnectedEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {

    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_CONNECTED;
    }
}
