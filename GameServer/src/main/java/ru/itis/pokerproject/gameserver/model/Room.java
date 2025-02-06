package ru.itis.pokerproject.gameserver.model;

import ru.itis.pokerproject.gameserver.model.game.Player;
import ru.itis.pokerproject.gameserver.server.RoomManager;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private final int maxPlayers;
    private final List<Player> players = new CopyOnWriteArrayList<>();
    private final long minBet;
    private final RoomManager manager;

    public Room(int maxPlayers, long minBet, RoomManager manager) {
        this.maxPlayers = maxPlayers;
        this.minBet = minBet;
        this.manager = manager;
    }

    public boolean addPlayer(Player player) {
        if (players.size() == maxPlayers) {
            return false;
        }
        players.add(player);
        return true;
    }

    public void removePlayer(Player player) {
        try {
            player.getSocket().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public Player getPlayer(String username) {
        return players.stream().filter(p -> p.getUsername().equals(username)).findFirst().orElse(null);
    }

    public Player getPlayer(int index) {
        return players.get(index);
    }

    public Player getPlayer(Socket socket) {
        return players.stream().filter(p -> p.getSocket().equals(socket)).findFirst().orElse(null);
    }

    public boolean containsPlayer(Socket socket) {
        return players.stream().filter(p -> p.getSocket().equals(socket)).toList().size() == 1;
    }

    public long getMinBet() {
        return minBet;
    }

    public boolean allPlayersReady() {
        return maxPlayers == currentPlayersCount() && players.stream().filter(Player::isReady).toList().size() == currentPlayersCount();
    }

    public int currentPlayersCount() {
        return players.size();
    }

    public int maxPlayersCount() {
        return maxPlayers;
    }

    public String getRoomInfo() {
        return "%s;%s;%s".formatted(maxPlayers, currentPlayersCount(), minBet);
    }

    public String getRoomAndPlayersInfo() {
        StringBuilder answer = new StringBuilder();
        answer.append(getRoomInfo());
        answer.append("\n");
        for (var player : players) {
            answer.append(player.getInfo());
            answer.append("\n");
        }
        return answer.toString();
    }

    public RoomManager getManager() {
        return manager;
    }
}
