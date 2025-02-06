package ru.itis.pokerproject.gameserver.service;

import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;


public class GetRoomsInfoService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static byte[] getRooms() {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }
        String info = String.join("\n", server.getRoomsInfo());
        return info.getBytes();
    }
}
