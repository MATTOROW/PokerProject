package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.model.Card;
import ru.itis.pokerproject.shared.model.Suit;
import ru.itis.pokerproject.shared.model.Value;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameEndEventListener implements GameEventListener {

    @Override
    public void handle(GameServerMessage message) {

        String data = new String(message.getData());

        String[] lines = data.split("\n");
        List<String> winnersUsernames = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split(";");
            if (parts.length < 4) {
                continue; // Пропускаем неверно сформированные строки.
            }
            String username = parts[0].trim();
            String winIndicator = parts[1].trim();
            String firstCardStr = parts[2].trim();
            String secondCardStr = parts[3].trim();


            Card firstCard = parseCard(firstCardStr);
            Card secondCard = parseCard(secondCardStr);


            PlayerInfo player;
            if (username.equals(Game.getMyPlayer().getUsername())) {
                player = Game.getMyPlayer();
            } else {
                player = Game.getPlayerByUsername(username);
            }
            if (player != null) {
                List<Card> hand = new ArrayList<>();
                hand.add(firstCard);
                hand.add(secondCard);
                player.setHand(hand);
            }


            if (winIndicator.equals("1")) {
                winnersUsernames.add(username);
            }
        }


        Platform.runLater(() -> {

            Game.getGameScreen().updateUI();

            long pot = Game.getPot();
            int winnersCount = winnersUsernames.size();
            long winAmount = winnersCount > 0 ? pot / winnersCount : 0;


            StringBuilder winnersDisplay = new StringBuilder();
            for (int i = 0; i < winnersUsernames.size(); i++) {
                String name = winnersUsernames.get(i);

                if (name.equals(Game.getMyPlayer().getUsername())) {
                    winnersDisplay.append("вы");
                } else {
                    winnersDisplay.append(name);
                }
                if (i < winnersUsernames.size() - 1) {
                    winnersDisplay.append(", ");
                }
            }

            String messageText = "Игроки " + winnersDisplay.toString() +
                    " победили.\nОни получили выигрыш в размере " + winAmount + ".";


            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Конец игры");
            alert.setHeaderText(null);
            alert.setContentText(messageText);


            alert.setOnHidden(e -> Game.getManager().showRoomsScreen());
            Optional<ButtonType> result = alert.showAndWait();


            Game.getManager().showLoginScreen();
        });
    }


    private Card parseCard(String cardStr) {
        String[] parts = cardStr.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Неверный формат карты: " + cardStr);
        }

        Suit suit = Suit.valueOf(parts[0].toUpperCase());
        Value value = Value.valueOf(parts[1].toUpperCase());
        return new Card(suit, value);
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.GAME_END;
    }
}

