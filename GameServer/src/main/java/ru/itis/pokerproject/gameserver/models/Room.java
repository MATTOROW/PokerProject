package ru.itis.pokerproject.gameserver.models;

import ru.itis.pokerproject.gameserver.models.game.Player;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private final int maxPlayers;
    private final Map<Player, Boolean> players = new ConcurrentHashMap<>();
    private final long minBet;

    public Room(int maxPlayers, long minBet) {
        this.maxPlayers = maxPlayers;
        this.minBet = minBet;
    }

    public boolean addPlayer(Player player) {
        if (players.size() == maxPlayers) {
            return false;
        }
        players.put(player, false);
        return true;
    }

    public void removePlayer(Player player) {
        players.remove(player);
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
        for (var player: players.entrySet()) {
            answer.append(player.getKey().getInfo());
            answer.append(";");
            answer.append(player.getValue() ? 1 : 0);
            answer.append("\n");
        }
        return answer.toString();
    }

    public void startGame() {

    }
}
