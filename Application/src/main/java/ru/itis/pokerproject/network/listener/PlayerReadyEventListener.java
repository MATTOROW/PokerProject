package ru.itis.pokerproject.network.listener;


import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerReadyEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String data = new String(message.getData());
        Game.updatePlayerStatus(data, true);
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_IS_READY;
    }
}
