package ru.itis.pokerproject.gameserver.server.listener;

import ru.itis.pokerproject.gameserver.service.CreateRoomService;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CreateRoomEventListener implements ServerEventListener<GameMessageType, GameServerMessage> {

    @Override
    public GameServerMessage handle(Socket socket, GameServerMessage message) throws ServerEventListenerException {

        String data = new String(message.getData(), StandardCharsets.UTF_8);
        String[] parts = data.split(";");
        if (parts.length != 2) {
            GameServerMessage errorMessage = GameServerMessageUtils.createMessage(
                    GameMessageType.ERROR,
                    "Invalid message format. Expected maxPlayers;minBet".getBytes()
            );
            return errorMessage;
        }
        int maxPlayers;
        long minBet;
        try {
            maxPlayers = Integer.parseInt(parts[0]);
            minBet = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            GameServerMessage errorMessage = GameServerMessageUtils.createMessage(
                    GameMessageType.ERROR,
                    "Invalid message format. Expected maxPlayers;minBet".getBytes()
            );
            return errorMessage;
        }

        byte[] code = CreateRoomService.createRoom(maxPlayers, minBet);

        return GameServerMessageUtils.createMessage(GameMessageType.CREATE_ROOM_RESPONSE, code);
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.CREATE_ROOM_REQUEST;
    }
}
