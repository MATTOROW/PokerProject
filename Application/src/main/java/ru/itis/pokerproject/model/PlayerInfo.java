package ru.itis.pokerproject.model;

import javafx.beans.property.SimpleLongProperty;
import ru.itis.pokerproject.shared.model.Card;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo {
    private final SimpleLongProperty moneyProperty;
    private String username;
    private long defaultMoney;
    private boolean isReady;
    private List<Card> hand;
    private boolean isFolded;
    private long currentBet;

    public PlayerInfo(String username, long money, boolean isReady) {
        this.username = username;
        this.moneyProperty = new SimpleLongProperty(money);
        this.defaultMoney = money;
        this.isReady = isReady;
        hand = new ArrayList<>(2);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SimpleLongProperty moneyProperty() {
        return moneyProperty;
    }

    public long getMoney() {
        return moneyProperty.get();
    }

    public void setMoney(long money) {
        this.moneyProperty.set(money);
    }

    public long getDefaultMoney() {
        return defaultMoney;
    }

    public void setDefaultMoney(long defaultMoney) {
        this.defaultMoney = defaultMoney;
    }

    public boolean isReady() {
        return isReady;
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
        this.moneyProperty.set(this.moneyProperty.get() - money);
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
