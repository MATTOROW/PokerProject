package ru.itis.pokerproject.gameserver.server.listener;

import ru.itis.pokerproject.gameserver.service.GetRoomsCountService;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;

public class GetRoomsCountEventListener implements ServerEventListener<GameMessageType, GameServerMessage> {
    @Override
    public GameServerMessage handle(Socket socket, GameServerMessage message) throws ServerEventListenerException {
        byte[] data = GetRoomsCountService.getRoomsCount();
        GameServerMessage answer = GameServerMessageUtils.createMessage(GameMessageType.GET_ROOMS_RESPONSE, data);
        return answer;
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.GET_ROOMS_COUNT_REQUEST;
    }
}
