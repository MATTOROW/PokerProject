package ru.itis.pokerproject.gameserver.service;

import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;

public class GetRoomsCountService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static byte[] getRoomsCount() {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }
        return String.valueOf(server.getRoomsInfo().size()).getBytes();
    }
}
