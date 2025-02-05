package ru.itis.pokerproject.gameserver.model.game;

import ru.itis.pokerproject.shared.model.Card;

import java.net.Socket;
import java.util.List;

public class Player {
    private Socket socket;
    private String username;
    private long money;
    private long defaultMoney;
    private boolean isReady;
    private List<Card> hand;
    private boolean isFolded = false;
    private boolean isAllInned = false;
    private long currentBet;

    public Player(Socket socket, String username, long money) {
        this.socket = socket;
        this.username = username;
        this.money = money;
        this.defaultMoney = money;
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

    public List<Card> getHand() {
        return this.hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public boolean isFolded() {
        return isFolded;
    }

    public void setFolded(boolean folded) {
        isFolded = folded;
    }

    public boolean isAllInned() {
        return isAllInned;
    }

    public void setAllInned(boolean allInned) {
        isAllInned = allInned;
    }

    public long getDefaultMoney() {
        return defaultMoney;
    }

    public void setDefaultMoney(long defaultMoney) {
        this.defaultMoney = defaultMoney;
    }

    public long getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(long currentBet) {
        this.currentBet = currentBet;
    }

    public void reset() {
        isFolded = false;
        isAllInned = false;
        currentBet = 0;
        hand = null;
    }
}
