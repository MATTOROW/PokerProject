package ru.itis.pokerproject.model;

import javafx.application.Platform;
import ru.itis.pokerproject.application.GameScreen;
import ru.itis.pokerproject.application.ScreenManager;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private static List<PlayerInfo> players = new ArrayList<>();
    private static PlayerInfo myPlayer;
    private static int maxPlayers;
    private static int currentPlayers;
    private static long minBet;
    private static GameScreen gameScreen = null;
    private static ScreenManager manager = null;

    public static List<PlayerInfo> getPlayers() {
        return players;
    }

    public static void setPlayers(List<PlayerInfo> playerss) {
        players = playerss;
    }

    public static PlayerInfo getMyPlayer() {
        return myPlayer;
    }

    public static void setMyPlayer(PlayerInfo myPlayer) {
        Game.myPlayer = myPlayer;
    }

    public static int getMaxPlayers() {
        return maxPlayers;
    }

    public static void setMaxPlayers(int maxPlayers) {
        Game.maxPlayers = maxPlayers;
    }

    public static int getCurrentPlayers() {
        return currentPlayers;
    }

    public static void setCurrentPlayers(int currentPlayers) {
        Game.currentPlayers = currentPlayers;
    }

    public static long getMinBet() {
        return minBet;
    }

    public static void setMinBet(long minBet) {
        Game.minBet = minBet;
    }

    public static void setGameScreen(GameScreen screen) {
        gameScreen = screen;
    }

    public static ScreenManager getManager() {
        return manager;
    }

    public static void setManager(ScreenManager manager) {
        Game.manager = manager;
    }

    private static void notifyUpdate() {
        if (gameScreen != null) {
            Platform.runLater(gameScreen::updateUI); // Обновляем UI в JavaFX-потоке
        }
    }

    public static void updatePlayerStatus(String username, boolean status) {
        if (username.equals(myPlayer.getUsername())) {
            myPlayer.setReady(true);
        } else {
            PlayerInfo player = players.stream().filter(p -> p.getUsername().equals(username)).findFirst().get();
            player.setReady(status);
        }
        notifyUpdate();
    }

    public static void addPlayer(PlayerInfo player) {
        players.add(player);
        notifyUpdate();
    }

    public static void removePlayer(String username) {
        players.remove(players.stream().filter(r -> r.getUsername().equals(username)).findFirst().orElse(null));
        notifyUpdate();
    }

    public static GameScreen getGameScreen() {
        return gameScreen;
    }
}
