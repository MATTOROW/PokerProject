package ru.itis.pokerproject.model;

import ru.itis.pokerproject.shared.model.Card;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo {
    private String username;
    private long money;
    private boolean isReady;
    private List<Card> hand;
    private boolean isFolded;
    private long currentBet;

    public PlayerInfo(String username, long money, boolean isReady) {
        this.username = username;
        this.money = money;
        this.isReady = isReady;
        hand = new ArrayList<>(2);
    }

    public String getUsername() { return username; }
    public long getMoney() { return money; }
    public boolean isReady() { return isReady; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public void subtractMoney(long money) {
        this.money -= money;
    }

    public boolean isFolded() {
        return isFolded;
    }

    public void setFolded(boolean folded) {
        isFolded = folded;
    }

    public long getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(long currentBet) {
        this.currentBet = currentBet;
    }
}
