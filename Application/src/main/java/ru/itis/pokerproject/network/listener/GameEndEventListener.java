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
        // Преобразуем входящие данные в строку.
        String data = new String(message.getData());
        // Каждая строка имеет формат: username;1/0;firstCard;secondCard
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

            // Парсим карты
            Card firstCard = parseCard(firstCardStr);
            Card secondCard = parseCard(secondCardStr);

            // Находим игрока по username.
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

            // Если winIndicator равен "1", добавляем имя игрока в список победителей.
            if (winIndicator.equals("1")) {
                winnersUsernames.add(username);
            }
        }

        // Выполняем обновление UI и показываем модальное окно в UI-потоке.
        Platform.runLater(() -> {
            // Обновляем отображение карт на игровом экране.
            Game.getGameScreen().updateUI();

            long pot = Game.getPot();
            int winnersCount = winnersUsernames.size();
            long winAmount = winnersCount > 0 ? pot / winnersCount : 0;

            // Формируем строку с именами победителей.
            StringBuilder winnersDisplay = new StringBuilder();
            for (int i = 0; i < winnersUsernames.size(); i++) {
                String name = winnersUsernames.get(i);
                // Если это ваш игрок, выводим "вы" вместо имени.
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

            // Создаем модальное окно.
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Конец игры");
            alert.setHeaderText(null);
            alert.setContentText(messageText);

            // При закрытии окна (как по кнопке, так и другими способами) переходим на экран с комнатами.
            alert.setOnHidden(e -> Game.getManager().showRoomsScreen());
            Optional<ButtonType> result = alert.showAndWait();

            // Если окно закрыто (например, нажатием на крестик), то тоже переходим на экран с комнатами.
            Game.getManager().showLoginScreen();
        });
    }

    /**
     * Вспомогательный метод для парсинга строки, описывающей карту.
     * Ожидается формат: "SUIT VALUE" (например, "HEARTS ACE").
     */
    private Card parseCard(String cardStr) {
        String[] parts = cardStr.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Неверный формат карты: " + cardStr);
        }
        // Приводим к верхнему регистру для сопоставления с константами перечислений.
        Suit suit = Suit.valueOf(parts[0].toUpperCase());
        Value value = Value.valueOf(parts[1].toUpperCase());
        return new Card(suit, value);
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.GAME_END;
    }
}

