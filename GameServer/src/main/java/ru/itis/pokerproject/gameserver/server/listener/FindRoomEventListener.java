package ru.itis.pokerproject.gameserver.server.listener;

import ru.itis.pokerproject.gameserver.service.FindRoomService;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;
import java.util.UUID;

public class FindRoomEventListener implements ServerEventListener<GameMessageType, GameServerMessage> {
    @Override
    public GameServerMessage handle(Socket socket, GameServerMessage message) throws ServerEventListenerException {
        UUID code = UUID.fromString(new String(message.getData()));
        return GameServerMessageUtils.createMessage(GameMessageType.FIND_ROOM_RESPONSE, FindRoomService.findRoom(code));
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.FIND_ROOM_REQUEST;
    }
}
