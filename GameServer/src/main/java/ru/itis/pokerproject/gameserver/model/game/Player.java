package ru.itis.pokerproject.gameserver.model.game;

import java.net.Socket;

public class Player {
    private Socket socket;
    private String username;
    private long money;
    private boolean isReady;

    public Player(Socket socket, String username, long money) {
        this.socket = socket;
        this.username = username;
        this.money = money;
        this.isReady = false;
    }

    public Player(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getMoney() {
        return money;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public void addMoney(long money) {
        this.money += money;
    }

    public void subtractMoney(long money) {
        this.money -= money;
    }

    public String getInfo() {
        return "%s;%s;%s".formatted(username, money, isReady ? 1 : 0);
    }

    public Socket getSocket() {
        return socket;
    }
}
