package ru.itis.pokerproject.gameserver.models;

public class Player {
    private String username;
    private long money;

    public Player(String username, long money) {
        this.username = username;
        this.money = money;
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

    public void addMoney(long money) {
        this.money += money;
    }

    public void subtractMoney(long money) {
        this.money -= money;
    }
}
