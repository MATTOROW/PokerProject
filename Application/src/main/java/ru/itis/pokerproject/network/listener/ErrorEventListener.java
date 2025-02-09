package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class ErrorEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        Platform.runLater(() -> Game.getManager().showErrorScreen(new String(message.getData())));
        Platform.runLater(Game.getManager()::showRoomsScreen);
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.ERROR;
    }
}
