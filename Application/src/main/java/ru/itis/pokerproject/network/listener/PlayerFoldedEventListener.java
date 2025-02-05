package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerFoldedEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String username = new String(message.getData());
        PlayerInfo player = Game.getPlayerByUsername(username);

        if (player != null) {
            Platform.runLater(() -> {
                Game.getGameScreen().updateUI();
                Game.getGameScreen().showNotification(username + " сбросил карты (FOLD)");
            });
        }
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_FOLDED;
    }
}
