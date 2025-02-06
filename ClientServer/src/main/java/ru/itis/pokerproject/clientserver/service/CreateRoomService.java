package ru.itis.pokerproject.clientserver.service;

import ru.itis.pokerproject.clientserver.server.SocketServer;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.util.List;

public class CreateRoomService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static byte[] createRoom(int maxPlayers, int minBet) {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }
        GameServerMessage request = GameServerMessageUtils.createMessage(GameMessageType.GET_ROOMS_COUNT_REQUEST, new byte[0]);
        List<GameServerMessage> responses = server.sendBroadcastRequestToGameServer(request);

        if (responses.isEmpty()) {
            return new byte[0];
        }

        long[] counts = responses.stream().mapToLong(resp -> Long.parseLong(new String(resp.getData()))).toArray();

        int index = 0;
        long minValue = counts[index];

        for (int i = 1; i < counts.length; ++i) {
            if (counts[i] < minValue) {
                minValue = counts[i];
                index = i;
            }
        }

        byte[] data = "%d;%d".formatted(maxPlayers, minBet).getBytes();
        GameServerMessage secondRequest = GameServerMessageUtils.createMessage(GameMessageType.CREATE_ROOM_REQUEST, data);
        GameServerMessage response = server.sendRequestToGameServer(index, secondRequest);
        return response.getData();
    }
}
