package ru.itis.pokerproject.network.listener;

import ru.itis.pokerproject.model.Game;
import ru.itis.pokerproject.model.PlayerInfo;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public class PlayerConnectedEventListener implements GameEventListener {
    @Override
    public void handle(GameServerMessage message) {
        String[] data = new String(message.getData()).split(";");
        PlayerInfo newPlayer = new PlayerInfo(data[0], Long.parseLong(data[1]), data[2].equals("1"));
        Game.addPlayer(newPlayer);
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.PLAYER_CONNECTED;
    }
}
