package ru.itis.pokerproject.gameserver.model.game;

import java.util.List;

public record HandWorth(HandType type, List<Card> normalizedHand, int value) implements Comparable<HandWorth> {

    @Override
    public int compareTo(HandWorth o) {
        return this.value - o.value;
    }
}
