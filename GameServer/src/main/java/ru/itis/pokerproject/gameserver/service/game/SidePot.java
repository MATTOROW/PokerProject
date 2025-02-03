package ru.itis.pokerproject.gameserver.service.game;

import ru.itis.pokerproject.gameserver.model.game.Player;

import java.util.ArrayList;
import java.util.List;

public class SidePot {
    private long amount; // Общая сумма в этом банке
    private List<Player> eligiblePlayers; // Игроки, которые могут выиграть этот банк

    public SidePot(long amount, List<Player> eligiblePlayers) {
        this.amount = amount;
        this.eligiblePlayers = new ArrayList<>(eligiblePlayers);
    }

    public long getAmount() {
        return amount;
    }

    public List<Player> getEligiblePlayers() {
        return eligiblePlayers;
    }

    public void addAmount(long amount) {
        this.amount += amount;
    }
}