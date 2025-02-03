package ru.itis.pokerproject.gameserver.service.game;

import ru.itis.pokerproject.gameserver.model.game.Player;

import java.util.ArrayList;
import java.util.List;

public class Pot {
    private long amount;
    private final List<Player> participants;

    public Pot() {
        this.amount = 0;
        this.participants = new ArrayList<>();
    }

    public void addAmount(long amount) {
        this.amount += amount;
    }

    public void addParticipant(Player player) {
        if (!participants.contains(player)) {
            participants.add(player);
        }
    }

    public long getAmount() {
        return amount;
    }

    public List<Player> getParticipants() {
        return participants;
    }
}