package ru.itis.pokerproject.gameserver.model.game;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private Socket socket;
    private String username;
    private long money;
    private boolean isReady;
    private List<Card> cards;
    private boolean isFolded;
    private boolean isAllIn;
    private long currentBet;

    public Player(Socket socket, String username, long money) {
        this.socket = socket;
        this.username = username;
        this.money = money;
        this.isReady = false;
        this.cards = new ArrayList<>();
        this.isFolded = false;
        this.currentBet = 0;
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

    public boolean isFolded() {
        return isFolded;
    }

    public void setFolded(boolean folded) {
        isFolded = folded;
    }

    public boolean isAllIn() {
        return isAllIn;
    }

    public void setAllIn(boolean allIn) {
        isAllIn = allIn;
    }

    public List<Card> getCards() {
        return cards;
    }

    public String getCardsInfo() {
        return String.join(";", cards.stream().map(Card::toString).toList());
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void addMoney(long money) {
        this.money += money;
    }

    public void subtractMoney(long money) {
        this.money -= money;
    }

    public long getCurrentBet() {
        return currentBet;
    }

    public void addBet(long money) {
        this.currentBet += money;
    }

    public void resetBet() {
        this.currentBet = 0;
    }

    public void subtractBet(long money) {
        this.currentBet -= money;
    }

    public void reset() {
        isReady = false;
        isFolded = false;
        cards = new ArrayList<>();
        currentBet = 0;
    }

    public String getInfo() {
        return "%s;%s;%s".formatted(username, money, isReady ? 1 : 0);
    }

    public Socket getSocket() {
        return socket;
    }
}
