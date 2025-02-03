package ru.itis.pokerproject.gameserver.model.game;

public enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES;

    @Override
    public String toString() {
        return name();
    }
}
