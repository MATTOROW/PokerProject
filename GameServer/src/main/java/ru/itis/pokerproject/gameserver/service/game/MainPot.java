package ru.itis.pokerproject.gameserver.service.game;

import java.util.ArrayList;
import java.util.List;

public class MainPot {
    private long amount; // Сумма основного банка
    private List<SidePot> sidePots = new ArrayList<>(); // Дополнительные банки

    public void addAmount(long amount) {
        this.amount += amount;
    }

    public void addSidePot(SidePot sidePot) {
        sidePots.add(sidePot);
    }

    public long getAmount() {
        return amount;
    }

    public List<SidePot> getSidePots() {
        return sidePots;
    }
}