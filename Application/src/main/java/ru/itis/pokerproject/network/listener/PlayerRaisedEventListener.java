package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerRaisedEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String[] data = new String(message.getData()).split(";");
        String username = data[0];
        long bet = Long.parseLong(data[1]);
        long toSubtract = Long.parseLong(data[2]);

        PlayerInfo player = Game.getPlayerByUsername(username);
        if (player != null) {
            player.subtractMoney(toSubtract);
            Game.setPot(Game.getPot() + toSubtract);
            Game.setCurrentBet(bet);
            player.setCurrentBet(toSubtract);

            Platform.runLater(() -> {
                Game.getGameScreen().updatePotAndBet();
                Game.getGameScreen().updateUI();
                Game.getGameScreen().showNotification(username + " повысил ставку до " + bet + " (RAISE)");
            });
        }
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_RAISED;
    }
}

