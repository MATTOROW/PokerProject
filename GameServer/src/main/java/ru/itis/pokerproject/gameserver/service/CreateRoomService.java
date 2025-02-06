package ru.itis.pokerproject.gameserver.service;

import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.util.UUID;

public class CreateRoomService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static byte[] createRoom(int maxPlayers, long minBet) {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }
        UUID code = server.getRoomManager().createRoom(maxPlayers, minBet);
        return code.toString().getBytes();
    }
}
