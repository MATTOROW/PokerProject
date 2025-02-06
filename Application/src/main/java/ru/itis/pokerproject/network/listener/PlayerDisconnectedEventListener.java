package ru.itis.pokerproject.network.listener;

import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerDisconnectedEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String data = new String(message.getData());
        Game.removePlayer(data);
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_DISCONNECTED;
    }
}
