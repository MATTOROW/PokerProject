package ru.itis.pokerproject.gameserver.service;

import ru.itis.pokerproject.gameserver.model.GameHandler;
import ru.itis.pokerproject.gameserver.server.RoomManager;
import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.net.Socket;
import java.util.UUID;

public class DisconnectFromRoomService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static void disconnectFromRoom(Socket socket) {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }

        RoomManager roomManager = server.getRoomManager();
        UUID roomId = roomManager.getRoomIdBySocket(socket);
        GameHandler handler = roomManager.getGameHandler(roomId);

        if (handler != null) {
            handler.removePlayer(socket);
        }
    }
}
