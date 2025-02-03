package ru.itis.pokerproject.gameserver.model;

import ru.itis.pokerproject.gameserver.model.game.Player;

import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private final int maxPlayers;
    private final List<Player> players = new CopyOnWriteArrayList<>();
    private final long minBet;

    public Room(int maxPlayers, long minBet) {
        this.maxPlayers = maxPlayers;
        this.minBet = minBet;
    }

    public boolean addPlayer(Player player) {
        if (players.size() == maxPlayers) {
            return false;
        }
        players.add(player);
        return true;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public Player getPlayer(String username) {
        return players.stream().filter(p -> p.getUsername().equals(username)).findFirst().get();
    }

    public Player getPlayer(Socket socket) {
        return players.stream().filter(p -> p.getSocket().equals(socket)).findFirst().get();
    }

    public boolean containsPlayer(Socket socket) {
        return players.stream().filter(p -> p.getSocket().equals(socket)).toList().size() == 1;
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
        for (var player: players) {
            answer.append(player.getInfo());
            answer.append("\n");
        }
        return answer.toString();
    }
}
