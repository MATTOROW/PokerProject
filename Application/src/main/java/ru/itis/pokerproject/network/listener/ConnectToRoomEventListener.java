package ru.itis.pokerproject.network.listener;

import javafx.application.Platform;
import ru.itis.pokerproject.application.GameScreen;
import ru.itis.pokerproject.application.SessionStorage;
import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

import java.util.ArrayList;
import java.util.List;

public class ConnectToRoomEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {

        String[] data = new String(message.getData()).split("\n");
        String[] roomInfo = data[0].split(";");
        int maxPlayers = Integer.parseInt(roomInfo[0]);
        int currentPlayers = Integer.parseInt(roomInfo[1]);
        long minBet = Long.parseLong(roomInfo[2]);
        PlayerInfo myPlayer = null;
        List<PlayerInfo> playerInfos = new ArrayList<>();
        for (int i = 1; i < data.length; ++i) {
            String[] playerInfo = data[i].split(";");
            PlayerInfo info = new PlayerInfo(playerInfo[0], Long.parseLong(playerInfo[1]), playerInfo[2].equals("1"));
            if (info.getUsername().equals(SessionStorage.getUsername())) {
                myPlayer = info;
            } else {
                playerInfos.add(info);
            }
        }
        Game.setMaxPlayers(maxPlayers);
        Game.setCurrentPlayers(currentPlayers);
        Game.setMinBet(minBet);
        Game.setPlayers(playerInfos);
        Game.setMyPlayer(myPlayer);
        GameScreen screen = new GameScreen(Game.getMaxPlayers(), Game.getCurrentPlayers(), Game.getMinBet(), Game.getPlayers(), Game.getMyPlayer(), Game.getManager());
        Game.setGameScreen(screen);
        Platform.runLater(() -> {
            screen.updateUI();
            Game.getManager().getPrimaryStage().getScene().setRoot(screen);
            Game.getManager().getPrimaryStage().sizeToScene();
        });
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.CONNECT_TO_ROOM_RESPONSE;
    }
}
