package ru.itis.pokerproject.gameserver.models;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {
    private UUID code;
    private String name;
    private int maxPlayers;
    private final List<Socket> players = new ArrayList<>();

    public Room(String name, int maxPlayers) {
        this.code = UUID.randomUUID();
        this.name = name;
        this.maxPlayers = maxPlayers;
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
        return "%s;%s;%s;%s".formatted(code.toString(), name, maxPlayers, currentPlayersCount());
    }
}
