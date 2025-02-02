package ru.itis.pokerproject.model;

public class PlayerInfo {
    private String username;
    private long money;
    private boolean isReady;

    public PlayerInfo(String username, long money, boolean isReady) {
        this.username = username;
        this.money = money;
        this.isReady = isReady;
    }

    public String getUsername() { return username; }
    public long getMoney() { return money; }
    public boolean isReady() { return isReady; }
}
