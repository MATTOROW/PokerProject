package ru.itis.pokerproject.gameserver.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import ru.itis.pokerproject.gameserver.model.GameHandler;
import ru.itis.pokerproject.gameserver.model.Room;
import ru.itis.pokerproject.gameserver.model.game.Player;
import ru.itis.pokerproject.gameserver.server.RoomManager;
import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.shared.template.server.ServerException;

import java.net.Socket;
import java.util.Date;
import java.util.UUID;

public class ConnectToRoomService {
    private static SocketServer server = null;
    private static boolean init = false;

    public static void init(SocketServer socketServer) {
        server = socketServer;
        init = true;
    }

    public static byte[] connectToRoom(Socket socket, UUID code, String token) {
        if (!init) {
            throw new ServerException("Server is not initialized!");
        }
        Claims claims = Jwts.parser()
                .setSigningKey("EgorPomidorVishelIzZAGORPOSADILKARtoshkuVishelNaLukoshkoLALALALALALAL")
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();  // Получаем username
        Long money = claims.get("money", Long.class);  // Получаем money
        Date expiration = claims.getExpiration();  // Получаем дату истечения срока действия
        if (expiration.before(new Date(System.currentTimeMillis()))) {
            return null;
        }
        if (money == 0) {
            return null;
        }
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(code);
        GameHandler handler = roomManager.getGameHandler(code);

        if (room == null || handler == null) {
            return new byte[0]; // Комната не найдена
        }

        boolean added = handler.addPlayer(new Player(socket, username, money));
        if (!added) {
            return new byte[0]; // Комната заполнена
        }

        return room.getRoomAndPlayersInfo().getBytes();
    }
}
