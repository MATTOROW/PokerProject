package ru.itis.pokerproject.gameserver.models;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {
    private final UUID code;
    private final int maxPlayers;
    private final List<Socket> players = new ArrayList<>();
    private final long minBet;

    public Room(int maxPlayers, long minBet) {
        this.code = UUID.randomUUID();
        this.maxPlayers = maxPlayers;
        this.minBet = minBet;
    }

    public UUID getCode() {
        return this.code;
    }

    public void addPlayer(Socket player) {
        players.add(player);
    }

    public void removePlayer(Socket player) {
        players.remove(player);
    }

    public int currentPlayersCount() {
        return players.size();
    }

    public int maxPlayersCount() {
        return maxPlayers;
    }

    public String getRoomInfo() {
        return "%s;%s;%s;%s".formatted(code.toString(), maxPlayers, currentPlayersCount(), minBet);
    }

    public void startGame() {

    }
}
