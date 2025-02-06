package ru.itis.pokerproject.clientserver.service;

import ru.itis.pokerproject.clientserver.server.SocketServer;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.util.List;
import java.util.UUID;

public class ConnectToRoomService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static byte[] getServerAddress(UUID code) {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }
        GameServerMessage request = GameServerMessageUtils.createMessage(GameMessageType.FIND_ROOM_REQUEST, code.toString().getBytes());
        List<GameServerMessage> responses = server.sendBroadcastRequestToGameServer(request);

        if (responses.isEmpty()) {
            return null;
        }
        int index = -1;
        List<Byte> contains = responses.stream().map(r -> r.getData()[0]).toList();
        for (int i = 0; i < contains.size(); ++i) {
            if (contains.get(i) == 1) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return null;
        } else {
            return server.getServerAddr(index).getBytes();
        }
    }
}
