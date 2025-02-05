package ru.itis.pokerproject.model;

import ru.itis.pokerproject.shared.model.Card;
import javafx.beans.property.SimpleLongProperty;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo {
    private String username;
    // Заменяем поле money на наблюдаемое свойство
    private final SimpleLongProperty moneyProperty;
    private boolean isReady;
    private List<Card> hand;
    private boolean isFolded;
    private long currentBet;

    public PlayerInfo(String username, long money, boolean isReady) {
        this.username = username;
        this.moneyProperty = new SimpleLongProperty(money);
        this.isReady = isReady;
        hand = new ArrayList<>(2);
    }

    public String getUsername() {
        return username;
    }

    // Геттер для наблюдаемого свойства
    public SimpleLongProperty moneyProperty() {
        return moneyProperty;
    }

    // Геттер, возвращающий текущее значение
    public long getMoney() {
        return moneyProperty.get();
    }

    // Сеттер – обновляет значение свойства
    public void setMoney(long money) {
        this.moneyProperty.set(money);
    }

    public boolean isReady() {
        return isReady;
    }

    public void setUsername(String username) {
        this.username = username;
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

    // При вычитании денег обновляем наблюдаемое свойство
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
