package ru.itis.pokerproject.gameserver.server.listener;

import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.gameserver.service.ConnectToRoomService;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.listener.ServerEventListenerException;

import java.net.Socket;
import java.util.UUID;

public class ConnectToRoomEventListener implements ServerEventListener<GameMessageType, GameServerMessage> {
    private final SocketServer socketServer;

    public ConnectToRoomEventListener(SocketServer socketServer) {
        this.socketServer = socketServer;
    }

    @Override
    public GameServerMessage handle(Socket socket, GameServerMessage message) throws ServerEventListenerException {
        String[] data = new String(message.getData()).split(";");
        String roomId = data[0];
        String token = data[1];
        byte[] answerData = ConnectToRoomService.connectToRoom(socket, UUID.fromString(roomId), token);
        GameServerMessage answer;
        if (answerData == null) {
            answer = GameServerMessageUtils.createMessage(GameMessageType.ERROR, "Either your token expired and you need to re login, or your money is 0, connection refused".getBytes());
        } else if (answerData.length == 0) {
            answer = GameServerMessageUtils.createMessage(GameMessageType.ERROR, "The room is full, you can't connect.".getBytes());
        } else {
            answer = GameServerMessageUtils.createMessage(GameMessageType.CONNECT_TO_ROOM_RESPONSE, answerData);
        }
        return answer;
    }

    @Override
    public GameMessageType getType() {
        return GameMessageType.CONNECT_TO_ROOM_REQUEST;
    }
}
