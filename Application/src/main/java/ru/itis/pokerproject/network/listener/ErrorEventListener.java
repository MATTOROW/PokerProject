package ru.itis.pokerproject.network.listener;

import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class ErrorEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        Game.getGameScreen().getManager().showErrorScreen(new String(message.getData()));
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.ERROR;
    }
}
