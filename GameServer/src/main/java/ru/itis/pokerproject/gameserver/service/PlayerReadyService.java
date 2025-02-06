package ru.itis.pokerproject.gameserver.service;

import ru.itis.pokerproject.gameserver.model.GameHandler;
import ru.itis.pokerproject.gameserver.model.game.Player;
import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.net.Socket;
import java.util.UUID;

public class PlayerReadyService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static void setReady(Socket socket) {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }
        UUID roomId = server.getRoomManager().getRoomIdBySocket(socket);
        Player player = server.getRoomManager().getRoom(roomId).getPlayer(socket);
        GameHandler gameHandler = server.getRoomManager().getGameHandler(roomId);
        gameHandler.setPlayerReady(player.getUsername());
    }
}
