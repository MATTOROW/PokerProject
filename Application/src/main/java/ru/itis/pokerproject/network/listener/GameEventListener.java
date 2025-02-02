package ru.itis.pokerproject.network.listener;

import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;

public interface GameEventListener {
    void handle(GameServerMessage message);

    GameMessageType getType();
}
