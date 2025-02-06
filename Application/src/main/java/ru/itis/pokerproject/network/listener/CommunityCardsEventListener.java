package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.model.Card;
import ru.itis.pokerproject.shared.model.Suit;
import ru.itis.pokerproject.shared.model.Value;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommunityCardsEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String[] cardsData = new String(message.getData()).split(";");
        List<Card> cards = Arrays.stream(cardsData)
                .map(cardStr -> {
                    String[] parts = cardStr.split(" ");
                    return new Card(Suit.valueOf(parts[0]), Value.valueOf(parts[1]));
                })
                .collect(Collectors.toList());
        Game.getCommunityCards().addAll(cards);
        List<PlayerInfo> players = Game.getPlayers();
        for (PlayerInfo player : players) {
            System.out.println("Текущая ставка у игрока (из communityCardsListener)" + player.getCurrentBet());
        }
        Game.setCurrentBet(0);
        System.out.println("Текущая ставка в игре (из communityCardsListener): " + Game.getCurrentBet());
        Platform.runLater(() -> {
            Game.getGameScreen().updatePotAndBet();
            Game.getGameScreen().updateCommunityCards();
        });
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.COMMUNITY_CARDS;
    }
}
