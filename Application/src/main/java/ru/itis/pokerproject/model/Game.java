package ru.itis.pokerproject.model;

import ru.itis.pokerproject.application.GameScreen;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private static List<PlayerInfo> players = new ArrayList<>();
    private static GameScreen gameScreen = null;

    public static List<PlayerInfo> getPlayers() {
        return players;
    }

    public static void setPlayers(List<PlayerInfo> playerss) {
        players = playerss;
    }

    public static void addPlayer(PlayerInfo player) {
        players.add(player);
    }

    public static void setGameScreen(GameScreen screen) {
        gameScreen = screen;
    }

    public static void updatePlayerStatus(String username, boolean status) {
        PlayerInfo player = players.stream().filter(p -> p.getUsername().equals(username)).findFirst().get();
        player.setReady(status);
    }

    public static GameScreen getGameScreen() {
        return gameScreen;
    }
}
