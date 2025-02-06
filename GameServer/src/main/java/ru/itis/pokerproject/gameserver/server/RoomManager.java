package ru.itis.pokerproject.gameserver.server;

import ru.itis.pokerproject.gameserver.model.GameHandler;
import ru.itis.pokerproject.gameserver.model.Room;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoomManager {
    private final Map<UUID, Room> rooms = new HashMap<>();
    private final Map<UUID, GameHandler> gameHandlers = new HashMap<>();
    private final SocketServer socketServer;

    public RoomManager(SocketServer socketServer) {
        this.socketServer = socketServer;
    }

    public UUID createRoom(int maxPlayers, long minBet) {
        Room newRoom = new Room(maxPlayers, minBet, this);
        UUID roomId = UUID.randomUUID();
        rooms.put(roomId, newRoom);
        gameHandlers.put(roomId, new GameHandler(newRoom, socketServer));
        return roomId;
    }

    public Room getRoom(UUID roomId) {
        return rooms.get(roomId);
    }

    public void removeRoom(Room room) {
        for (Map.Entry<UUID, Room> entry : rooms.entrySet()) {
            if (room.equals(entry.getValue())) {
                UUID roomId = entry.getKey();
                rooms.remove(roomId);
                gameHandlers.remove(roomId);
                break;
            }
        }
    }

    public UUID getRoomIdBySocket(Socket socket) {
        return rooms.entrySet().stream().filter(s -> s.getValue().containsPlayer(socket)).findFirst().get().getKey();
    }

    public GameHandler getGameHandler(UUID roomId) {
        return gameHandlers.get(roomId);
    }

    public Map<UUID, Room> getRooms() {
        return rooms;
    }
}