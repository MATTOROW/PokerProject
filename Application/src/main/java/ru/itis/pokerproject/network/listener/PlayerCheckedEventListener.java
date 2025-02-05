package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerCheckedEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String username = new String(message.getData());
        Platform.runLater(() -> {
            Game.getGameScreen().showNotification(username + " сделал CHECK");
        });
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_CHECKED;
    }
}

