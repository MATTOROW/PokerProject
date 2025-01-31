package ru.itis.pokerproject.clientserver.service;

import ru.itis.pokerproject.clientserver.server.SocketServer;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.util.List;

public class GetRoomsService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static byte[] getRooms() {
        if (!init) {
            throw new ServerException();
        }
        GameServerMessage request = GameServerMessageUtils.createMessage(GameMessageType.GET_ROOMS_REQUEST, new byte[0]);
        List<GameServerMessage> responses = server.sendBroadcastRequestToGameServer(request);
        System.out.println("Ответы игрового:" + responses.size());

        // Объединяем все данные о комнатах
        byte joinSymbol = '\n';
        int length = responses.stream().mapToInt(m -> m.getData().length).sum() + responses.size() - 1;

        byte[] result = new byte[length];
        int currentPos = 0;
        for (GameServerMessage message: responses) {
            System.arraycopy(message.getData(), 0, result, currentPos, message.getData().length);
            currentPos = message.getData().length;
            if (currentPos == length) {
                break;
            }
            result[currentPos] = joinSymbol;
            currentPos++;
        }

        return result;
    }
}
