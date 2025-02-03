package ru.itis.pokerproject.gameserver.model;


import ru.itis.pokerproject.gameserver.model.game.Player;
import ru.itis.pokerproject.gameserver.server.SocketServer;
import ru.itis.pokerproject.shared.protocol.gameserver.GameMessageType;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessage;
import ru.itis.pokerproject.shared.protocol.gameserver.GameServerMessageUtils;

public class GameHandler {
    private final Room room;
    private final SocketServer socketServer;

    public GameHandler(Room room, SocketServer socketServer) {
        this.room = room;
        this.socketServer = socketServer;
    }

    public synchronized boolean addPlayer(Player player) {
        boolean added = room.addPlayer(player);
        if (!added) return false;

        broadcastNewPlayer(player);
        return true;
    }

    private void broadcastNewPlayer(Player player) {
        GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.PLAYER_CONNECTED, player.getInfo().getBytes());
        for (Player p : room.getPlayers()) {
            if (!p.getUsername().equals(player.getUsername())) {
                socketServer.sendMessage(p.getSocket(), message);
            }
        }
    }

    public void setPlayerReady(String username) {
        room.getPlayer(username).setReady(true);
        broadcastPlayerReady(username);

        if (room.allPlayersReady()) {
            startGame();
        }
    }

    private void broadcastPlayerReady(String username) {
        GameServerMessage message = GameServerMessageUtils.createMessage(GameMessageType.PLAYER_IS_READY, username.getBytes());
        for (Player p : room.getPlayers()) {
            if (!p.getUsername().equals(username)) {
                socketServer.sendMessage(p.getSocket(), message);
            }
        }
    }

    private void startGame() {
        GameServerMessage startMessage = GameServerMessageUtils.createMessage(GameMessageType.START_GAME, new byte[0]);
        for (Player p : room.getPlayers()) {
            socketServer.sendMessage(p.getSocket(), startMessage);
        }
    }
}
