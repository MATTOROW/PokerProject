package ru.itis.pokerproject.gameserver.service;

import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.util.UUID;

public class FindRoomService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static byte[] findRoom(UUID code) {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }
        boolean found = server.findRoom(code);
        if (found) {
            return new byte[]{1};
        } else {
            return new byte[]{0};
        }
    }
}
