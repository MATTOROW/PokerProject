package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import ru.itis.pokerproject.application.GameScreen;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class WaitForActionEventListener implements GameEventListener {

    public void handle(GameServerMessage message) {
        Platform.runLater(() -> {
            GameScreen gameScreen = Game.getGameScreen();
            PlayerInfo player = Game.getMyPlayer();

            long currentBet = Game.getCurrentBet(); // Текущая ставка на столе
            long playerMoney = player.getMoney();   // Деньги игрока
            long playerBet = player.getCurrentBet(); // Сколько уже поставил игрок

            long toCall = currentBet - playerBet; // Сколько нужно добавить для уравнивания


            Platform.runLater(gameScreen::restoreActionButtons);


            gameScreen.getFoldButton().setDisable(false);
            gameScreen.getAllInButton().setDisable(false);


            System.out.println("Ставка игрока: " + playerBet);
            System.out.println("Ставка в игре: " + currentBet);
            gameScreen.getCheckButton().setDisable(currentBet != playerBet);


            gameScreen.getCallButton().setDisable(playerMoney <= toCall || currentBet == 0);


            TextField raiseInput = gameScreen.getRaiseAmountField();
            raiseInput.setDisable(playerMoney <= toCall); // Поле недоступно, если Raise невозможен
            raiseInput.setText(""); // Очищаем предыдущее значение


            if (playerMoney > toCall) {
                long minRaise = currentBet + 1;
                long maxRaise = playerBet + playerMoney;

                raiseInput.setPromptText("От " + minRaise + " до " + maxRaise);
                raiseInput.textProperty().addListener((obs, oldValue, newValue) -> {
                    try {
                        long value = Long.parseLong(newValue);
                        if (value < minRaise || value > maxRaise) {
                            raiseInput.setStyle("-fx-border-color: red;");
                        } else {
                            raiseInput.setStyle("");
                        }
                    } catch (NumberFormatException e) {
                        raiseInput.setStyle("-fx-border-color: red;");
                    }
                });
            }
        });
    }


    @Override
    public GameMessageType getType() {
        return GameMessageType.WAITING_FOR_ACTION;
    }
}
