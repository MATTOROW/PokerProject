package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerCalledEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String[] data = new String(message.getData()).split(";");
        String username = data[0];
        long toSubtract = Long.parseLong(data[1]);

        PlayerInfo player = Game.getPlayerByUsername(username);
        if (player != null) {
            player.subtractMoney(toSubtract);
            Game.setPot(Game.getPot() + toSubtract);
            player.setCurrentBet(toSubtract);

            Platform.runLater(() -> {
                Game.getGameScreen().updatePotAndBet();
                Game.getGameScreen().updateUI();
                Game.getGameScreen().showNotification(username + " сделал CALL (+" + toSubtract + " в банк)");
            });
        }
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_CALLED;
    }
}

