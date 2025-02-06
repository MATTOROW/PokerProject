package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerAllInnedEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String[] data = new String(message.getData()).split(";");
        String username = data[0];
        long bet = Long.parseLong(data[1]);

        PlayerInfo player = Game.getPlayerByUsername(username);
        if (player != null) {
            long allMoney = player.getDefaultMoney();
            long toSubtract = allMoney - player.getCurrentBet();
            System.out.println("ВСЕ ДЕНЬГИ ИГРОКА АЛЛ ИН: " + allMoney);
            System.out.println("НУЖНО ВЫЧЕСТЬ: " + toSubtract);
            player.subtractMoney(toSubtract);
            Game.setPot(Game.getPot() + toSubtract);

            if (bet != -1) {
                Game.setCurrentBet(bet);
            }

            Platform.runLater(() -> {
                Game.getGameScreen().updatePotAndBet();
                Game.getGameScreen().updateUI();
                Game.getGameScreen().showNotification(username + " пошел ALL IN (" + toSubtract + ")");
            });
        }
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_ALL_INNED;
    }
}

